package com.chutneytesting.admin.domain.gitbackup;

import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

@Component
public class GitBackupService {

    private final Remotes remotes;
    private final GitClient gitClient;

    public GitBackupService(Remotes remotes, GitClient gitClient1) {
        this.remotes = remotes;
        this.gitClient = gitClient1;
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
        // WIP
    }

    public void backup(String name) {
        this.backup(remotes.get(name));
    }
}
