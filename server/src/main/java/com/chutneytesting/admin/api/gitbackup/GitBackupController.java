package com.chutneytesting.admin.api.gitbackup;

import com.chutneytesting.admin.domain.gitbackup.RemoteRepository;
import com.chutneytesting.admin.domain.gitbackup.GitBackupService;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/backups/git")
@CrossOrigin(origins = "*")
public class GitBackupController {

    private final GitBackupService gitBackupService;

    public GitBackupController(GitBackupService gitBackupService) {
        this.gitBackupService = gitBackupService;
    }

    @PreAuthorize("hasAuthority('ADMIN_ACCESS')")
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public List<GitRemoteDto> getAllRemotes() {
        return gitBackupService.getAll().stream()
            .map(this::toDto)
            .collect(Collectors.toList());
    }

    @PreAuthorize("hasAuthority('ADMIN_ACCESS')")
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public GitRemoteDto addRemote(@RequestBody GitRemoteDto dto) {
        return toDto(gitBackupService.add(fromDto(dto)));
    }

    @PreAuthorize("hasAuthority('ADMIN_ACCESS')")
    @DeleteMapping(path = "/{name}")
    public void removeRemote(@PathVariable("name") String name) {
        gitBackupService.remove(name);
    }

    @PreAuthorize("hasAuthority('ADMIN_ACCESS')")
    @GetMapping(path = "/{name}/backup")
    public void backup(@PathVariable("name") String name) {
        gitBackupService.backup(name);
    }

    private GitRemoteDto toDto(RemoteRepository remote) {
        return ImmutableGitRemoteDto.builder()
            .name(remote.name)
            .url(remote.url)
            .branch(remote.branch)
            .privateKeyPath(remote.privateKeyPath)
            .privateKeyPassphrase(remote.privateKeyPassphrase)
            .build();
    }

    private RemoteRepository fromDto(GitRemoteDto dto) {
        return new RemoteRepository(dto.name(), dto.url(), dto.branch(), dto.privateKeyPath(), dto.privateKeyPassphrase());
    }
}
