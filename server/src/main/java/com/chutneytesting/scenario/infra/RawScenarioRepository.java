package com.chutneytesting.scenario.infra;


import com.chutneytesting.scenario.domain.AggregatedRepository;
import com.chutneytesting.scenario.domain.TestCase;
import com.chutneytesting.scenario.domain.TestCaseMetadata;
import com.chutneytesting.scenario.domain.gwt.GwtTestCase;
import java.util.List;
import java.util.Optional;

public interface RawScenarioRepository extends AggregatedRepository {

    /**
     * @return the id of the scenario
     */
    String save(GwtTestCase scenario);

}
