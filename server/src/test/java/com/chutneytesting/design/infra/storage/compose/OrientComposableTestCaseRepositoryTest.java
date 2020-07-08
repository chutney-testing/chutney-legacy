package com.chutneytesting.design.infra.storage.compose;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.failBecauseExceptionWasNotThrown;

import com.orientechnologies.common.log.OLogManager;
import com.orientechnologies.orient.core.id.ORecordId;
import com.orientechnologies.orient.core.record.OElement;
import com.chutneytesting.design.domain.compose.ComposableScenario;
import com.chutneytesting.design.domain.compose.ComposableTestCase;
import com.chutneytesting.design.domain.compose.ComposableTestCaseRepository;
import com.chutneytesting.design.domain.compose.FunctionalStep;
import com.chutneytesting.design.domain.compose.StepRepository;
import com.chutneytesting.design.domain.scenario.TestCaseMetadata;
import com.chutneytesting.design.domain.scenario.TestCaseMetadataImpl;
import com.chutneytesting.design.infra.storage.db.orient.OrientComponentDB;
import com.chutneytesting.tests.AbstractOrientDatabaseTest;
import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.apache.groovy.util.Maps;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

public class OrientComposableTestCaseRepositoryTest extends AbstractOrientDatabaseTest {

    private static ComposableTestCaseRepository sut;

    @BeforeClass
    public static void setUp() {
        OrientFunctionalStepRepositoryTest.initComponentDB(DATABASE_NAME);
        sut = new OrientComposableTestCaseRepository(orientComponentDB);
        OLogManager.instance().setWarnEnabled(false);
        initFunctionalStepsRepository();
    }

    private static FunctionalStep FUNC_STEP_REF;
    private static Map<String, String> FUNC_STEP_REF_PARAMERTERS = Maps.of(
        "child parameter with no overload", "child initial value",
        "child parameter with parent overload", "child value to be overloaded",
        "child parameter with scenario overload", "child value to be overloaded"
    );
    private static FunctionalStep FUNC_STEP_PARENT_REF;
    private static Map<String, String> FUNC_STEP_PARENT_REF_PARAMERTERS = Maps.of(
        "parent parameter with no overload", "parent initial value",
        "parent parameter with scenario overload", "parent value to be overloaded"
    );
    private static void initFunctionalStepsRepository() {
        StepRepository funcStepRepository = new OrientFunctionalStepRepository(orientComponentDB);

        FunctionalStep FUNC_STEP = FunctionalStep.builder()
            .withName("func step without children")
            .withParameters(FUNC_STEP_REF_PARAMERTERS)
            .build();
        FUNC_STEP_REF = FunctionalStep.builder()
            .from(FUNC_STEP)
            .withId(funcStepRepository.save(FUNC_STEP))
            .build();

        FunctionalStep funcStepInstance = FunctionalStep.builder()
            .from(FUNC_STEP_REF)
            .overrideDataSetWith(
                Maps.of(
                    "child parameter with no overload", "child initial value",
                    "child parameter with parent overload", "parent value overload child value",
                    "child parameter with scenario overload", ""
                ))
            .build();
        FunctionalStep FUNC_STEP_P = FunctionalStep.builder()
            .withName("func step with child")
            .withSteps(Collections.singletonList(funcStepInstance))
            .withParameters(FUNC_STEP_PARENT_REF_PARAMERTERS)
            .build();
        FUNC_STEP_PARENT_REF = FunctionalStep.builder()
            .from(FUNC_STEP_P)
            .withId(funcStepRepository.save(FUNC_STEP_P))
            .build();
    }

    @After
    public void after() {
        truncateCollection(DATABASE_NAME, OrientComponentDB.TESTCASE_CLASS);
    }

    @AfterClass
    public static void tearDown() {
        OrientFunctionalStepRepositoryTest.destroyDB(DATABASE_NAME);
    }

    @Test
    public void should_throw_exception_when_saving_testcase_with_not_existing_step() {
        // Given
        final String UKNOWN_FUNC_STEP_ID = "unknown_id";
        ComposableTestCase composableTestCase =
            new ComposableTestCase("",
                TestCaseMetadataImpl.builder().build(),
                ComposableScenario.builder()
                    .withFunctionalSteps(
                        Collections.singletonList(
                        buildFunctionalStep("", "", UKNOWN_FUNC_STEP_ID))
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
    public void should_create_testCase_with_empty_id_when_save_called() {
        // Given
        ComposableTestCase composableTestCase = saveEmptyTestCase("");

        // When
        String testCaseId = sut.save(composableTestCase);

        // Then
        final OElement element = loadById(testCaseId);
        assertThat(element).isNotNull();
    }

    @Test
    public void should_create_testCase_with_not_existing_id_when_save_called() {
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
    }

    @Test
    public void should_update_testCase_with_no_id_when_save_called() {
        // Given
        ComposableTestCase composableTestCase = saveEmptyTestCase("");

        // When
        String testCaseId = sut.save(composableTestCase);

        // Then
        final OElement element = loadById(testCaseId);
        assertThat(element).isNotNull();
    }

    @Test
    public void should_find_existing_testCase_with_default_dataset_when_findById_called() {
        // Given
        FunctionalStep FuncStepRefScenarioInstance = FunctionalStep.builder()
            .from(FUNC_STEP_REF)
            .overrideDataSetWith(
                Maps.of(
                    "child parameter with no overload", "child initial value",
                    "child parameter with parent overload", "",
                    "child parameter with scenario overload", "scenario value overload child value"
                )
            )
            .build();

        FunctionalStep FuncStepRefParentScenarioInstance = FunctionalStep.builder()
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
                    .withFunctionalSteps(Arrays.asList(FuncStepRefScenarioInstance, FuncStepRefParentScenarioInstance))
                    .withParameters(scenarioParameters)
                    .build()
                );
        final Map<String, String> expectedDataSet = Maps.of(
            "scenario parameter", "scenario value",
            "child parameter with parent overload", "",
            "parent parameter with no overload", "",
            "child parameter with scenario overload", ""
        );
        assertThat(composableTestCase.computedParameters).containsAllEntriesOf(expectedDataSet);

        String testCaseId = sut.save(composableTestCase);

        // When
        final ComposableTestCase composableTestCaseFound = sut.findById(testCaseId);

        // Then
        assertThat(composableTestCaseFound.id).isEqualTo(testCaseId);
        assertThat(composableTestCaseFound.metadata.title()).isEqualTo(composableTestCase.metadata.title());
        assertThat(composableTestCaseFound.metadata.description()).isEqualTo(composableTestCase.metadata.description());
        assertThat(composableTestCaseFound.metadata.creationDate()).isEqualTo(composableTestCase.metadata.creationDate());
        assertThat(composableTestCaseFound.metadata.tags()).containsExactly("TAG1", "TAG2");
        assertThat(composableTestCaseFound.composableScenario.functionalSteps)
            .containsExactly(FuncStepRefScenarioInstance, FuncStepRefParentScenarioInstance);
        assertThat(composableTestCaseFound.composableScenario.parameters).containsAllEntriesOf(scenarioParameters);
        assertThat(composableTestCaseFound.computedParameters).containsAllEntriesOf(expectedDataSet);
    }

    @Test
    public void should_find_all_testCases_when_findAll_called() {
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
    public void should_delete_testCase_when_removeById_called() {
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
