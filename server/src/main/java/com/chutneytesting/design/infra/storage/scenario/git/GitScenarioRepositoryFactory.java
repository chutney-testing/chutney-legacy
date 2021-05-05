package com.chutneytesting.design.infra.storage.scenario.git;

import com.chutneytesting.admin.infra.storage.JsonFilesGitRepository;
import com.chutneytesting.design.infra.storage.scenario.DelegateScenarioRepository;
import com.chutneytesting.design.infra.storage.scenario.jdbc.TestCaseData;
import com.chutneytesting.design.infra.storage.scenario.git.json.versionned.JsonMapper;
import java.util.stream.Stream;
import org.springframework.stereotype.Service;

@Service
public class GitScenarioRepositoryFactory {

    private final GitClient gitClient;
    private final JsonMapper<TestCaseData> jsonMapper;
    private final JsonFilesGitRepository jsonFilesGitRepository;

    public GitScenarioRepositoryFactory(GitClient gitClient, JsonMapper<TestCaseData> jsonMapper, JsonFilesGitRepository jsonFilesGitRepository) {
        this.gitClient = gitClient;
        this.jsonMapper = jsonMapper;
        this.jsonFilesGitRepository = jsonFilesGitRepository;
    }

    public Stream<DelegateScenarioRepository> listGitRepo() {
        return jsonFilesGitRepository.listGitRepository()
            .stream()
            .map(this::create);
    }

    private DelegateScenarioRepository create(GitRepository repo) {
        return new GitScenarioRepository(
            repo,
            gitClient,
            jsonMapper);

    }
}
