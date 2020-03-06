package com.chutneytesting.execution.domain.compiler;

import com.chutneytesting.design.domain.globalvar.GlobalvarRepository;
import com.chutneytesting.design.domain.scenario.raw.RawTestCase;
import org.apache.commons.text.StringEscapeUtils;
import org.springframework.stereotype.Component;

@Component
public class RawDataSetPreProcessor implements TestCasePreProcessor<RawTestCase> {

    private final GlobalvarRepository globalvarRepository;

    public RawDataSetPreProcessor(GlobalvarRepository globalvarRepository) {
        this.globalvarRepository = globalvarRepository;
    }

    @Override
    public int order() {
        return 0;
    }

    @Override
    public RawTestCase apply(RawTestCase testCase) {
        return RawTestCase.builder()
            .withMetadata(testCase.metadata)
            .withScenario(replaceParams(testCase.content, globalvarRepository.getFlatMap(), testCase.dataSet(), StringEscapeUtils::escapeJson))
            .build();
    }

}
