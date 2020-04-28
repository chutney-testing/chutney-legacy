package com.chutneytesting.execution.domain.compiler;

import com.chutneytesting.design.domain.compose.ComposableTestCase;
import com.chutneytesting.design.domain.globalvar.GlobalvarRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

@Component
public class ComposableTestCasePreProcessor implements TestCasePreProcessor<ComposableTestCase> {

    private final ComposableTestCaseDataSetPreProcessor dataSetPreProcessor;
    private final ComposableTestCaseLoopPreProcessor loopPreProcessor;

    public ComposableTestCasePreProcessor(ObjectMapper objectMapper, GlobalvarRepository globalvarRepository) {
        this.dataSetPreProcessor = new ComposableTestCaseDataSetPreProcessor(globalvarRepository);
        this.loopPreProcessor = new ComposableTestCaseLoopPreProcessor(objectMapper);
    }

    @Override
    public ComposableTestCase apply(ComposableTestCase testCase, String environment) {
        return dataSetPreProcessor.apply(loopPreProcessor.apply(dataSetPreProcessor.applyOnStrategy(testCase, environment)), environment);
    }
}
