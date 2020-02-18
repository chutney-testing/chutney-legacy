package com.chutneytesting.execution.domain.compiler;

import com.chutneytesting.design.domain.scenario.gwt.GwtScenario;

public interface GwtScenarioMarshaller {

    String serialize(GwtScenario scenario);

    GwtScenario deserialize(String title, String description, String blob);

}
