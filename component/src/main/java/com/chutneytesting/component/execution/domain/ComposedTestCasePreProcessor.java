package com.chutneytesting.component.execution.domain;

import com.chutneytesting.component.dataset.domain.DataSetRepository;
import com.chutneytesting.server.core.execution.ExecutionRequest;
import com.chutneytesting.server.core.execution.processor.TestCasePreProcessor;
import com.chutneytesting.server.core.globalvar.GlobalvarRepository;
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
        if (testCase.metadata.datasetId().isPresent()) {
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
