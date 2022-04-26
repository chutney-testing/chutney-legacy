package com.chutneytesting.scenario.infra;

import static com.chutneytesting.tools.orient.ComposableIdUtils.toFrontId;

import com.chutneytesting.scenario.domain.ScenarioNotFoundException;
import com.chutneytesting.scenario.domain.TestCase;
import com.chutneytesting.scenario.domain.TestCaseMetadata;
import com.chutneytesting.scenario.domain.TestCaseMetadataImpl;
import com.chutneytesting.scenario.domain.TestCaseRepository;
import com.chutneytesting.scenario.domain.gwt.GwtTestCase;
import com.chutneytesting.scenario.infra.compose.OrientComposableTestCaseRepository;
import com.chutneytesting.scenario.infra.raw.DatabaseTestCaseRepository;
import com.chutneytesting.scenario.infra.raw.TestCaseData;
import com.chutneytesting.scenario.infra.raw.TestCaseDataMapper;
import com.orientechnologies.orient.core.id.ORecordId;
import java.util.Collections;
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

    private final DatabaseTestCaseRepository defaultRepository;
    private final OrientComposableTestCaseRepository composableTestCaseRepository;

    public TestCaseRepositoryAggregator(DatabaseTestCaseRepository defaultRepository,
                                        OrientComposableTestCaseRepository composableTestCaseRepository) {
        this.defaultRepository = defaultRepository;
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
        return Stream.of(defaultRepository);
    }

    @Override
    public TestCase findById(String scenarioId) {
        if (isComposableScenarioId(scenarioId)) { // TODO - Composable testcase repo should be able to be taken as others testcase repo
            return composableTestCaseRepository.findExecutableById(scenarioId);
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
        List<TestCaseMetadata> testCases = repositories()
            .parallel()
            .flatMap(this::findAllRepositoryStream)
            .collect(Collectors.toList());
        testCases.addAll(findAllComposableTestCase());
        return testCases;
    }

    @Override
    public void removeById(String scenarioId) {
        defaultRepository.removeById(scenarioId);
    }

    @Override
    public Integer lastVersion(String testCaseId) {
        if (isComposableScenarioId(testCaseId)) { // TODO - Composable testcase repo should be able to be taken as others testcase repo
            return composableTestCaseRepository.lastVersion(testCaseId);
        } else {
            return repositories()
                .map(repo -> repo.lastVersion(testCaseId))
                .filter(Optional::isPresent)
                .findFirst()
                .flatMap(tc -> tc)
                .orElseThrow(() -> new ScenarioNotFoundException(testCaseId));
        }
    }

    @Override
    public List<TestCaseMetadata> search(String textFilter) {
        List<TestCaseMetadata> testCases = repositories()
            .parallel()
            .flatMap(r -> searchAllRepositoryStream(r, textFilter))
            .collect(Collectors.toList());
        testCases.addAll(searchComposableTestCase(textFilter));
        return testCases;
    }

    private Stream<? extends TestCaseMetadata> findAllRepositoryStream(DelegateScenarioRepository repository) {
        try {
            return repository.findAll().stream();
        } catch (RuntimeException e) {
            LOGGER.warn("Could not aggregate scenarios from repository : " + repository.alias(), e);
            return Stream.empty();
        }
    }

    private Stream<? extends TestCaseMetadata> searchAllRepositoryStream(DelegateScenarioRepository repository, String textFilter) {
        try {
            return repository.search(textFilter).stream();
        } catch (RuntimeException e) {
            LOGGER.warn("Could not aggregate scenarios from repository : " + repository.alias(), e);
            return Stream.empty();
        }
    }


    private boolean isComposableScenarioId(String scenarioId) {
        return ORecordId.isA(scenarioId);
    }

    private List<TestCaseMetadata> findAllComposableTestCase() {
        try {
            return composableTestCaseRepository.findAll().stream()
                .map(testCaseMetadata -> TestCaseMetadataImpl.TestCaseMetadataBuilder.from(testCaseMetadata)
                    .withId(toFrontId(testCaseMetadata.id()))
                    .build())
                .collect(Collectors.toList());
        } catch (RuntimeException e) {
            LOGGER.warn("Could not find scenarios from composable repository", e);
            return Collections.emptyList();
        }
    }

    private List<TestCaseMetadata> searchComposableTestCase(String textFilter) {
        try {
            return composableTestCaseRepository.search(textFilter).stream()
                .map(testCaseMetadata -> TestCaseMetadataImpl.TestCaseMetadataBuilder.from(testCaseMetadata)
                    .withId(toFrontId(testCaseMetadata.id()))
                    .build())
                .collect(Collectors.toList());
        } catch (RuntimeException e) {
            LOGGER.warn("Could not search scenarios from composable repository", e);
            return Collections.emptyList();
        }
    }
}
