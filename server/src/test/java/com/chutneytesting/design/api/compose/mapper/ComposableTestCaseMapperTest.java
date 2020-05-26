package com.chutneytesting.design.api.compose.mapper;

import static com.chutneytesting.design.api.compose.mapper.ComposableTestCaseMapper.fromDto;
import static com.chutneytesting.design.api.compose.mapper.ComposableTestCaseMapper.fromFrontId;
import static com.chutneytesting.design.api.compose.mapper.ComposableTestCaseMapper.toDto;
import static com.chutneytesting.design.api.compose.mapper.ComposableTestCaseMapper.toFrontId;
import static org.assertj.core.api.Assertions.assertThat;

import com.chutneytesting.design.api.compose.dto.ComposableTestCaseDto;
import com.chutneytesting.design.api.compose.dto.FunctionalStepDto;
import com.chutneytesting.design.api.compose.dto.ImmutableComposableScenarioDto;
import com.chutneytesting.design.api.compose.dto.ImmutableComposableTestCaseDto;
import com.chutneytesting.design.api.compose.dto.ImmutableFunctionalStepDto;
import com.chutneytesting.design.api.compose.dto.ImmutableKeyValue;
import com.chutneytesting.design.api.compose.dto.KeyValue;
import com.chutneytesting.design.domain.compose.ComposableScenario;
import com.chutneytesting.design.domain.compose.ComposableTestCase;
import com.chutneytesting.design.domain.compose.FunctionalStep;
import com.chutneytesting.design.domain.compose.StepUsage;
import com.chutneytesting.design.domain.scenario.TestCaseMetadataImpl;
import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;
import org.apache.groovy.util.Maps;
import org.junit.jupiter.api.Test;

public class ComposableTestCaseMapperTest {

    private final String DEFAULT_COMPOSABLE_TESTCASE_DB_ID = "#30:1";
    private final String DEFAULT_COMPOSABLE_TESTCASE_ID = "30-1";

    private final ComposableTestCaseDto composableTestCaseDto =
        ImmutableComposableTestCaseDto.builder()
            .id(DEFAULT_COMPOSABLE_TESTCASE_ID)
            .title("Default title")
            .description("Default description")
            .creationDate(Instant.MIN)
            .addTags("tag1", "tag2")
            .scenario(
                ImmutableComposableScenarioDto.builder()
                    .componentSteps(Collections.singletonList(
                        ImmutableFunctionalStepDto.builder()
                            .id("30-10")
                            .name("First default functional ref")
                            .task("{ \"type\": \"default-identifier\" }")
                            .parameters(Arrays.asList(
                                ImmutableKeyValue.builder().key("key valued").value("value").build(),
                                ImmutableKeyValue.builder().key("empty key").value("").build()))
                            .dataSet(
                                KeyValue.fromMap(
                                    Maps.of(
                                        "key valued", "value",
                                        "empty key", ""
                                    )
                                )
                            )
                            .usage(FunctionalStepDto.StepUsage.STEP)
                            .build()))
                    .parameters(Arrays.asList(
                        ImmutableKeyValue.builder().key("scenario key valued").value("scenario value").build(),
                        ImmutableKeyValue.builder().key("scenario empty key").build()
                    ))
                    .build()
            )
            .dataSet(
                KeyValue.fromMap(
                    Maps.of(
                        "empty key", "",
                        "scenario key valued", "scenario value",
                        "scenario empty key", ""
                    )
                )
            )
            .build();

    private final ComposableTestCase composableTestCase =
        new ComposableTestCase(
            DEFAULT_COMPOSABLE_TESTCASE_DB_ID,
            TestCaseMetadataImpl.builder()
                .withTitle("Default title")
                .withDescription("Default description")
                .withCreationDate(Instant.MIN)
                .withTags(Arrays.asList("tag1","tag2"))
                .withRepositorySource("ComposableTestCase")
                .build(),
            ComposableScenario.builder()
                .withFunctionalSteps(
                    Collections.singletonList(
                        FunctionalStep.builder()
                            .withId("#30:10")
                            .withName("First default functional ref")
                            .withImplementation(Optional.of("{ \"type\": \"default-identifier\" }"))
                            .withParameters(
                                Maps.of(
                                    "key valued", "value",
                                    "empty key", ""
                                )
                            )
                            .withUsage(Optional.of(StepUsage.STEP))
                            .overrideDataSetWith(
                                Maps.of(
                                    "key valued", "value",
                                    "empty key", ""
                                )
                            )
                            .build()
                    )
                )
                .withParameters(
                    Maps.of(
                        "scenario key valued", "scenario value",
                        "scenario empty key", ""
                    )
                )
                .build()
        );

    @Test
    public void should_map_from_dto() {
        // When
        final ComposableTestCase ctc = fromDto(composableTestCaseDto);

        // Then
        assertThat(ctc)
            .usingRecursiveComparison()
            .ignoringFieldsMatchingRegexes("metadata")
            .isEqualTo(composableTestCase);
        assertThat(ctc.metadata)
            .usingRecursiveComparison()
            .ignoringFieldsMatchingRegexes("tags")
            .isEqualTo(composableTestCase.metadata);
        assertThat(ctc.metadata.tags()).containsExactly("TAG1", "TAG2");
    }

    @Test
    public void should_map_to_dto() {
        // When
        final ComposableTestCaseDto ctcd = toDto(composableTestCase);

        // Then
        assertThat(ctcd)
            .usingRecursiveComparison()
            .ignoringFieldsMatchingRegexes("tags")
            .isEqualTo(composableTestCaseDto);
        assertThat(ctcd.tags()).containsExactly("TAG1", "TAG2");
    }

    @Test
    public void should_map_to_front_id() {
        assertThat(toFrontId(DEFAULT_COMPOSABLE_TESTCASE_DB_ID)).isEqualTo(DEFAULT_COMPOSABLE_TESTCASE_ID);
    }

    @Test
    public void should_map_from_front_id() {
        assertThat(fromFrontId(Optional.of(DEFAULT_COMPOSABLE_TESTCASE_ID))).isEqualTo(DEFAULT_COMPOSABLE_TESTCASE_DB_ID);
    }

    @Test
    public void should_map_empty_from_front_id() {
        assertThat(fromFrontId(Optional.empty())).isEqualTo("");
    }
}
