package com.chutneytesting.scenario.domain;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.stereotype.Repository;

@Repository
public class TestCaseRepositoryAggregator implements TestCaseRepository {

    private final List<AggregatedRepository> aggregatedRepositories;

    public TestCaseRepositoryAggregator(List<AggregatedRepository> aggregatedRepositories) {
        this.aggregatedRepositories = aggregatedRepositories;
    }

    @Override
    public Optional<TestCase> findById(String testCaseId) {
        return aggregatedRepositories
            .stream()
            .map(repo -> repo.findById(testCaseId))
            .filter(Optional::isPresent)
            .map(Optional::get)
            .map(opt -> (TestCase) opt)// TODO why java is so ugly ? :-(
            .findFirst();
    }

    @Override
    public Optional<TestCaseMetadata> findMetadataById(String testCaseId) {
        Optional<TestCase> testCase = findById(testCaseId);
        return Optional.ofNullable(testCase.map(TestCase::metadata).orElse(null));
    }

    @Override
    public List<TestCaseMetadata> findAll() {
        return (List<TestCaseMetadata>) aggregatedRepositories
            .stream()
            .parallel()
            .flatMap(repo -> repo.findAll().stream())
            .collect(Collectors.toList());
    }

    @Override
    public void removeById(String testCaseId) {
        Optional<AggregatedRepository> repository = aggregatedRepositories
            .stream()
            .filter(repo -> repo.findById(testCaseId).isPresent())
            .findFirst();
        if (repository.isPresent()) {
            repository.get().removeById(testCaseId);
        } else {
            throw new ScenarioNotFoundException(testCaseId);
        }
    }

    @Override
    public Integer lastVersion(String testCaseId) {
        Optional<AggregatedRepository> repository = aggregatedRepositories
            .stream()
            .filter(repo -> repo.findById(testCaseId).isPresent())
            .findFirst();
        if (repository.isPresent()) {
            return (Integer) repository.get().lastVersion(testCaseId).get();//TODO ugly
        } else {
            throw new ScenarioNotFoundException(testCaseId);
        }
    }

    @Override
    public List<TestCaseMetadata> search(String textFilter) {
        return (List<TestCaseMetadata>) aggregatedRepositories.stream()
            .parallel()
            .flatMap(r -> r.search(textFilter).stream())
            .collect(Collectors.toList());
    }
}
