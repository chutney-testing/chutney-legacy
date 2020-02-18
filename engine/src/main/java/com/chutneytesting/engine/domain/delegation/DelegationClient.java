package com.chutneytesting.engine.domain.delegation;


import com.chutneytesting.engine.domain.execution.StepDefinition;
import com.chutneytesting.engine.domain.execution.report.StepExecutionReport;

public interface DelegationClient {

    StepExecutionReport handDown(StepDefinition stepDefinition, NamedHostAndPort delegate) throws CannotDelegateException;

}
