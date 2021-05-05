package com.chutneytesting.admin.api;

import com.chutneytesting.admin.infra.storage.JsonFilesGitRepository;
import com.chutneytesting.design.infra.storage.scenario.git.GitRepository;
import java.util.List;
import java.util.Set;
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
@RequestMapping("/api/source/git/v1")
@CrossOrigin(origins = "*")
public class GitRepositoryAdminController {

    private final JsonFilesGitRepository jsonFilesGitRepository;

    public GitRepositoryAdminController(JsonFilesGitRepository jsonFilesGitRepository) {
        this.jsonFilesGitRepository = jsonFilesGitRepository;
    }

    @PreAuthorize("hasAuthority('ADMIN_ACCESS')")
    @GetMapping(path = "", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<GitRepositoryDto> getGitRepositories() {
        return jsonFilesGitRepository.listGitRepository().stream()
            .map(r -> new GitRepositoryDto(
                r.id,
                r.url,
                r.repositoryName,
                r.testSubFolder)).collect(Collectors.toList());
    }

    @PreAuthorize("hasAuthority('ADMIN_ACCESS')")
    @PostMapping(path = "", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public void addNewGitRepository(@RequestBody GitRepositoryDto repo) {
        Set<GitRepository> gitRepositories = jsonFilesGitRepository.listGitRepository();
        long id = gitRepositories.stream()
            .filter(g -> g.id.equals(repo.id))
            .map(g -> g.id)
            .findFirst()
            .orElseGet(() -> gitRepositories.stream()
                .mapToLong(g -> g.id)
                .max()
                .orElse(0L) + 1);

        GitRepository gitRepository = new GitRepository(id, repo.url, repo.sourceDirectory, repo.name);
        jsonFilesGitRepository.save(gitRepository);
    }

    @PreAuthorize("hasAuthority('ADMIN_ACCESS')")
    @DeleteMapping(path = "/{repoId}")
    public void deleteGitRepository(@PathVariable("repoId") Long id) {
        Set<GitRepository> gitRepositories = jsonFilesGitRepository.listGitRepository();
        gitRepositories.stream().filter(g -> g.id.equals(id))
            .findFirst()
            .ifPresent(gitRepo -> jsonFilesGitRepository.delete(id));
    }

    public static class GitRepositoryDto {
        public final String url;
        public final String name;
        public final String sourceDirectory;
        public final Long id;

        public GitRepositoryDto(Long id, String url, String name, String sourceDirectory) {
            this.id = id;
            this.url = url;
            this.name = name;
            this.sourceDirectory = sourceDirectory;
        }

        /**
         * For jackson selialization
         */
        @SuppressWarnings("unused")
        public GitRepositoryDto() {
            this(-1L, "", "", "");
        }
    }
}
