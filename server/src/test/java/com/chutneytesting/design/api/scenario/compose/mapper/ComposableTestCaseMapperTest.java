package com.chutneytesting.design.api.scenario.compose.mapper;

import static com.chutneytesting.design.api.scenario.compose.mapper.ComposableTestCaseMapper.fromDto;
import static com.chutneytesting.design.api.scenario.compose.mapper.ComposableTestCaseMapper.toDto;
import static com.chutneytesting.tools.ui.ComposableIdUtils.fromFrontId;
import static com.chutneytesting.tools.ui.ComposableIdUtils.toFrontId;
import static org.assertj.core.api.Assertions.assertThat;

import com.chutneytesting.design.api.scenario.compose.dto.ComposableStepDto;
import com.chutneytesting.design.api.scenario.compose.dto.ComposableTestCaseDto;
import com.chutneytesting.design.api.scenario.compose.dto.ImmutableComposableScenarioDto;
import com.chutneytesting.design.api.scenario.compose.dto.ImmutableComposableTestCaseDto;
import com.chutneytesting.design.api.scenario.compose.dto.ImmutableComposableStepDto;
import com.chutneytesting.design.domain.scenario.TestCaseMetadataImpl;
import com.chutneytesting.design.domain.scenario.compose.ComposableScenario;
import com.chutneytesting.design.domain.scenario.compose.ComposableTestCase;
import com.chutneytesting.design.domain.scenario.compose.ComposableStep;
import com.chutneytesting.tools.ui.ImmutableKeyValue;
import com.chutneytesting.tools.ui.KeyValue;
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
                        ImmutableComposableStepDto.builder()
                            .id("30-10")
                            .name("First default functional ref")
                            .task("{ \"type\": \"default-identifier\" }")
                            .defaultParameters(Arrays.asList(
                                ImmutableKeyValue.builder().key("key valued").value("value").build(),
                                ImmutableKeyValue.builder().key("empty key").value("").build()))
                            .executionParameters(
                                KeyValue.fromMap(
                                    Maps.of(
                                        "key valued", "value",
                                        "empty key", ""
                                    )
                                )
                            )
                            .usage(ComposableStepDto.StepUsage.STEP)
                            .build()))
                    .parameters(Arrays.asList(
                        ImmutableKeyValue.builder().key("scenario key valued").value("scenario value").build(),
                        ImmutableKeyValue.builder().key("scenario empty key").build()
                    ))
                    .build()
            )
            .executionParameters(
                KeyValue.fromMap(
                    Maps.of(
                        "empty key", "",
                        "scenario key valued", "scenario value",
                        "scenario empty key", ""
                    )
                )
            )
            .datasetId("66-7")
            .author("author")
            .updateDate(Instant.MIN)
            .version(666)
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
                .withDatasetId("#66:7")
                .withAuthor("author")
                .withUpdateDate(Instant.MIN)
                .withVersion(666)
                .build(),
            ComposableScenario.builder()
                .withComposableSteps(
                    Collections.singletonList(
                        ComposableStep.builder()
                            .withId("#30:10")
                            .withName("First default functional ref")
                            .withImplementation("{ \"type\": \"default-identifier\" }")
                            .withDefaultParameters(
                                Maps.of(
                                    "key valued", "value",
                                    "empty key", ""
                                )
                            )
                            .withExecutionParameters(
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
