package com.chutneytesting.design.infra.storage.scenario.git;

import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.function.Consumer;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.ListBranchCommand.ListMode;
import org.eclipse.jgit.api.MergeCommand.FastForwardMode;
import org.eclipse.jgit.api.TransportConfigCallback;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.eclipse.jgit.transport.JschConfigSessionFactory;
import org.eclipse.jgit.transport.OpenSshConfig.Host;
import org.eclipse.jgit.transport.SshTransport;
import org.eclipse.jgit.util.FS;
import com.chutneytesting.tools.Try;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * More example of the use of jgit here : https://github.com/centic9/jgit-cookbook
 */
@Service
public class GitClient {

    private static final Logger LOGGER = LoggerFactory.getLogger(GitClient.class);

    public void loadRepository(String repoUrl, String repoName) {
        final File directory = getGitDirectory(repoName).toFile();
        if (!isValidGitRoot(directory.getPath())) {
            cloneRepository(repoUrl, repoName);
        }

        try {
            updateRepository(getGit(directory), repoName);
        } catch (IOException e) {
            LOGGER.error("Cannot update repoository " + repoName, e);
        }
    }

    public void removeCommitPushFile(String repoName, String commitMessage, String fileName) {
        Consumer<Git> consumer = git -> {
            Try.exec(() -> git.rm().addFilepattern(fileName).call());
        };

        doThenCommitAndPush(repoName, commitMessage, consumer);
    }

    public void addCommitPushFile(String repoName, String commitMessage) {
        Consumer<Git> consumer = git -> {
            Try.exec(() -> git.add().addFilepattern(".").call());
        };

        doThenCommitAndPush(repoName, commitMessage, consumer);
    }

    private void doThenCommitAndPush(String repoName, String commitMessage, Consumer<Git> stageFile) {
        try {
            final Git git = getGit(getGitDirectory(repoName).toFile());

            stageFile.accept(git);

            // and then commit the changes.
            git.commit().setSign(false).setMessage(commitMessage).call();

            // push if remote branch
            if (git.branchList().setListMode(ListMode.REMOTE).call().size() > 0) {
                updateRepository(git, repoName);
                git.push().setTransportConfigCallback(getTransportConfigCallback(repoName)).setRemote("origin").call();
            }
        } catch (IOException | GitAPIException e) {
            throw new RuntimeException("Cannot commit: <" + commitMessage + "> to repo: " + repoName, e);
        }
    }

    public Path getGitDirectory(String repoName) {
        return Paths.get(System.getProperty("user.home")).resolve(repoName);
    }

    private void updateRepository(Git git, String repoName) throws IOException {
        try {
            git.fetch().setTransportConfigCallback(getTransportConfigCallback(repoName)).call();
            git.merge().setFastForward(FastForwardMode.FF_ONLY).setCommit(false).include(git.getRepository().findRef("HEAD")).call();
        } catch (GitAPIException e) {
            LOGGER.warn("Cannot fetch/merge repository: " + git.getRepository() + ". " + e.getMessage());
        }
    }

    private void cloneRepository(String repoUrl, String repoName) {
        try {
            final Path repoDirectory = getGitDirectory(repoName);
            Files.createDirectories(repoDirectory);
            Git.cloneRepository().setTransportConfigCallback(getTransportConfigCallback(repoName)).setURI(repoUrl).setDirectory(repoDirectory.toFile()).call();
        } catch (GitAPIException | IOException e) {
            LOGGER.warn("Cannot clone repository: " + repoUrl + ". " + e.getMessage());
        }
    }

    private Git getGit(File directory) throws IOException {
        try {
            final Repository repository = new FileRepositoryBuilder().findGitDir(directory).build();
            return new Git(repository);
        }
        catch (IllegalArgumentException e) {
            LOGGER.info("Cannnot find git repository at " + directory + ". I will try to create it for you.");
        }

        return initLocalRepository(directory);
    }

    private Git initLocalRepository(File directory) {
        try {
            return Git.init().setDirectory(directory).call();
        } catch (GitAPIException e) {
            throw new RuntimeException("Cannot create git repository at path : " + directory);
        }
    }

    private boolean isValidGitRoot(String path) {
        final File gitRoot = new File(path);
        if (!gitRoot.isDirectory()) {
            return false;
        }

        final File gitDirectoryInRoot = new File(gitRoot, ".git");
        return gitDirectoryInRoot.isDirectory();
    }

    private TransportConfigCallback getTransportConfigCallback(String repoName) {
        return transport -> ((SshTransport) transport).setSshSessionFactory(
            new JschConfigSessionFactory() {
                @Override
                protected void configure(Host hc, Session session) {
                    session.setConfig("StrictHostKeyChecking", "no");
                }

                @Override
                protected JSch createDefaultJSch(FS fs) throws JSchException {
                    JSch defaultJSch = super.createDefaultJSch(fs);
                    if(Files.exists(Paths.get("/home/webadm/.ssh/id_rsa_" + repoName))) {
                        defaultJSch.addIdentity("/home/webadm/.ssh/id_rsa_" + repoName);
                    }
                    return defaultJSch;
                }
            });
    }
}
