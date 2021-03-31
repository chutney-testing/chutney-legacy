package com.chutneytesting.junit.engine;

import com.chutneytesting.ExecutionConfiguration;
import com.chutneytesting.engine.api.execution.StepDefinitionDto;
import com.chutneytesting.engine.api.execution.StepExecutionReportDto;
import com.chutneytesting.glacio.api.ExecutionRequestMapper;
import io.reactivex.Observable;
import org.junit.platform.engine.support.hierarchical.EngineExecutionContext;

public class ChutneyEngineExecutionContext implements EngineExecutionContext {

    private final ExecutionConfiguration executionConfiguration;

    protected ChutneyEngineExecutionContext(ExecutionConfiguration executionConfiguration) {
        this.executionConfiguration = executionConfiguration;
    }

    protected Observable<StepExecutionReportDto> executeScenario(StepDefinitionDto stepDefinitionDto) {
        Long executionId = executionConfiguration.embeddedTestEngine().executeAsync(ExecutionRequestMapper.toDto(stepDefinitionDto));
        return executionConfiguration.embeddedTestEngine().receiveNotification(executionId);
    }

}
