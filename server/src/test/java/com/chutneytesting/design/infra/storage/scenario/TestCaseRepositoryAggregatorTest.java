package com.chutneytesting.design.infra.storage.scenario;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatExceptionOfType;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.chutneytesting.design.domain.compose.ComposableTestCaseRepository;
import com.chutneytesting.design.domain.scenario.ScenarioNotFoundException;
import com.chutneytesting.design.domain.scenario.TestCaseMetadata;
import com.chutneytesting.design.domain.scenario.TestCaseMetadataImpl;
import com.chutneytesting.design.domain.scenario.TestCaseRepository;
import com.chutneytesting.design.domain.scenario.gwt.GwtScenario;
import com.chutneytesting.design.domain.scenario.gwt.GwtStep;
import com.chutneytesting.design.domain.scenario.gwt.GwtTestCase;
import com.chutneytesting.design.infra.storage.scenario.git.GitScenarioRepositoryFactory;
import com.chutneytesting.design.infra.storage.scenario.jdbc.DatabaseTestCaseRepository;
import com.chutneytesting.documentation.infra.ExamplesRepository;
import java.time.Instant;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

public class TestCaseRepositoryAggregatorTest {

    private GitScenarioRepositoryFactory gitScenarioRepositoryFactory = mock(GitScenarioRepositoryFactory.class);
    private ExamplesRepository examples = mock(ExamplesRepository.class);
    private ComposableTestCaseRepository composableTestCaseRepository = mock(ComposableTestCaseRepository.class);

    @Test
    public void should_throw_exception_when_try_to_save_to_repo_other_than_default() {
        // Given
        final String REPO_SOURCE = "REPO_1";
        DatabaseTestCaseRepository repo1 = mock(DatabaseTestCaseRepository.class);
        when(repo1.alias()).thenReturn(REPO_SOURCE);
        when(examples.alias()).thenReturn("examples");
        GwtTestCase testCase = defaultScenarioWithRepoSource(REPO_SOURCE);

        TestCaseRepositoryAggregator sut = new TestCaseRepositoryAggregator(repo1, gitScenarioRepositoryFactory, examples, composableTestCaseRepository);

        // When
        assertThatThrownBy(() -> sut.save(testCase))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Saving to repository other than default local is not allowed");
    }

    @Test
    public void should_save_in_default_repo_when_source_is_unknown() {
        // Given
        DatabaseTestCaseRepository repo1 = mock(DatabaseTestCaseRepository.class);
        when(repo1.alias()).thenReturn(TestCaseRepository.DEFAULT_REPOSITORY_SOURCE);
        when(examples.alias()).thenReturn("examples");
        GwtTestCase testCase = defaultScenarioWithRepoSource("UNKNOWN_REPO");

        TestCaseRepositoryAggregator sut = new TestCaseRepositoryAggregator(repo1, gitScenarioRepositoryFactory, examples, composableTestCaseRepository);

        // When
        sut.save(testCase);

        // Then
        verify(repo1).save(Mockito.any());
    }

    @Test
    public void should_call_every_repos_when_search_not_existing_scenario() {
        // Given
        DatabaseTestCaseRepository repo1 = mock(DatabaseTestCaseRepository.class);
        DelegateScenarioRepository repo2 = mock(DelegateScenarioRepository.class);
        DelegateScenarioRepository repo3 = mock(DelegateScenarioRepository.class);

        when(gitScenarioRepositoryFactory.listGitRepo()).thenReturn(Stream.of(repo2, repo3));

        TestCaseRepositoryAggregator sut = new TestCaseRepositoryAggregator(repo1, gitScenarioRepositoryFactory, examples, composableTestCaseRepository);

        final String scenarioId = "12345";

        // When
        assertThatExceptionOfType(ScenarioNotFoundException.class)
            .isThrownBy(() -> sut.findById(scenarioId));

        // Then
        verify(repo1).findById(scenarioId);
        verify(examples).findById(scenarioId);
        verify(repo2).findById(scenarioId);
        verify(repo3).findById(scenarioId);
    }

    @Test
    public void should_not_call_every_repos_when_remove() {
        // Given
        DatabaseTestCaseRepository repo1 = mock(DatabaseTestCaseRepository.class);
        DelegateScenarioRepository repo2 = mock(DelegateScenarioRepository.class);
        DelegateScenarioRepository repo3 = mock(DelegateScenarioRepository.class);

        when(gitScenarioRepositoryFactory.listGitRepo()).thenReturn(Stream.of(repo2, repo3));

        TestCaseRepositoryAggregator sut = new TestCaseRepositoryAggregator(repo1, gitScenarioRepositoryFactory, examples, composableTestCaseRepository);

        final String scenarioId = "12345";

        // When
        sut.removeById(scenarioId);

        // Then
        verify(repo1).removeById(scenarioId);
        verify(examples, times(0)).removeById(scenarioId);
        verify(repo2, times(0)).removeById(scenarioId);
        verify(repo3, times(0)).removeById(scenarioId);
    }

    @Test
    public void should_aggregate_all_repos_scenarios_when_findAll() {
        // Given
        DatabaseTestCaseRepository repo1 = mock(DatabaseTestCaseRepository.class);
        DelegateScenarioRepository repo2 = mock(DelegateScenarioRepository.class);

        when(gitScenarioRepositoryFactory.listGitRepo()).thenReturn(Stream.of(repo2));

        when(repo1.findAll()).thenReturn(asList(mock(TestCaseMetadata.class), mock(TestCaseMetadata.class)));
        when(repo2.findAll()).thenReturn(singletonList(mock(TestCaseMetadata.class)));

        TestCaseRepositoryAggregator sut = new TestCaseRepositoryAggregator(repo1, gitScenarioRepositoryFactory, examples, composableTestCaseRepository);

        // When
        final List<TestCaseMetadata> allScenario = sut.findAll();

        // Then
        assertThat(allScenario).hasSize(3);
    }

    @Test
    public void should_aggregate_all_repos_available_scenarios_when_findAll_with_one_repo_failed() {
        // Given
        DatabaseTestCaseRepository repo1 = mock(DatabaseTestCaseRepository.class);
        DelegateScenarioRepository repo2 = mock(DelegateScenarioRepository.class);
        DelegateScenarioRepository repo3 = mock(DelegateScenarioRepository.class);

        when(repo1.findAll()).thenReturn(asList(mock(TestCaseMetadata.class), mock(TestCaseMetadata.class)));
        when(repo2.findAll()).thenThrow(new RuntimeException("Error searching for scenarios !!!"));
        when(repo3.findAll()).thenReturn(singletonList(mock(TestCaseMetadata.class)));

        when(gitScenarioRepositoryFactory.listGitRepo()).thenReturn(Stream.of(repo2, repo3));

        TestCaseRepositoryAggregator sut = new TestCaseRepositoryAggregator(repo1, gitScenarioRepositoryFactory, examples, composableTestCaseRepository);

        // When
        final List<TestCaseMetadata> allScenario = sut.findAll();

        // Then
        assertThat(allScenario).hasSize(3);
    }

    private GwtTestCase defaultScenarioWithRepoSource(String repositorySource) {
        return GwtTestCase.builder()
            .withMetadata(TestCaseMetadataImpl.builder()
                .withCreationDate(Instant.now())
                .withRepositorySource(repositorySource)
                .build())
            .withScenario(GwtScenario.builder().withWhen(GwtStep.NONE).build())
            .build();
    }
}
