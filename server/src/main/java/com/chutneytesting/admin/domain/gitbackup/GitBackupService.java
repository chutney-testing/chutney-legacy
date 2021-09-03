package com.chutneytesting.admin.domain.gitbackup;

import static com.chutneytesting.admin.domain.gitbackup.ChutneyContentFSWriter.writeChutneyContent;
import static com.chutneytesting.tools.file.FileUtils.deleteFolder;
import static com.chutneytesting.tools.file.FileUtils.initFolder;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class GitBackupService {

    private static final Path ROOT_DIRECTORY_NAME = Paths.get("backups", "git");

    private final Remotes remotes;
    private final GitClient gitClient;
    private final Set<ChutneyContentProvider> contentProviders;
    private final Path gitRepositoryFolderPath;

    public GitBackupService(Remotes remotes,
                            GitClient gitClient,
                            Set<ChutneyContentProvider> contentProviders,
                            String gitRepositoryFolderPath) {
        this.remotes = remotes;
        this.gitClient = gitClient;
        this.contentProviders = contentProviders;
        this.gitRepositoryFolderPath = Paths.get(gitRepositoryFolderPath).resolve(ROOT_DIRECTORY_NAME).toAbsolutePath();
        initFolder(this.gitRepositoryFolderPath);
    }

    public List<RemoteRepository> getAll() {
        return remotes.getAll().stream()
            .map(r -> new RemoteRepository(r.name, r.url, r.branch, r.privateKeyPath, ""))
            .collect(Collectors.toList());
    }

    public RemoteRepository add(RemoteRepository remoteRepository) {
        if (gitClient.hasAccess(remoteRepository)) {
            return remotes.add(remoteRepository);
        }

        throw new UnreachableRemoteException("Remote cannot be reached. Please check provided information.");
    }

    public void remove(String name) {
        remotes.remove(name);
        deleteFolder(gitRepositoryFolderPath.resolve(name));
    }

    public void backup(String name) {
        this.backup(remotes.get(name));
    }

    public void backup(RemoteRepository remote) {
        Path workingDirectory = gitRepositoryFolderPath.resolve(remote.name);
        cleanWorkingFolder(workingDirectory);
        gitClient.initRepository(remote, workingDirectory);
        gitClient.update(remote, workingDirectory);
        writeChutneyContent(workingDirectory, contentProviders);
        gitClient.addAll(workingDirectory);
        gitClient.commit(workingDirectory, "Update Chutney content");
        gitClient.push(remote, workingDirectory);
    }

    private void cleanWorkingFolder(Path workingDirectory) {
        deleteFolder(workingDirectory);
        initFolder(workingDirectory);
    }

    public void backup() {
        backup(remotes.getAll().get(0));
    }
}
