package com.chutneytesting.scenario.api;

import static org.assertj.core.api.Assertions.assertThat;

import com.chutneytesting.scenario.api.dto.ComposableStepDto;
import com.chutneytesting.scenario.api.dto.ComposableTestCaseDto;
import com.chutneytesting.scenario.api.dto.ImmutableComposableScenarioDto;
import com.chutneytesting.scenario.api.dto.ImmutableComposableStepDto;
import com.chutneytesting.scenario.api.dto.ImmutableComposableTestCaseDto;
import com.chutneytesting.scenario.domain.ComposableScenario;
import com.chutneytesting.scenario.domain.ComposableStep;
import com.chutneytesting.scenario.domain.ComposableTestCase;
import com.chutneytesting.scenario.domain.TestCaseMetadataImpl;
import com.chutneytesting.tools.ui.ImmutableKeyValue;
import com.chutneytesting.tools.ui.KeyValue;
import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

public class ComposableTestCaseMapperTest {

    private final String DEFAULT_COMPOSABLE_TESTCASE_ID = "30-1";

    private final ComposableTestCaseDto composableTestCaseDto =
        ImmutableComposableTestCaseDto.builder()
            .id(DEFAULT_COMPOSABLE_TESTCASE_ID)
            .title("Default title")
            .description("Default description")
            .creationDate(Instant.MIN)
            .addTags("tag1", "tag2", "")
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
                                    Map.of(
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
                    Map.of(
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
            DEFAULT_COMPOSABLE_TESTCASE_ID,
            TestCaseMetadataImpl.builder()
                .withTitle("Default title")
                .withDescription("Default description")
                .withCreationDate(Instant.MIN)
                .withTags(Arrays.asList("tag1","tag2"))
                .withRepositorySource("ComposableTestCase")
                .withDatasetId("66-7")
                .withAuthor("author")
                .withUpdateDate(Instant.MIN)
                .withVersion(666)
                .build(),
            ComposableScenario.builder()
                .withComposableSteps(
                    Collections.singletonList(
                        ComposableStep.builder()
                            .withId("30-10")
                            .withName("First default functional ref")
                            .withImplementation("{ \"type\": \"default-identifier\" }")
                            .withDefaultParameters(
                                Map.of(
                                    "key valued", "value",
                                    "empty key", ""
                                )
                            )
                            .withExecutionParameters(
                                Map.of(
                                    "key valued", "value",
                                    "empty key", ""
                                )
                            )
                            .build()
                    )
                )
                .withParameters(
                    Map.of(
                        "scenario key valued", "scenario value",
                        "scenario empty key", ""
                    )
                )
                .build()
        );

    @Test
    public void should_map_from_dto() {
        // When
        final ComposableTestCase ctc = ComposableTestCaseMapper.fromDto(composableTestCaseDto);

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
        final ComposableTestCaseDto ctcd = ComposableTestCaseMapper.toDto(composableTestCase);

        // Then
        Assertions.assertThat(ctcd)
            .usingRecursiveComparison()
            .ignoringFieldsMatchingRegexes("tags")
            .ignoringCollectionOrder()
            .isEqualTo(composableTestCaseDto);
        Assertions.assertThat(ctcd.tags()).containsExactly("TAG1", "TAG2");
    }

}
