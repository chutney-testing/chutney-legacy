package com.chutneytesting.execution.domain.compiler;

import com.chutneytesting.design.domain.globalvar.GlobalvarRepository;
import com.chutneytesting.design.domain.scenario.gwt.GwtScenario;
import com.chutneytesting.design.domain.scenario.gwt.GwtTestCase;
import com.chutneytesting.execution.domain.ExecutionRequest;
import java.util.Map;
import org.apache.commons.text.StringEscapeUtils;
import org.springframework.stereotype.Component;

@Component
public class GwtDataSetPreProcessor implements TestCasePreProcessor<GwtTestCase> {

    private final GlobalvarRepository globalvarRepository;
    private final GwtScenarioMarshaller marshaller;

    public GwtDataSetPreProcessor(GwtScenarioMarshaller marshaller, GlobalvarRepository globalvarRepository) {
        this.marshaller = marshaller;
        this.globalvarRepository = globalvarRepository;
    }

    @Override
    public GwtTestCase apply(ExecutionRequest executionRequest) {
        GwtTestCase testCase = (GwtTestCase) executionRequest.testCase;
        return GwtTestCase.builder()
            .withMetadata(testCase.metadata)
            .withParameters(testCase.parameters)
            .withScenario(replaceParams(testCase.scenario, testCase.parameters))
            .build();
    }

    private GwtScenario replaceParams(GwtScenario scenario, Map<String, String> dataSet) {
        String blob = marshaller.serialize(scenario);
        return marshaller.deserialize(scenario.title, scenario.description, replaceParams(blob, globalvarRepository.getFlatMap(), dataSet, StringEscapeUtils::escapeJson));
    }

}
