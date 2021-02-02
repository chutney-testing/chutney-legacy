package com.chutneytesting.design.infra.storage.scenario;


import com.chutneytesting.design.domain.scenario.TestCaseMetadata;
import com.chutneytesting.design.infra.storage.scenario.jdbc.TestCaseData;
import java.util.List;
import java.util.Optional;

public interface DelegateScenarioRepository {

    String alias();

    /**
     * @return the id of the scenario
     */
    String save(TestCaseData scenario);

    Optional<TestCaseData> findById(String scenarioId);

    List<TestCaseMetadata> findAll();

    void removeById(String scenarioId);

    Optional<Integer> lastVersion(String scenarioId);
}
