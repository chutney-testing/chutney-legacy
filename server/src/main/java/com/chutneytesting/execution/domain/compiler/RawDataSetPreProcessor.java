package com.chutneytesting.execution.domain.compiler;

import com.chutneytesting.execution.domain.ExecutionRequest;
import com.chutneytesting.globalvar.domain.GlobalvarRepository;
import com.chutneytesting.scenario.domain.raw.RawTestCase;
import org.apache.commons.text.StringEscapeUtils;
import org.springframework.stereotype.Component;

@Component
public class RawDataSetPreProcessor implements TestCasePreProcessor<RawTestCase> {

    private final GlobalvarRepository globalvarRepository;

    public RawDataSetPreProcessor(GlobalvarRepository globalvarRepository) {
        this.globalvarRepository = globalvarRepository;
    }

    @Override
    public RawTestCase apply(ExecutionRequest executionRequest) {
        RawTestCase testCase = (RawTestCase) executionRequest.testCase;
        return RawTestCase.builder()
            .withMetadata(testCase.metadata)
            .withScenario(replaceParams(testCase.scenario, globalvarRepository.getFlatMap(), testCase.executionParameters(), StringEscapeUtils::escapeJson))
            .build();
    }
}
