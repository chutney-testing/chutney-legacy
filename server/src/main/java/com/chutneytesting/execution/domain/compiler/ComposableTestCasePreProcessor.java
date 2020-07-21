package com.chutneytesting.execution.domain.compiler;

import com.chutneytesting.design.domain.compose.ComposableTestCase;
import com.chutneytesting.design.domain.dataset.DataSetRepository;
import com.chutneytesting.design.domain.globalvar.GlobalvarRepository;
import com.chutneytesting.execution.domain.ExecutionRequest;
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
    public ComposableTestCase apply(ExecutionRequest executionRequest) {
        String environment = executionRequest.environment;

        // Process scenario default dataset if requested
        ComposableTestCase testCase = (ComposableTestCase) executionRequest.testCase;
        if (executionRequest.withScenarioDefaultDataSet) {
            testCase = dataSetPreProcessor.apply(
                new ExecutionRequest(testCase, environment)
            );
        }
        // Process loop strategy
        testCase = loopPreProcessor.apply(
            new ExecutionRequest(
                parametersResolutionPreProcessor.applyOnStrategy(testCase, environment),
                environment
            )
        );
        // Process parameters (value them)
        return parametersResolutionPreProcessor.apply(
            new ExecutionRequest(testCase, environment)
        );
    }
}
