package com.chutneytesting.execution.domain.compiler;

import com.chutneytesting.design.domain.dataset.DataSetRepository;
import com.chutneytesting.design.domain.globalvar.GlobalvarRepository;
import com.chutneytesting.execution.domain.ExecutionRequest;
import com.chutneytesting.execution.domain.scenario.composed.ExecutableComposedTestCase;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

@Component
public class ComposedTestCasePreProcessor implements TestCasePreProcessor<ExecutableComposedTestCase> {

    private final ComposedTestCaseParametersResolutionPreProcessor parametersResolutionPreProcessor;
    private final ComposedTestCaseLoopPreProcessor loopPreProcessor;
    private final ComposedTestCaseDataSetPreProcessor dataSetPreProcessor;

    public ComposedTestCasePreProcessor(ObjectMapper objectMapper, GlobalvarRepository globalvarRepository, DataSetRepository dataSetRepository) {
        this.parametersResolutionPreProcessor = new ComposedTestCaseParametersResolutionPreProcessor(globalvarRepository, objectMapper);
        this.loopPreProcessor = new ComposedTestCaseLoopPreProcessor(objectMapper);
        this.dataSetPreProcessor = new ComposedTestCaseDataSetPreProcessor(dataSetRepository);
    }

    @Override
    public ExecutableComposedTestCase apply(ExecutionRequest executionRequest) {
        String environment = executionRequest.environment;
        String userId = executionRequest.userId;

        // Process scenario default dataset if requested
        ExecutableComposedTestCase testCase = (ExecutableComposedTestCase) executionRequest.testCase;
        if (executionRequest.withScenarioDefaultDataSet) {
            testCase = dataSetPreProcessor.apply(
                new ExecutionRequest(testCase, environment, userId)
            );
        }
        // Process loop strategy
        testCase = loopPreProcessor.apply(
            new ExecutionRequest(
                parametersResolutionPreProcessor.applyOnStrategy(testCase, environment),
                environment,
                userId
            )
        );
        // Process parameters (value them)
        return parametersResolutionPreProcessor.apply(
            new ExecutionRequest(testCase, environment, userId)
        );
    }
}
