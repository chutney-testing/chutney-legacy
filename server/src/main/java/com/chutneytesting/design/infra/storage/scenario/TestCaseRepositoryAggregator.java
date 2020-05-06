package com.chutneytesting.design.infra.storage.scenario;

import com.chutneytesting.design.domain.compose.ComposableTestCaseRepository;
import com.chutneytesting.design.domain.scenario.ScenarioNotFoundException;
import com.chutneytesting.design.domain.scenario.TestCase;
import com.chutneytesting.design.domain.scenario.TestCaseMetadata;
import com.chutneytesting.design.domain.scenario.TestCaseRepository;
import com.chutneytesting.design.domain.scenario.gwt.GwtTestCase;
import com.chutneytesting.design.infra.storage.scenario.git.GitScenarioRepositoryFactory;
import com.chutneytesting.design.infra.storage.scenario.jdbc.DatabaseTestCaseRepository;
import com.chutneytesting.design.infra.storage.scenario.jdbc.TestCaseData;
import com.chutneytesting.design.infra.storage.scenario.jdbc.TestCaseDataMapper;
import com.chutneytesting.documentation.infra.ExamplesRepository;
import com.orientechnologies.orient.core.id.ORecordId;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

@Repository
public class TestCaseRepositoryAggregator implements TestCaseRepository {

    private static final Logger LOGGER = LoggerFactory.getLogger(TestCaseRepositoryAggregator.class);

    private DatabaseTestCaseRepository defaultRepository;
    private GitScenarioRepositoryFactory gitScenarioRepositoryFactory;
    private final ExamplesRepository examples;
    private final ComposableTestCaseRepository composableTestCaseRepository;

    public TestCaseRepositoryAggregator(DatabaseTestCaseRepository defaultRepository,
                                        GitScenarioRepositoryFactory gitScenarioRepositoryFactory,
                                        ExamplesRepository examples,
                                        ComposableTestCaseRepository composableTestCaseRepository) {
        this.defaultRepository = defaultRepository;
        this.gitScenarioRepositoryFactory = gitScenarioRepositoryFactory;
        this.examples = examples;
        this.composableTestCaseRepository = composableTestCaseRepository;
    }

    @Override
    public String save(GwtTestCase testCase) {
        DelegateScenarioRepository repository = findRepository(testCase.metadata.repositorySource);

        if (savingIsAllowed(repository)) {
            return repository.save(TestCaseDataMapper.toDto(testCase));
        } else {
            throw new IllegalArgumentException("Saving to repository other than default local is not allowed");
        }
    }

    private boolean savingIsAllowed(DelegateScenarioRepository repository) {
        return repository.alias().equals(DEFAULT_REPOSITORY_SOURCE);
    }

    private DelegateScenarioRepository findRepository(String source) {
        return repositories()
            .filter(repo -> repo.alias().equals(source))
            .findFirst()
            .orElse(defaultRepository);
    }

    private Stream<DelegateScenarioRepository> repositories() {
        return Stream.concat(
            Stream.of(defaultRepository, examples),
            gitScenarioRepositoryFactory.listGitRepo()
        );
    }

    @Override
    public TestCase findById(String scenarioId) {
        if (isComposableScenarioId(scenarioId)) { // TODO - Composable testcase repo should be able to be taken as others testcase repo
            return composableTestCaseRepository.findById(scenarioId);
        } else {
            TestCaseData testCaseData = repositories()
                .map(repo -> repo.findById(scenarioId))
                .filter(Optional::isPresent)
                .findFirst()
                .flatMap(tc -> tc)
                .orElseThrow(() -> new ScenarioNotFoundException(scenarioId));

            return TestCaseDataMapper.fromDto(testCaseData);
        }
    }

    @Override
    public TestCaseMetadata findMetadataById(String scenarioId) {
        return findById(scenarioId).metadata();
    }

    @Override
    public List<TestCaseMetadata> findAll() {
        return repositories()
            .parallel()
            .flatMap(this::findAllRepositoryStream)
            .collect(Collectors.toList());
    }

    @Override
    public void removeById(String scenarioId) {
        defaultRepository.removeById(scenarioId);
    }


    private Stream<? extends TestCaseMetadata> findAllRepositoryStream(DelegateScenarioRepository repository) {
        try {
            return repository.findAll().stream();
        } catch (RuntimeException e) {
            LOGGER.warn("Could not aggregate scenarios from repository : " + repository.alias(), e);
            return Stream.empty();
        }
    }

    private boolean isComposableScenarioId(String scenarioId) {
        return ORecordId.isA(scenarioId);
    }

}
