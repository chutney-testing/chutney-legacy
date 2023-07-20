package com.chutneytesting.scenario.infra;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.chutneytesting.scenario.infra.raw.DatabaseTestCaseRepository;
import com.chutneytesting.server.core.domain.scenario.AggregatedRepository;
import com.chutneytesting.server.core.domain.scenario.TestCase;
import com.chutneytesting.server.core.domain.scenario.TestCaseMetadata;
import java.util.List;
import java.util.Optional;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

public class TestCaseRepositoryAggregatorTest {

    @Test
    public void should_not_support_save_operation() {
        // Given
        TestCaseRepositoryAggregator sut = new TestCaseRepositoryAggregator(emptyList());

        // When
        Throwable exception = Assertions.catchThrowable(() -> sut.save(mock(TestCase.class)));

        // Then
        assertThat(exception).isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    public void should_call_findById_on_all_repos_even_if_one_fails() {
        // Given
        AggregatedRepository<? extends TestCase> repo1 = (AggregatedRepository<? extends TestCase>) mock(AggregatedRepository.class);
        AggregatedRepository<? extends TestCase> repo2 = (AggregatedRepository<? extends TestCase>) mock(AggregatedRepository.class);

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
        AggregatedRepository<? extends TestCase> repo1 = (AggregatedRepository<? extends TestCase>) mock(AggregatedRepository.class);
        AggregatedRepository<? extends TestCase> repo2 = (AggregatedRepository<? extends TestCase>) mock(AggregatedRepository.class);

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
        final AggregatedRepository<? extends TestCase> externalTestCaseRepository = (AggregatedRepository<? extends TestCase>) mock(AggregatedRepository.class);

        when(repo1.findAll()).thenReturn(asList(mock(TestCaseMetadata.class), mock(TestCaseMetadata.class)));
        when(externalTestCaseRepository.findAll()).thenReturn(asList(mock(TestCaseMetadata.class), mock(TestCaseMetadata.class)));

        List<AggregatedRepository<? extends TestCase>> repos = List.of(repo1, externalTestCaseRepository);
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
        final AggregatedRepository<? extends TestCase> externalTestCaseRepository = (AggregatedRepository<? extends TestCase>) mock(AggregatedRepository.class);

        when(repo1.findAll()).thenReturn(asList(mock(TestCaseMetadata.class), mock(TestCaseMetadata.class)));
        when(externalTestCaseRepository.findAll()).thenThrow(new RuntimeException("Error searching for scenarios !!!"));

        List<AggregatedRepository<? extends TestCase>> repos = List.of(repo1, externalTestCaseRepository);
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
        final AggregatedRepository<? extends TestCase> externalTestCaseRepository = (AggregatedRepository<? extends TestCase>) mock(AggregatedRepository.class);

        when(repo1.search(filter)).thenReturn(asList(mock(TestCaseMetadata.class), mock(TestCaseMetadata.class)));
        when(externalTestCaseRepository.search(filter)).thenReturn(asList(mock(TestCaseMetadata.class), mock(TestCaseMetadata.class)));

        List<AggregatedRepository<? extends TestCase>> repos = List.of(repo1, externalTestCaseRepository);
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
        final AggregatedRepository<? extends TestCase> externalTestCaseRepository = (AggregatedRepository<? extends TestCase>) mock(AggregatedRepository.class);

        when(repo1.search(filter)).thenThrow(new RuntimeException("Error searching for scenarios !!!"));
        when(externalTestCaseRepository.search(filter)).thenReturn(asList(mock(TestCaseMetadata.class), mock(TestCaseMetadata.class)));

        List<AggregatedRepository<? extends TestCase>> repos = List.of(repo1, externalTestCaseRepository);
        TestCaseRepositoryAggregator sut = new TestCaseRepositoryAggregator(repos);

        // When
        final List<TestCaseMetadata> allScenario = sut.search(filter);

        // Then
        assertThat(allScenario).hasSize(2);
    }
}
