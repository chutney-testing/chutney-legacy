package com.chutneytesting.admin.domain.gitbackup;

import static com.chutneytesting.admin.domain.gitbackup.ChutneyContentFSWriter.cleanWorkingFolder;
import static com.chutneytesting.admin.domain.gitbackup.ChutneyContentFSWriter.writeChutneyContent;

import com.chutneytesting.tools.file.FileUtils;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class GitBackupService {

    private final Remotes remotes;
    private final GitClient gitClient;
    private final Set<ChutneyContentProvider> contentProviders;
    private final String gitRepositoryFolderPath;

    public GitBackupService(Remotes remotes,
                            GitClient gitClient,
                            Set<ChutneyContentProvider> contentProviders,
                            @Value("${configuration-folder:conf/backups}") String gitRepositoryFolderPath) {
        this.remotes = remotes;
        this.gitClient = gitClient;
        this.contentProviders = contentProviders;
        this.gitRepositoryFolderPath = gitRepositoryFolderPath;
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
        FileUtils.deleteFolder(Paths.get(gitRepositoryFolderPath).resolve(name));
    }

    public void backup(String name) {
        this.backup(remotes.get(name));
    }

    public void backup(RemoteRepository remote) {
        Path workingDirectory = Paths.get(gitRepositoryFolderPath).resolve(remote.name);
        gitClient.initRepository(remote, workingDirectory);

        writeChutneyContent(workingDirectory, contentProviders);

        // commit
        gitClient.addAll(workingDirectory);
        gitClient.commit(workingDirectory, "Update Chutney content");

        // push to remote
        gitClient.push(remote, workingDirectory);
    }

}
