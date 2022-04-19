package com.chutneytesting.execution.domain.compiler;

import com.chutneytesting.scenario.domain.gwt.GwtScenario;

public interface GwtScenarioMarshaller {

    String serialize(GwtScenario scenario);

    GwtScenario deserialize(String title, String description, String blob);

}
