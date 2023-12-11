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

package com.chutneytesting.execution.domain;

import com.chutneytesting.scenario.domain.raw.RawTestCase;
import com.chutneytesting.server.core.domain.execution.ExecutionRequest;
import com.chutneytesting.server.core.domain.execution.processor.TestCasePreProcessor;
import com.chutneytesting.server.core.domain.globalvar.GlobalvarRepository;
import org.apache.commons.text.StringEscapeUtils;
import org.springframework.stereotype.Component;

@Component
public class RawDataSetPreProcessor implements TestCasePreProcessor<RawTestCase> {

    private final GlobalvarRepository globalvarRepository;

    public RawDataSetPreProcessor(GlobalvarRepository globalvarRepository) {
        this.globalvarRepository = globalvarRepository;
    }

    @Override
    public RawTestCase apply(ExecutionRequest executionRequest) {
        RawTestCase testCase = (RawTestCase) executionRequest.testCase;
        return RawTestCase.builder()
            .withMetadata(testCase.metadata)
            .withScenario(replaceParams(globalvarRepository.getFlatMap(), testCase.scenario, StringEscapeUtils::escapeJson))
            .build();
    }
}
