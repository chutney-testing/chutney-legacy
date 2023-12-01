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

import static org.hjson.JsonValue.readHjson;

import com.chutneytesting.execution.domain.GwtScenarioMarshaller;
import com.chutneytesting.scenario.api.raw.dto.ImmutableRawTestCaseDto;
import com.chutneytesting.scenario.api.raw.dto.RawTestCaseDto;
import com.chutneytesting.scenario.domain.gwt.GwtScenario;
import com.chutneytesting.scenario.domain.gwt.GwtTestCase;
import com.chutneytesting.scenario.domain.raw.RawTestCase;
import com.chutneytesting.server.core.domain.scenario.ScenarioNotParsableException;
import com.chutneytesting.server.core.domain.scenario.TestCase;
import com.chutneytesting.server.core.domain.scenario.TestCaseMetadataImpl;
import com.chutneytesting.server.core.domain.tools.ui.KeyValue;
import org.hjson.Stringify;

public class RawTestCaseMapper {

    private static final GwtScenarioMarshaller marshaller = new GwtScenarioMapper();

    // RawTestCase -> GwtTestCase
    public static GwtTestCase fromRaw(RawTestCase testCase) {
        String jsonScenario = formatContentToJson(testCase.scenario);
        GwtScenario gwtScenario = marshaller.deserialize(testCase.metadata().title(), testCase.metadata().description(), jsonScenario);
        return GwtTestCase.builder()
            .withMetadata(TestCaseMetadataImpl.builder()
                .withId(testCase.metadata().id())
                .withTitle(testCase.metadata().title())
                .withDescription(testCase.metadata().description())
                .withTags(testCase.metadata().tags())
                .withCreationDate(testCase.metadata().creationDate())
                .withAuthor(testCase.metadata.author)
                .withUpdateDate(testCase.metadata.updateDate)
                .withVersion(testCase.metadata.version)
                .build())
            .withScenario(gwtScenario)
            .withExecutionParameters(testCase.executionParameters())
            .build();
    }

    // DTO -> RawTestCase
    public static GwtTestCase fromDto(RawTestCaseDto dto) {
        String jsonScenario = formatContentToJson(dto.scenario());
        GwtScenario gwtScenario = marshaller.deserialize(dto.title(), dto.description().orElse(""), jsonScenario);
        return GwtTestCase.builder()
            .withMetadata(TestCaseMetadataImpl.builder()
                .withId(dto.id().orElse(null))
                .withTitle(dto.title())
                .withDescription(dto.description().orElse(null))
                .withTags(dto.tags())
                .withCreationDate(dto.creationDate())
                .withAuthor(dto.author())
                .withUpdateDate(dto.updateDate())
                .withVersion(dto.version())
                .withDefaultDataset(dto.defaultDataset().orElse(null))
                .build())
            .withScenario(gwtScenario)
            .withExecutionParameters(KeyValue.toMap(dto.parameters()))
            .build();
    }

    private static String formatContentToJson(String content) {
        try {
            return readHjson(content).toString();
        } catch (Exception e) {
            throw new ScenarioNotParsableException("Malformed json or hjson format. ", e);
        }
    }

    public static RawTestCaseDto toDto(TestCase testCase) {
        if (testCase instanceof RawTestCase rawTestCase) {
            return toDto(rawTestCase);
        }

        if (testCase instanceof GwtTestCase gwtTestCase) {
            return toDto(gwtTestCase);
        }

        throw new IllegalStateException("Bad format." +
            "Test Case [" + testCase.metadata().id() + "] is not a RawTestCase but a " + testCase.getClass().getSimpleName());
    }

    public static RawTestCaseDto toDto(RawTestCase testCase) {
        return ImmutableRawTestCaseDto.builder()
            .id(testCase.metadata().id())
            .title(testCase.metadata().title())
            .description(testCase.metadata().description())
            .scenario(readHjson(testCase.scenario).toString(Stringify.HJSON))
            .tags(testCase.metadata().tags())
            .creationDate(testCase.metadata().creationDate())
            .parameters(KeyValue.fromMap(testCase.executionParameters()))
            .author(testCase.metadata.author)
            .updateDate(testCase.metadata.updateDate)
            .version(testCase.metadata.version)
            .defaultDataset(testCase.metadata.defaultDataset)
            .build();
    }

    public static RawTestCaseDto toDto(GwtTestCase testCase) {
        return ImmutableRawTestCaseDto.builder()
            .id(testCase.metadata().id())
            .title(testCase.metadata().title())
            .description(testCase.metadata().description())
            .scenario(readHjson(marshaller.serialize(testCase.scenario)).toString(Stringify.HJSON))
            .tags(testCase.metadata().tags())
            .creationDate(testCase.metadata().creationDate())
            .parameters(KeyValue.fromMap(testCase.executionParameters))
            .author(testCase.metadata.author)
            .updateDate(testCase.metadata.updateDate)
            .version(testCase.metadata.version)
            .defaultDataset(testCase.metadata.defaultDataset)
            .build();
    }

}
