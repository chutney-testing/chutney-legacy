package com.chutneytesting.scenario.domain;

import java.util.List;
import java.util.Optional;

public interface TestCaseRepository {

    Optional<TestCase> findById(String testCaseId);

    Optional<TestCaseMetadata> findMetadataById(String testCaseId);

    List<TestCaseMetadata> findAll();

    void removeById(String testCaseId);

    Integer lastVersion(String testCaseId); // not used

    List<TestCaseMetadata> search(String textFilter);
}
