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
    private final ComposedTestCaseDatatableIterationsPreProcessor dataSetPreProcessor;

    public ComposedTestCasePreProcessor(ObjectMapper objectMapper, GlobalvarRepository globalvarRepository, DataSetRepository dataSetRepository) {
        this.parametersResolutionPreProcessor = new ComposedTestCaseParametersResolutionPreProcessor(globalvarRepository, objectMapper);
        this.dataSetPreProcessor = new ComposedTestCaseDatatableIterationsPreProcessor(dataSetRepository);
    }

    @Override
    public ExecutableComposedTestCase apply(ExecutionRequest executionRequest) {
        String environment = executionRequest.environment;
        String userId = executionRequest.userId;

        // Process scenario default dataset if requested
        ExecutableComposedTestCase testCase = (ExecutableComposedTestCase) executionRequest.testCase;
        if (executionRequest.withExternalDataset) {
            testCase = dataSetPreProcessor.apply(
                new ExecutionRequest(testCase, environment, userId)
            );
        }

        // Process parameters (value them)
        return parametersResolutionPreProcessor.apply(
            new ExecutionRequest(testCase, environment, userId)
        );
    }
}
