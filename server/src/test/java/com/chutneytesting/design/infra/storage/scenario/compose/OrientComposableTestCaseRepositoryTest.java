package com.chutneytesting.design.infra.storage.scenario.compose;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.failBecauseExceptionWasNotThrown;

import com.chutneytesting.design.domain.scenario.TestCaseMetadata;
import com.chutneytesting.design.domain.scenario.TestCaseMetadataImpl;
import com.chutneytesting.design.domain.scenario.compose.ComposableScenario;
import com.chutneytesting.design.domain.scenario.compose.ComposableStep;
import com.chutneytesting.design.domain.scenario.compose.ComposableStepRepository;
import com.chutneytesting.design.domain.scenario.compose.ComposableTestCase;
import com.chutneytesting.design.domain.scenario.compose.ComposableTestCaseRepository;
import com.chutneytesting.design.infra.storage.scenario.compose.orient.OrientComponentDB;
import com.chutneytesting.tests.AbstractOrientDatabaseTest;
import com.orientechnologies.common.log.OLogManager;
import com.orientechnologies.orient.core.id.ORecordId;
import com.orientechnologies.orient.core.record.OElement;
import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.apache.groovy.util.Maps;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class OrientComposableTestCaseRepositoryTest extends AbstractOrientDatabaseTest {

    private static ComposableTestCaseRepository sut;

    @BeforeAll
    public static void setUp() {
        OrientComposableStepRepositoryTest.initComponentDB(DATABASE_NAME);
        sut = new OrientComposableTestCaseRepository(orientComponentDB, testCaseMapper);
        OLogManager.instance().setWarnEnabled(false);
        initComposableStepsRepository();
    }

    private static ComposableStep FUNC_STEP_REF;
    private static Map<String, String> FUNC_STEP_REF_PARAMERTERS = Maps.of(
        "child parameter with no overload", "child initial value",
        "child parameter with parent overload", "child value to be overloaded",
        "child parameter with scenario overload", "child value to be overloaded"
    );
    private static ComposableStep FUNC_STEP_PARENT_REF;
    private static Map<String, String> FUNC_STEP_PARENT_REF_PARAMERTERS = Maps.of(
        "parent parameter with no overload", "parent initial value",
        "parent parameter with scenario overload", "parent value to be overloaded"
    );

    private static void initComposableStepsRepository() {
        ComposableStepRepository funcComposableStepRepository = new OrientComposableStepRepository(orientComponentDB, stepMapper);

        ComposableStep FUNC_STEP = ComposableStep.builder()
            .withName("func step without children")
            .withParameters(FUNC_STEP_REF_PARAMERTERS)
            .build();
        FUNC_STEP_REF = ComposableStep.builder()
            .from(FUNC_STEP)
            .withId(funcComposableStepRepository.save(FUNC_STEP))
            .build();

        ComposableStep funcStepInstance = ComposableStep.builder()
            .from(FUNC_STEP_REF)
            .overrideDataSetWith(
                Maps.of(
                    "child parameter with no overload", "child initial value",
                    "child parameter with parent overload", "parent value overload child value",
                    "child parameter with scenario overload", ""
                ))
            .build();
        ComposableStep FUNC_STEP_P = ComposableStep.builder()
            .withName("func step with child")
            .withSteps(Collections.singletonList(funcStepInstance))
            .withParameters(FUNC_STEP_PARENT_REF_PARAMERTERS)
            .build();
        FUNC_STEP_PARENT_REF = ComposableStep.builder()
            .from(FUNC_STEP_P)
            .withId(funcComposableStepRepository.save(FUNC_STEP_P))
            .build();
    }

    @AfterEach
    public void after() {
        truncateCollection(DATABASE_NAME, OrientComponentDB.TESTCASE_CLASS);
    }

    @AfterAll
    public static void tearDown() {
        OrientComposableStepRepositoryTest.destroyDB(DATABASE_NAME);
    }

    @Test
    public void should_throw_exception_when_saving_testcase_with_unknown_step() {
        // Given
        final String UKNOWN_FUNC_STEP_ID = "unknown_id";
        ComposableTestCase composableTestCase =
            new ComposableTestCase("",
                TestCaseMetadataImpl.builder().build(),
                ComposableScenario.builder()
                    .withComposableSteps(
                        Collections.singletonList(
                            buildComposableStep("", "", UKNOWN_FUNC_STEP_ID))
                    )
                    .build()
            );

        // When / Then
        try {
            sut.save(composableTestCase);
            failBecauseExceptionWasNotThrown(RuntimeException.class);
        } catch (RuntimeException re) {
            assertThat(re).hasMessageContaining(UKNOWN_FUNC_STEP_ID);
        }
    }

    @Test
    public void should_create_and_find_new_testCase() {
        // When
        ComposableTestCase composableTestCase = saveEmptyTestCase("");

        // Then
        assertThat(composableTestCase).isNotNull();
        assertThat(composableTestCase.id).isNotBlank();
    }

    @Test
    public void should_create_new_testCase_with_valid_unknown_id() {
        // Given
        final String VALID_UNKNOWN_TESTCASE_ID = "#10:2";
        assertThat(ORecordId.isA(VALID_UNKNOWN_TESTCASE_ID)).isTrue();
        ComposableTestCase composableTestCase =
            new ComposableTestCase(VALID_UNKNOWN_TESTCASE_ID,
                TestCaseMetadataImpl.builder().build(),
                ComposableScenario.builder().build()
            );

        // When
        String testCaseId = sut.save(composableTestCase);

        // Then
        final OElement element = loadById(testCaseId);
        assertThat(element).isNotNull();
        assertThat(element.getIdentity().toString()).isNotEqualTo(VALID_UNKNOWN_TESTCASE_ID);
    }

    @Test
    public void should_update_testCase() {
        // Given
        ComposableTestCase composableTestCase = saveEmptyTestCase("");

        // When
        String new_title = "new title";
        composableTestCase = new ComposableTestCase(composableTestCase.id,
            TestCaseMetadataImpl.TestCaseMetadataBuilder.from(composableTestCase.metadata).withTitle(new_title).build(),
            composableTestCase.composableScenario);
        ComposableTestCase composableTestCaseUpdated = sut.findById(sut.save(composableTestCase));

        // Then
        assertThat(composableTestCaseUpdated.id).isEqualTo(composableTestCase.id);
        assertThat(composableTestCaseUpdated.metadata.title()).isEqualTo(new_title);
    }

    @Test
    public void should_find_existing_testCase_with_default_dataset() {
        // Given
        ComposableStep FuncStepRefScenarioInstance = ComposableStep.builder()
            .from(FUNC_STEP_REF)
            .overrideDataSetWith(
                Maps.of(
                    "child parameter with no overload", "child initial value",
                    "child parameter with parent overload", "",
                    "child parameter with scenario overload", "scenario value overload child value"
                )
            )
            .build();

        ComposableStep FuncStepRefParentScenarioInstance = ComposableStep.builder()
            .from(FUNC_STEP_PARENT_REF)
            .overrideDataSetWith(
                Maps.of(
                    "parent parameter with no overload", "",
                    "parent parameter with scenario overload", "scenario value overload parent value",
                    "child parameter with scenario overload", ""
                )
            )
            .build();

        final Map<String, String> scenarioParameters = Maps.of(
            "scenario parameter", "scenario value"
        );
        ComposableTestCase composableTestCase =
            new ComposableTestCase("",
                TestCaseMetadataImpl.builder()
                    .withTitle("title")
                    .withDescription("description")
                    .withCreationDate(Instant.now())
                    .withTags(Arrays.asList("tag1", "tag2"))
                    .build(),
                ComposableScenario.builder()
                    .withComposableSteps(Arrays.asList(FuncStepRefScenarioInstance, FuncStepRefParentScenarioInstance))
                    .withParameters(scenarioParameters)
                    .build()
            );
        final Map<String, String> expectedDataSet = Maps.of(
            "scenario parameter", "scenario value",
            "child parameter with parent overload", "",
            "parent parameter with no overload", "",
            "child parameter with scenario overload", ""
        );
        assertThat(composableTestCase.parameters).containsAllEntriesOf(expectedDataSet);

        String testCaseId = sut.save(composableTestCase);

        // When
        final ComposableTestCase composableTestCaseFound = sut.findById(testCaseId);

        // Then
        assertThat(composableTestCaseFound.id).isEqualTo(testCaseId);
        assertThat(composableTestCaseFound.metadata.title()).isEqualTo(composableTestCase.metadata.title());
        assertThat(composableTestCaseFound.metadata.description()).isEqualTo(composableTestCase.metadata.description());
        assertThat(composableTestCaseFound.metadata.creationDate()).isEqualTo(composableTestCase.metadata.creationDate());
        assertThat(composableTestCaseFound.metadata.tags()).containsExactly("TAG1", "TAG2");
        assertThat(composableTestCaseFound.composableScenario.composableSteps)
            .containsExactly(FuncStepRefScenarioInstance, FuncStepRefParentScenarioInstance);
        assertThat(composableTestCaseFound.composableScenario.parameters).containsAllEntriesOf(scenarioParameters);
        assertThat(composableTestCaseFound.parameters).containsAllEntriesOf(expectedDataSet);
    }

    @Test
    public void should_find_all_testCases() {
        // Given
        ComposableTestCase composableTestCase1 = saveEmptyTestCase("one");
        ComposableTestCase composableTestCase2 = saveEmptyTestCase("two");

        // When
        final List<TestCaseMetadata> all = sut.findAll();

        // Then
        assertThat(all).hasSize(2);
        assertThat(all).containsExactly(composableTestCase1.metadata, composableTestCase2.metadata);
    }

    @Test
    public void should_delete_testCase_by_id() {
        // Given
        ComposableTestCase composableTestCase = saveEmptyTestCase("one");

        // When
        sut.removeById(composableTestCase.id);

        // Then
        assertThat(loadById(composableTestCase.id)).isNull();
    }

    private ComposableTestCase saveEmptyTestCase(String title) {
        final ComposableTestCase composableTestCase = new ComposableTestCase("",
            TestCaseMetadataImpl.builder().withTitle(title).build(),
            ComposableScenario.builder().build());
        return sut.findById(
            sut.save(composableTestCase));
    }
}
