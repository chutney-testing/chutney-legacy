/*
 * Copyright 2017-2023 Enedis
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.chutneytesting.engine.api.execution;

import io.reactivex.rxjava3.core.Observable;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * Exposes Scenario execution HTTP service.
 * Used by agents only for the moment
 */
@RestController
@CrossOrigin(origins = "*")
public class HttpTestEngine implements TestEngine {

    public static final String EXECUTION_URL = "/api/scenario/execution/v1";

    private final TestEngine testEngine;

    public HttpTestEngine(TestEngine embeddedTestEngine) {
        this.testEngine = embeddedTestEngine;
    }

    @Override
    @PreAuthorize("hasAuthority('SCENARIO_EXECUTE')")
    @PostMapping(path = EXECUTION_URL, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public StepExecutionReportDto execute(@RequestBody ExecutionRequestDto request) {
        return testEngine.execute(request);
    }

    @Override
    public Long executeAsync(ExecutionRequestDto request) {
        throw new IllegalArgumentException();
    }

    @Override
    public Observable<StepExecutionReportDto> receiveNotification(Long executionId) {
        throw new IllegalArgumentException();
    }

    @Override
    public void pauseExecution(Long executionId) {
        throw new IllegalArgumentException();
    }

    @Override
    public void resumeExecution(Long executionId) {
        throw new IllegalArgumentException();
    }

    @Override
    public void stopExecution(Long executionId) {
        throw new IllegalArgumentException();
    }

    @Override
    public void close() throws Exception {
        testEngine.close();
    }
}
