package com.chutneytesting.scenario.infra;


import com.chutneytesting.scenario.domain.TestCaseMetadata;
import com.chutneytesting.scenario.infra.raw.TestCaseData;
import java.util.List;
import java.util.Optional;

public interface DelegateScenarioRepository {

    String alias();

    /**
     * @return the id of the scenario
     */
    String save(TestCaseData scenario);

    // infra object in domain
    Optional<TestCaseData> findById(String scenarioId);

    List<TestCaseMetadata> findAll();

    void removeById(String scenarioId);

    Optional<Integer> lastVersion(String scenarioId);

    List<TestCaseMetadata> search(String textFilter);
}
