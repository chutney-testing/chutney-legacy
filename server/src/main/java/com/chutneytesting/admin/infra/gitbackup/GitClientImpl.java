package com.chutneytesting.admin.infra.gitbackup;

import com.chutneytesting.admin.domain.gitbackup.GitClient;
import com.chutneytesting.admin.domain.gitbackup.RemoteRepository;
import com.chutneytesting.tools.file.FileUtils;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.Optional;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.LsRemoteCommand;
import org.eclipse.jgit.api.MergeCommand;
import org.eclipse.jgit.api.TransportConfigCallback;
import org.eclipse.jgit.api.errors.EmptyCommitException;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.RefNotAdvertisedException;
import org.eclipse.jgit.api.errors.RefNotFoundException;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.eclipse.jgit.transport.JschConfigSessionFactory;
import org.eclipse.jgit.transport.OpenSshConfig;
import org.eclipse.jgit.transport.SshTransport;
import org.eclipse.jgit.transport.URIish;
import org.eclipse.jgit.util.FS;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class GitClientImpl implements GitClient {

    private static final Logger LOGGER = LoggerFactory.getLogger(GitClientImpl.class);

    @Override
    public void clone(RemoteRepository remote, Path cloningPath) {
        try {
            Files.createDirectories(cloningPath);
        } catch (IOException e) {
            throw new RuntimeException("Cannot create repository for cloning : " + remote.url + ". " + e.getMessage(), e);
        }

        Git git = null;
        try {
            git = Git.cloneRepository()
                .setRemote(remote.name)
                .setURI(remote.url)
                .setDirectory(cloningPath.toFile())
                .setBranch(remote.branch)
                .setNoCheckout(true)
                .setNoTags()
                .setTransportConfigCallback(getTransportConfigCallback(remote))
                .call();
        } catch (GitAPIException e) {
            throw new RuntimeException("Cannot clone repository: " + remote.url + ". " + e.getMessage(), e);
        } finally {
            Optional.ofNullable(git)
                .ifPresent(Git::close);
        }
    }

    @Override
    public void update(RemoteRepository remote, Path workingDirectory) {
        try (Repository repository = FileRepositoryBuilder.create(workingDirectory.resolve(".git").toFile());
             Git git = Git.wrap(repository)
        ) {
            git.pull()
                .setRemote(remote.name)
                .setFastForward(MergeCommand.FastForwardMode.FF_ONLY)
                .setTransportConfigCallback(getTransportConfigCallback(remote))
                .call();
        } catch (RefNotAdvertisedException e) {
            createBranch(remote, workingDirectory);
        } catch (Exception e) {
            throw new RuntimeException("Cannot update repository " + remote.url + ". " + e.getMessage(), e);
        }
    }

    @Override
    public void createBranch(RemoteRepository remote, Path workingDirectory) {
        try (Repository repository = FileRepositoryBuilder.create(workingDirectory.resolve(".git").toFile());
             Git git = Git.wrap(repository)
        ) {
            git.branchCreate()
                .setName(remote.branch)
                .call();
        } catch (RefNotFoundException e) {
            // falling here might mean that the remote repo is new and empty
            initRepository(remote, workingDirectory);
        } catch (Exception e) {
            throw new RuntimeException("Cannot create branch " + remote.url + ". " + e.getMessage(), e);
        }
    }

    @Override
    public void updateRemote(RemoteRepository remote, Path workingDirectory) {
        try (Repository repository = FileRepositoryBuilder.create(workingDirectory.resolve(".git").toFile());
             Git git = Git.wrap(repository)
        ) {
            git.remoteRemove()
                .setRemoteName(remote.name)
                .call();
        } catch (Exception e) {
            throw new RuntimeException("Cannot update remote : " + remote.url + ". " + e.getMessage(), e);
        }
    }

    @Override
    public void initRepository(RemoteRepository remote, Path workingDirectory) {
        try {
            FileUtils.deleteFolder(workingDirectory);
            Files.createDirectories(workingDirectory);
        } catch (IOException e) {
            throw new RuntimeException("Cannot init repository: " + remote.url + ". " + e.getMessage(), e);
        }

        try (Git git = Git.init().setInitialBranch(remote.branch).setDirectory(workingDirectory.toFile()).call()) {
            git.remoteAdd()
                .setName(remote.name)
                .setUri(new URIish(remote.url))
                .call();
        } catch (GitAPIException | URISyntaxException e) {
            throw new RuntimeException("Cannot create git repository at path : " + workingDirectory + ". " + e.getMessage(), e);
        }
    }

    @Override
    public void addAll(Path workingDirectory) {
        try (Repository repository = FileRepositoryBuilder.create(workingDirectory.resolve(".git").toFile());
             Git git = Git.wrap(repository)
        ) {
            git.add().addFilepattern(".").call(); // stage all modified and untracked files
            git.add().setUpdate(true).addFilepattern(".").call(); // stage all deleted files
        } catch (Exception e) {
            throw new RuntimeException("Cannot stage all files at path : " + workingDirectory + ". " + e.getMessage(), e);
        }
    }

    @Override
    public boolean isGitDir(Path workingDirectory) {
        return Files.exists(workingDirectory.resolve(".git"));
    }

    @Override
    public void commit(Path workingDirectory, String message) {
        try (Repository repository = FileRepositoryBuilder.create(workingDirectory.resolve(".git").toFile());
             Git git = Git.wrap(repository)
        ) {
            git.commit()
                .setSign(false)
                .setAllowEmpty(false)
                .setAuthor("Chutney", "no-reply@chutney-testing.com")
                .setMessage(message)
                .call();
        } catch (EmptyCommitException e) {
            // do nothing
        } catch (IOException | GitAPIException e) {
            throw new RuntimeException("Cannot commit files at path : " + workingDirectory + ". " + e.getMessage(), e);
        }
    }

    @Override
    public void push(RemoteRepository remote, Path workingDirectory) {
        try (Repository repository = FileRepositoryBuilder.create(workingDirectory.resolve(".git").toFile());
             Git git = Git.wrap(repository)
        ) {
            git.push()
                .setRemote(remote.name)
                .setTransportConfigCallback(getTransportConfigCallback(remote))
                .call();

        } catch (IOException | GitAPIException e) {
            throw new RuntimeException("Cannot push to remote : " + remote.url + ". " + e.getMessage(), e);
        }
    }

    @Override
    public boolean hasAccess(RemoteRepository remote) {
        try {
            LsRemoteCommand lsRemoteCommand = Git.lsRemoteRepository()
                .setRemote(remote.url)
                .setTags(true)
                .setHeads(true)
                .setTimeout(5)
                .setTransportConfigCallback(getTransportConfigCallback(remote));

            Collection<Ref> remoteRefs = lsRemoteCommand.call();

            if (remoteRefs.isEmpty() || remoteRefs.stream().noneMatch(ref -> ref.getName().contains(remote.branch))) {
                LOGGER.warn("No branch " + remote.branch + " found on remote " + remote.url);
            }

        } catch (GitAPIException e) {
            LOGGER.warn("Remote " + remote.url + " is not accessible. " + e.getMessage(), e);
            return false;
        }

        return true;
    }

    private TransportConfigCallback getTransportConfigCallback(RemoteRepository remote) {
        return transport -> {
            SshTransport sshTransport = (SshTransport) transport;
            sshTransport.setSshSessionFactory(new JschConfigSessionFactory() {

                @Override
                protected void configure(OpenSshConfig.Host host, Session session) {
                    session.setConfig("StrictHostKeyChecking", "no");
                }

                @Override
                protected JSch createDefaultJSch(FS fs) throws JSchException {
                    JSch defaultJSch = super.createDefaultJSch(fs);
                    defaultJSch.removeAllIdentity();
                    if (Files.exists(Paths.get(remote.privateKeyPath))) {
                        defaultJSch.addIdentity(remote.privateKeyPath, remote.privateKeyPassphrase);
                    }
                    return defaultJSch;
                }

            });
        };
    }


}
