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

import com.chutneytesting.scenario.api.raw.dto.GwtTestCaseDto;
import com.chutneytesting.scenario.api.raw.dto.ImmutableGwtTestCaseDto;
import com.chutneytesting.scenario.domain.gwt.GwtTestCase;
import com.chutneytesting.scenario.domain.raw.RawTestCase;
import com.chutneytesting.server.core.domain.scenario.TestCase;
import com.chutneytesting.server.core.domain.scenario.TestCaseMetadataImpl;
import java.util.Collections;

// TODO test me
public class GwtTestCaseMapper {

    // DTO -> TestCase
    public static GwtTestCase fromDto(GwtTestCaseDto dto) {
        return GwtTestCase.builder()
            .withMetadata(TestCaseMetadataImpl.builder()
                .withId(dto.id().orElse(null))
                .withTitle(dto.title())
                .withDescription(dto.description().orElse(null))
                .withTags(dto.tags())
                .withCreationDate(dto.creationDate().orElse(null))
                .withAuthor(dto.author())
                .withUpdateDate(dto.updateDate())
                .withVersion(dto.version())
                .withDefaultDataset(dto.defaultDataset().orElse(null))
                .build())
            .withScenario(GwtScenarioMapper.fromDto(dto.title(), dto.description().orElse(""), dto.scenario()))
            .build();
    }

    // TestCase -> DTO
    public static GwtTestCaseDto toDto(TestCase testCase) {
        if (testCase instanceof GwtTestCase) {
            return fromGwt((GwtTestCase) testCase);
        }

        if (testCase instanceof RawTestCase) {
            return fromGwt(RawTestCaseMapper.fromRaw((RawTestCase) testCase));
        }

        throw new IllegalStateException("Bad format. " +
            "Test Case [" + testCase.metadata().id() + "] is not a GwtTestCase but a " + testCase.getClass().getSimpleName());
    }

    private static GwtTestCaseDto fromGwt(GwtTestCase testCase) {
        return ImmutableGwtTestCaseDto.builder()
            .id(testCase.metadata().id())
            .title(testCase.metadata().title())
            .description(testCase.metadata().description())
            .tags(testCase.metadata().tags())
            .executions(Collections.emptyList())
            .creationDate(testCase.metadata().creationDate())
            .scenario(GwtScenarioMapper.toDto(testCase.scenario))
            .author(testCase.metadata.author)
            .updateDate(testCase.metadata.updateDate)
            .version(testCase.metadata.version)
            .defaultDataset(testCase.metadata.defaultDataset)
            .build();
    }
}
