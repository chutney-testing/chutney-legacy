package com.chutneytesting.server.core.domain.scenario;

import java.util.List;
import java.util.Optional;

public interface AggregatedRepository<T extends TestCase> {

    String save(T scenario);

    Optional<T> findById(String testCaseId);

    Optional<TestCaseMetadata> findMetadataById(String testCaseId);

    List<TestCaseMetadata> findAll();

    void removeById(String testCaseId);

    Optional<Integer> lastVersion(String testCaseId);

    List<TestCaseMetadata> search(String textFilter);

}
