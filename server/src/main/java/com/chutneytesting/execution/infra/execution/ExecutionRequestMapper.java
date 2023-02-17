package com.chutneytesting.execution.infra.execution;

import com.chutneytesting.engine.api.execution.ExecutionRequestDto;
import com.chutneytesting.server.core.domain.execution.ExecutionRequest;

public interface ExecutionRequestMapper {

    ExecutionRequestDto toDto(ExecutionRequest executionRequest);

}
