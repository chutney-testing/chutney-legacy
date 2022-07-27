package com.chutneytesting.scenario.infra;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.chutneytesting.scenario.domain.AggregatedRepository;
import com.chutneytesting.scenario.domain.ComposableTestCase;
import com.chutneytesting.scenario.domain.TestCase;
import com.chutneytesting.scenario.domain.TestCaseMetadata;
import com.chutneytesting.scenario.domain.TestCaseMetadataImpl;
import com.chutneytesting.scenario.domain.TestCaseRepositoryAggregator;
import com.chutneytesting.scenario.domain.gwt.GwtScenario;
import com.chutneytesting.scenario.domain.gwt.GwtStep;
import com.chutneytesting.scenario.domain.gwt.GwtTestCase;
import com.chutneytesting.scenario.infra.raw.DatabaseTestCaseRepository;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;

public class TestCaseRepositoryAggregatorTest {

    private final AggregatedRepository<ComposableTestCase> composableTestCaseRepository = mock(OrientComposableTestCaseRepository.class);


/*    @Test
    public void should_save_in_default_repo_when_source_is_unknown() {
        // Given
        DatabaseTestCaseRepository repo1 = mock(DatabaseTestCaseRepository.class);
        when(repo1.alias()).thenReturn(TestCaseRepository.DEFAULT_REPOSITORY_SOURCE);
        GwtTestCase testCase = defaultScenarioWithRepoSource("UNKNOWN_REPO");

        TestCaseRepositoryAggregator sut = new TestCaseRepositoryAggregator(repo1, composableTestCaseRepository);

        // When
        sut.save(testCase);

        // Then
        verify(repo1).save(any());
        verify(composableTestCaseRepository, times(0)).save(any());
    }*/

    @Test
    public void should_call_findById_on_all_repos_even_if_one_fails() {
        // Given
        AggregatedRepository repo1 = mock(AggregatedRepository.class);
        AggregatedRepository repo2 = mock(AggregatedRepository.class);

        when(repo1.findById(any())).thenThrow(RuntimeException.class);

        TestCaseRepositoryAggregator sut = new TestCaseRepositoryAggregator(List.of(repo1, repo2));

        final String scenarioId = "12345";

        // When
        Optional<TestCase> actual = sut.findById(scenarioId);

        // Then
        assertThat(actual).isEmpty();
        verify(repo1).findById(scenarioId);
        verify(repo2).findById(scenarioId);
    }

    @Test
    public void should_call_removeById_on_all_repos_even_if_one_fails() {
        // Given
        AggregatedRepository repo1 = mock(AggregatedRepository.class);
        AggregatedRepository repo2 = mock(AggregatedRepository.class);

        when(repo1.findById(any())).thenThrow(RuntimeException.class);

        TestCaseRepositoryAggregator sut = new TestCaseRepositoryAggregator(List.of(repo1, repo2));

        final String scenarioId = "12345";

        // When
        sut.removeById(scenarioId);

        // Then
        verify(repo1).removeById(scenarioId);
        verify(repo2).removeById(scenarioId);
    }

    @Test
    public void should_aggregate_all_repos_scenarios_when_findAll() {
        // Given
        DatabaseTestCaseRepository repo1 = mock(DatabaseTestCaseRepository.class);

        when(repo1.findAll()).thenReturn(asList(mock(TestCaseMetadata.class), mock(TestCaseMetadata.class)));
        when(composableTestCaseRepository.findAll()).thenReturn(asList(mock(TestCaseMetadata.class), mock(TestCaseMetadata.class)));

        List<AggregatedRepository<? extends TestCase>> repos = List.of(repo1, composableTestCaseRepository);
        TestCaseRepositoryAggregator sut = new TestCaseRepositoryAggregator(repos);

        // When
        final List<TestCaseMetadata> allScenario = sut.findAll();

        // Then
        assertThat(allScenario).hasSize(4);
    }

    @Test
    public void should_aggregate_all_repos_available_scenarios_when_findAll_with_one_repo_failed() {
        // Given
        DatabaseTestCaseRepository repo1 = mock(DatabaseTestCaseRepository.class);

        when(repo1.findAll()).thenReturn(asList(mock(TestCaseMetadata.class), mock(TestCaseMetadata.class)));
        when(composableTestCaseRepository.findAll()).thenThrow(new RuntimeException("Error searching for scenarios !!!"));

        List<AggregatedRepository<? extends TestCase>> repos = List.of(repo1, composableTestCaseRepository);
        TestCaseRepositoryAggregator sut = new TestCaseRepositoryAggregator(repos);

        // When
        final List<TestCaseMetadata> allScenario = sut.findAll();

        // Then
        assertThat(allScenario).hasSize(2);
    }

    @Test
    public void should_aggregate_all_repos_scenarios_when_search() {
        // Given
        final String filter = "filter";
        DatabaseTestCaseRepository repo1 = mock(DatabaseTestCaseRepository.class);

        when(repo1.search(filter)).thenReturn(asList(mock(TestCaseMetadata.class), mock(TestCaseMetadata.class)));
        when(composableTestCaseRepository.search(filter)).thenReturn(asList(mock(TestCaseMetadata.class), mock(TestCaseMetadata.class)));

        List<AggregatedRepository<? extends TestCase>> repos = List.of(repo1, composableTestCaseRepository);
        TestCaseRepositoryAggregator sut = new TestCaseRepositoryAggregator(repos);

        // When
        final List<TestCaseMetadata> allScenario = sut.search(filter);

        // Then
        assertThat(allScenario).hasSize(4);
    }

    @Test
    public void should_aggregate_all_repos_available_scenarios_when_search_with_one_repo_failed() {
        // Given
        String filter = "filter";
        DatabaseTestCaseRepository repo1 = mock(DatabaseTestCaseRepository.class);

        when(repo1.search(filter)).thenThrow(new RuntimeException("Error searching for scenarios !!!"));
        when(composableTestCaseRepository.search(filter)).thenReturn(asList(mock(TestCaseMetadata.class), mock(TestCaseMetadata.class)));

        List<AggregatedRepository<? extends TestCase>> repos = List.of(repo1, composableTestCaseRepository);
        TestCaseRepositoryAggregator sut = new TestCaseRepositoryAggregator(repos);

        // When
        final List<TestCaseMetadata> allScenario = sut.search(filter);

        // Then
        assertThat(allScenario).hasSize(2);
    }

    private GwtTestCase defaultScenarioWithRepoSource(String repositorySource) {
        return GwtTestCase.builder()
            .withMetadata(TestCaseMetadataImpl.builder()
                .withCreationDate(Instant.now())
                .build())
            .withScenario(GwtScenario.builder().withWhen(GwtStep.NONE).build())
            .build();
    }
}
