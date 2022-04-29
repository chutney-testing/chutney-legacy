package com.chutneytesting.junit.engine;

import com.chutneytesting.ExecutionConfiguration;
import com.chutneytesting.engine.api.execution.StepDefinitionDto;
import com.chutneytesting.engine.api.execution.StepExecutionReportDto;
import com.chutneytesting.glacio.api.ExecutionRequestMapper;
import io.reactivex.Observable;
import org.junit.platform.engine.support.hierarchical.EngineExecutionContext;

public class ChutneyEngineExecutionContext implements EngineExecutionContext {

    private final ExecutionConfiguration executionConfiguration;
    private final String environmentName;

    protected ChutneyEngineExecutionContext(ExecutionConfiguration executionConfiguration, String environmentName) {
        this.executionConfiguration = executionConfiguration;
        this.environmentName = environmentName;
    }

    protected Observable<StepExecutionReportDto> executeScenario(StepDefinitionDto stepDefinitionDto) {
        Long executionId = executionConfiguration.embeddedTestEngine().executeAsync(ExecutionRequestMapper.toDto(stepDefinitionDto, environmentName));
        return executionConfiguration.embeddedTestEngine().receiveNotification(executionId);
    }

}
