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

package com.chutneytesting.scenario.api.raw.mapper;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.chutneytesting.scenario.api.raw.dto.ImmutableRawTestCaseDto;
import com.chutneytesting.server.core.domain.scenario.ScenarioNotParsableException;
import org.junit.jupiter.api.Test;

public class RawTestCaseMapperTest {

    private final ImmutableRawTestCaseDto invalid_dto = ImmutableRawTestCaseDto.builder()
        .title("Test mapping")
        .scenario(" I am invalid\n {").build();

    @Test
    public void should_fail_on_parse_error() {
        assertThatThrownBy(() -> RawTestCaseMapper.fromDto(invalid_dto))
            .isInstanceOf(ScenarioNotParsableException.class);
    }

}
