package com.chutneytesting.server.core.domain.execution;

import com.chutneytesting.engine.api.execution.ExecutionRequestDto;

public interface ExecutionRequestMapper {

    ExecutionRequestDto toDto(ExecutionRequest executionRequest);

}
