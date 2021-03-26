package com.chutneytesting.admin.domain.gitbackup;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class GitBackupService {

    private final Remotes remotes;
    private final GitClient gitClient;

    @Value("${configuration-folder:conf/backups}")
    private String gitRepositoryFolderPath;

    public GitBackupService(Remotes remotes, GitClient gitClient) {
        this.remotes = remotes;
        this.gitClient = gitClient;
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

        throw new IllegalArgumentException("Remote cannot be reached. Please check provided information");
    }

    public void remove(String name) {
        remotes.remove(name);
    }

    public void backup(RemoteRepository remote) {
        Path workingDirectory = Paths.get(gitRepositoryFolderPath).resolve(remote.name);
        if (Files.notExists(workingDirectory)) {
            gitClient.clone(remote, workingDirectory);
        }

        gitClient.update(remote, workingDirectory);
        // prepare files

        // commit
        gitClient.addAll(workingDirectory);
        gitClient.commit(workingDirectory, "TEST");

        // push to remote
        gitClient.push(remote, workingDirectory);
    }

    public void backup(String name) {
        this.backup(remotes.get(name));
    }
}
