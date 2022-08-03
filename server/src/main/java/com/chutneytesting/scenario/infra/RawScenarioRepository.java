package com.chutneytesting.scenario.infra;


import com.chutneytesting.scenario.domain.gwt.GwtTestCase;
import com.chutneytesting.scenario.domain.raw.RawTestCase;
import com.chutneytesting.server.core.scenario.AggregatedRepository;

public interface RawScenarioRepository extends AggregatedRepository<RawTestCase> {

    /**
     * @return the id of the scenario
     */
    String save(GwtTestCase scenario);

}
