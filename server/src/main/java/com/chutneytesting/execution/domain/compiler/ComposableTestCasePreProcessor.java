package com.chutneytesting.execution.domain.compiler;

import com.chutneytesting.design.domain.compose.ComposableTestCase;
import com.chutneytesting.design.domain.dataset.DataSetRepository;
import com.chutneytesting.design.domain.globalvar.GlobalvarRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

@Component
public class ComposableTestCasePreProcessor implements TestCasePreProcessor<ComposableTestCase> {

    private final ComposableTestCaseParametersResolutionPreProcessor parametersResolutionPreProcessor;
    private final ComposableTestCaseLoopPreProcessor loopPreProcessor;
    private final ComposableTestCaseDataSetPreProcessor dataSetPreProcessor;

    public ComposableTestCasePreProcessor(ObjectMapper objectMapper, GlobalvarRepository globalvarRepository, DataSetRepository dataSetRepository) {
        this.parametersResolutionPreProcessor = new ComposableTestCaseParametersResolutionPreProcessor(globalvarRepository);
        this.loopPreProcessor = new ComposableTestCaseLoopPreProcessor(objectMapper);
        this.dataSetPreProcessor = new ComposableTestCaseDataSetPreProcessor(dataSetRepository);
    }

    @Override
    public ComposableTestCase apply(ComposableTestCase testCase, String environment) {
        ComposableTestCase dataSetProcessedTestCase = dataSetPreProcessor.apply(testCase, environment);
        ComposableTestCase loopStrategyProcessedTestCase = loopPreProcessor.apply(parametersResolutionPreProcessor.applyOnStrategy(dataSetProcessedTestCase, environment), environment);
        return parametersResolutionPreProcessor.apply(loopStrategyProcessedTestCase, environment);
    }
}
