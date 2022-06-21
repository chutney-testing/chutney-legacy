package com.chutneytesting.scenario.infra;


import com.chutneytesting.scenario.domain.TestCase;
import com.chutneytesting.scenario.domain.TestCaseMetadata;
import java.util.List;
import java.util.Optional;

public interface RawScenarioRepository {

    String alias();

    /**
     * @return the id of the scenario
     */
    String save(TestCase scenario);

    Optional<TestCase> findById(String scenarioId);

    List<TestCaseMetadata> findAll();

    void removeById(String scenarioId);

    Optional<Integer> lastVersion(String scenarioId);

    List<TestCaseMetadata> search(String textFilter);
}
