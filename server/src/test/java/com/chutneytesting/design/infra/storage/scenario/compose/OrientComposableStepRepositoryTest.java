package com.chutneytesting.design.infra.storage.scenario.compose;

import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.chutneytesting.design.domain.scenario.compose.AlreadyExistingComposableStepException;
import com.chutneytesting.design.domain.scenario.compose.ComposableStep;
import com.chutneytesting.design.domain.scenario.compose.ComposableStepRepository;
import com.chutneytesting.design.domain.scenario.compose.ParentStepId;
import com.chutneytesting.design.domain.scenario.compose.Strategy;
import com.chutneytesting.design.infra.storage.scenario.compose.orient.OrientComponentDB;
import com.chutneytesting.design.infra.storage.scenario.compose.orient.changelog.OrientChangelog;
import com.chutneytesting.tests.AbstractOrientDatabaseTest;
import com.chutneytesting.tools.ImmutablePaginationRequestParametersDto;
import com.chutneytesting.tools.ImmutableSortRequestParametersDto;
import com.chutneytesting.tools.PaginatedDto;
import com.orientechnologies.common.log.OLogManager;
import com.orientechnologies.orient.core.db.ODatabaseSession;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import org.apache.groovy.util.Maps;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

@SuppressWarnings({"SameParameterValue", "OptionalUsedAsFieldOrParameterType", "OptionalGetWithoutIsPresent"})
public class OrientComposableStepRepositoryTest extends AbstractOrientDatabaseTest {

    private static ComposableStepRepository sut;

    @BeforeAll
    public static void setUp() {
        OrientComposableStepRepositoryTest.initComponentDB(DATABASE_NAME);
        sut = new OrientComposableStepRepository(orientComponentDB, stepMapper);
        OLogManager.instance().setWarnEnabled(false);
    }

    @AfterEach
    public void after() {
        truncateCollection(DATABASE_NAME, OrientComponentDB.STEP_CLASS);
        truncateCollection(DATABASE_NAME, OrientComponentDB.GE_STEP_CLASS);
    }

    @AfterAll
    public static void tearDown() {
        OrientComposableStepRepositoryTest.destroyDB(DATABASE_NAME);
    }

    @Test
    public void should_find_saved_step_when_search_by_id() {
        // Given
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("timeOut", "10 s");
        parameters.put("retryDelay", "10 s");

        final ComposableStep fStep = saveAndReload(
            buildComposableStep(
                "a thing is connected",
                new Strategy("retry-with-timeout", parameters)
            )
        );

        // When
        ComposableStep foundFStep = sut.findById(fStep.id);

        // Then
        assertThat(foundFStep).isNotNull();
        assertThat(foundFStep.id).isEqualTo(fStep.id);
        assertThat(foundFStep.name).isEqualTo(fStep.name);
        assertThat(foundFStep.strategy.type).isEqualToIgnoringCase("retry-with-timeout");
        assertThat(foundFStep.strategy.parameters.get("retryDelay")).isEqualTo("10 s");
        assertThat(foundFStep.strategy.parameters.get("timeOut")).isEqualTo("10 s");
    }

    @Test
    public void should_find_saved_step_with_tags_when_search_by_id() {
        // Given
        List<String> tags = Stream.of("zug zug", "dabu").collect(toList());
        final ComposableStep fStep = saveAndReload(
            buildComposableStep(
                "a thing is connected",
                tags
            )
        );

        // When
        ComposableStep foundFStep = sut.findById(fStep.id);

        // Then
        assertThat(foundFStep).isNotNull();
        assertThat(foundFStep.id).isEqualTo(fStep.id);
        assertThat(foundFStep.name).isEqualTo(fStep.name);
        assertThat(foundFStep.tags).isEqualTo(fStep.tags);
    }

    @Test
    public void should_find_all_steps_when_findAll_called() {
        should_save_all_func_steps_with_multiple_step_types_when_save_scenario();
        List<ComposableStep> all = sut.findAll();
        assertThat(all).hasSize(7);
    }

    @Test
    public void should_save_all_func_steps_with_multiple_step_types_when_save_scenario() {
        // Given
        final ComposableStep f1 = saveAndReload(
            buildComposableStep("when"));
        final ComposableStep f21 = saveAndReload(
            buildComposableStep("then sub 1"));
        final ComposableStep f22 = saveAndReload(
            buildComposableStep("then sub 2 with implementation", "{\"type\": \"debug\"}"));
        final ComposableStep f23 = saveAndReload(
            buildComposableStep("then sub 3 with implementation", "  \"type\": \"debug\"  "));
        final ComposableStep f24 = saveAndReload(
            buildComposableStep("then sub 4"));
        final ComposableStep f25 = saveAndReload(
            buildComposableStep("then sub 5 with implementation", " {\r\"type\": \"debug\"\r} "));
        final ComposableStep f2 = buildComposableStep("then", f21, f22, f23, f24, f25);
        List<ComposableStep> steps = Arrays.asList(f1, f2);

        // When
        String record1 = sut.save(f1);
        String record2 = sut.save(f2);
        List<String> recordIds = Arrays.asList(record1, record2);

        // Then
        assertScenarioRids(steps, recordIds);
    }

    @Test
    public void should_not_update_func_step_when_name_already_exists() {
        // Given
        String name = "a thing is connected";
        sut.save(buildComposableStep(name));

        // When
        assertThatThrownBy(() -> sut.save(buildComposableStep(name)))
            .isInstanceOf(AlreadyExistingComposableStepException.class)
            .hasMessageContaining("already exists");
    }

    @Test
    public void should_update_func_step_when_id_already_exists() {
        // Given
        final ComposableStep subWithImplementation = saveAndReload(
            buildComposableStep("sub with implementation", "{\"type\": \"debug\"}"));
        String name = "a thing is connected";
        String fStepId = sut.save(
            buildComposableStep(name, subWithImplementation)
        );

        // When
        String newName = "another thing is connected";
        String newSubTechnicalContent = "{\"type\": \"success\"}";
        final ComposableStep newSub_with_implementation = saveAndReload(
            buildComposableStep("sub with implementation", newSubTechnicalContent, subWithImplementation.id));
        String updateFStepId = sut.save(ComposableStep.builder()
            .withName(newName)
            .withId(fStepId)
            .withSteps(Collections.singletonList(newSub_with_implementation))
            .build()
        );
        ComposableStep updatedFStep = sut.findById(updateFStepId);

        // Then
        assertThat(fStepId).isEqualTo(updateFStepId);
        assertThat(updatedFStep.name).isEqualTo(newName);
        assertThat(updatedFStep.steps.get(0).implementation.get()).isEqualTo(newSubTechnicalContent);
    }

    @Test
    public void should_find_saved_func_steps_by_page_when_find_called() {
        // Given
        final ComposableStep fStep_11 = saveAndReload(
            buildComposableStep("sub something happen 1.1"));
        final ComposableStep fStep_12 = saveAndReload(
            buildComposableStep("sub something happen 1.2 with implementation", "{\"type\": \"debug\"}"));
        final ComposableStep fStep_13 = saveAndReload(
            buildComposableStep("sub something happen 1.3"));
        final ComposableStep fStep_1 = saveAndReload(
            buildComposableStep("sub something happen 1", fStep_11, fStep_12, fStep_13));
        final ComposableStep fStep_2 = saveAndReload(
            buildComposableStep("another sub something 2 with implementation", "{\"type\": \"debug\"}"));
        final ComposableStep fStep_3 = saveAndReload(
            buildComposableStep("sub something happen 3"));
        final ComposableStep fStepRoot = saveAndReload(
            buildComposableStep("something happen", fStep_1, fStep_2, fStep_3));

        List<ComposableStep> fSteps = Arrays.asList(fStepRoot, fStep_11, fStep_12, fStep_13, fStep_1, fStep_2, fStep_3);

        // When
        long elementPerPage = 2;
        PaginatedDto<ComposableStep> foundFStepsPage1 = findWithPagination(1, elementPerPage);
        PaginatedDto<ComposableStep> foundFStepsPage2 = findWithPagination(3, elementPerPage);
        PaginatedDto<ComposableStep> foundFStepsPage3 = findWithPagination(5, elementPerPage);
        PaginatedDto<ComposableStep> foundFStepsPage4 = findWithPagination(7, elementPerPage);

        // Then
        List<ComposableStep> foundFSteps = new ArrayList<>();
        foundFSteps.addAll(foundFStepsPage1.data());
        foundFSteps.addAll(foundFStepsPage2.data());
        foundFSteps.addAll(foundFStepsPage3.data());
        foundFSteps.addAll(foundFStepsPage4.data());

        assertThat(foundFStepsPage1.data().size()).isEqualTo(elementPerPage);
        assertThat(foundFStepsPage1.totalCount()).isEqualTo(fSteps.size());
        assertThat(foundFStepsPage2.data().size()).isEqualTo(elementPerPage);
        assertThat(foundFStepsPage2.totalCount()).isEqualTo(fSteps.size());
        assertThat(foundFStepsPage3.data().size()).isEqualTo(elementPerPage);
        assertThat(foundFStepsPage3.totalCount()).isEqualTo(fSteps.size());
        assertThat(foundFSteps.size()).isEqualTo(fSteps.size());
        assertThat(foundFSteps).containsExactlyInAnyOrderElementsOf(fSteps);
    }

    @Test
    public void should_find_saved_func_steps_with_filter_when_find_called() {
        // Given
        final ComposableStep fStep_1 = saveAndReload(
            buildComposableStep("some thing is set"));
        final ComposableStep fStep_2 = saveAndReload(
            buildComposableStep("another thing happens", "{\"type\": \"debug\"}"));
        final ComposableStep fStep_3 = saveAndReload(
            buildComposableStep("the result is beauty"));
        final ComposableStep fStepRoot = saveAndReload(
            buildComposableStep("big root thing", fStep_1, fStep_2, fStep_3));

        // When
        PaginatedDto<ComposableStep> filteredByNameFSteps = findWithFilters("beauty", null, null);
        PaginatedDto<ComposableStep> sortedFSteps = findWithFilters("", "name", null);
        PaginatedDto<ComposableStep> sortedDescendentFSteps = findWithFilters("", "name", "");
        PaginatedDto<ComposableStep> filteredFSteps = findWithFilters("thing", "name", "name");

        // Then
        assertThat(filteredByNameFSteps.data()).containsExactly(fStep_3);
        assertThat(sortedFSteps.data()).containsExactly(fStep_2, fStepRoot, fStep_1, fStep_3);
        assertThat(sortedDescendentFSteps.data()).containsExactly(fStep_3, fStep_1, fStepRoot, fStep_2);
        assertThat(filteredFSteps.data()).containsExactly(fStep_1, fStepRoot, fStep_2);
    }

    @Test
    public void should_find_func_step_parents_when_asked_for() {
        // Given
        ComposableStep fStep_111   = saveAndReload(buildComposableStep("that 1"));
        ComposableStep fStep_112   = saveAndReload(buildComposableStep("that 2"));
        ComposableStep fStep_11    = saveAndReload(buildComposableStep("that", fStep_111, fStep_112));
        ComposableStep fStep_12    = saveAndReload(buildComposableStep("inner that", fStep_11));
        ComposableStep fStepRoot_1 = saveAndReload(buildComposableStep("a thing", fStep_11, fStep_12, fStep_11));
        ComposableStep fStep_21    = saveAndReload(buildComposableStep("this", fStep_11));

        // When
        List<ParentStepId> parentsId = sut.findParents(fStep_11.id);

        // Then
        assertThat(parentsId)
            .usingRecursiveFieldByFieldElementComparator()
            .containsExactlyInAnyOrder(
                new ParentStepId(fStepRoot_1.id, fStepRoot_1.name, false),
                new ParentStepId(fStep_12.id, fStep_12.name, false),
                new ParentStepId(fStep_21.id, fStep_21.name, false));
    }

    @Test
    public void should_find_saved_step_parameters_and_build_correct_dataset_when_search_by_id() {
        // Given
        Map<String, String> actionParameters = Maps.of(
            "action parameter with default value", "default action parameter value",
            "action parameter with no default value", "",
            "another action parameter with default value", "another default action parameter value");
        final ComposableStep actionStep = saveAndReload(
            buildComposableStep(
                "action with parameters",
                actionParameters
            )
        );

        Map<String, String> firstActionInstanceDataSet = Maps.of(
            "action parameter with default value", "first action instance parameter value",
            "action parameter with no default value", "",
            "another action parameter with default value", "another default action parameter value");
        final ComposableStep firstActionStepInstance = ComposableStep.builder()
            .from(actionStep)
            .overrideEnclosedUsageParametersWith(firstActionInstanceDataSet)
            .build();

        Map<String, String> secondActionInstanceDataSet = Maps.of(
            "action parameter with default value", "default action parameter value",
            "action parameter with no default value", "second action instance not default value value",
            "another action parameter with default value", "");
        final ComposableStep secondActionStepInstance = ComposableStep.builder()
            .from(actionStep)
            .overrideEnclosedUsageParametersWith(secondActionInstanceDataSet)
            .build();

        Map<String, String> middleParentParameters = Maps.of(
            "middle parent parameter with default value", "default middle parent parameter value",
            "middle parent parameter with not default value", "",
            "another middle parent parameter with default value", "another default middle parent parameter value");
        final ComposableStep middleParentStep = saveAndReload(
            buildComposableStep(
                "middle parent with parameters",
                middleParentParameters,
                firstActionStepInstance, secondActionStepInstance
            )
        );
        Map<String, String> middleParentExpectedDataSet = Maps.of(
            "action parameter with no default value", "",
            "another action parameter with default value", "",
            "middle parent parameter with default value", "default middle parent parameter value",
            "middle parent parameter with not default value", "",
            "another middle parent parameter with default value", "another default middle parent parameter value"
        );

        Map<String, String> firstMiddleParentInstanceDataSet = Maps.of(
            "action parameter with no default value", "first middle parent action no default value",
            "another action parameter with default value", "first middle parent action value",
            "middle parent parameter with default value", "default middle parent parameter value",
            "middle parent parameter with not default value", "first middle parent instance not default value value",
            "another middle parent parameter with default value", ""
        );
        final ComposableStep firstMiddleParentStepInstance = ComposableStep.builder()
            .from(middleParentStep)
            .overrideEnclosedUsageParametersWith(firstMiddleParentInstanceDataSet)
            .build();

        Map<String, String> secondMiddleParentInstanceDataSet = Maps.of(
            "action parameter with no default value", "",
            "another action parameter with default value", "second middle parent action value",
            "middle parent parameter with default value", "second middle parent parameter value",
            "middle parent parameter with not default value", "",
            "another middle parent parameter with default value", "another second middle parent parameter value"
        );
        final ComposableStep secondMiddleParentStepInstance = ComposableStep.builder()
            .from(middleParentStep)
            .overrideEnclosedUsageParametersWith(secondMiddleParentInstanceDataSet)
            .build();

        Map<String, String> thirdActionInstanceDataSet = Maps.of(
            "action parameter with default value", "third action instance parameter value",
            "action parameter with no default value", "yet another third action instance parameter value",
            "another action parameter with default value", "another third action instance parameter value");
        final ComposableStep thirdActionStepInstance = ComposableStep.builder()
            .from(actionStep)
            .overrideEnclosedUsageParametersWith(thirdActionInstanceDataSet)
            .build();

        Map<String, String> parentParameters = Maps.of(
            "parent parameter", "parent parameter default value");
        final ComposableStep parentStep = saveAndReload(
            buildComposableStep(
                "parent with parameters",
                parentParameters,
                firstMiddleParentStepInstance, thirdActionStepInstance, secondMiddleParentStepInstance
            )
        );
        Map<String, String> parentExpectedDataSet = Maps.of(
            "another middle parent parameter with default value", "",
            "action parameter with no default value", "",
            "middle parent parameter with not default value", "",
            "parent parameter", "parent parameter default value"
            );

        // When
        ComposableStep foundAction = sut.findById(actionStep.id);
        ComposableStep foundMiddleParentFStep = sut.findById(middleParentStep.id);
        ComposableStep foundParentFStep = sut.findById(parentStep.id);

        // Then
        assertThat(foundAction.builtInParameters).containsExactlyEntriesOf(actionParameters);
        assertThat(foundAction.enclosedUsageParameters).containsExactlyEntriesOf(actionParameters);

        assertThat(foundMiddleParentFStep.steps.get(0).builtInParameters).containsExactlyEntriesOf(actionParameters);
        assertThat(foundMiddleParentFStep.steps.get(0).enclosedUsageParameters).containsExactlyEntriesOf(firstActionInstanceDataSet);
        assertThat(foundMiddleParentFStep.steps.get(1).builtInParameters).containsExactlyEntriesOf(actionParameters);
        assertThat(foundMiddleParentFStep.steps.get(1).enclosedUsageParameters).containsExactlyEntriesOf(secondActionInstanceDataSet);
        assertThat(foundMiddleParentFStep.builtInParameters).containsExactlyEntriesOf(middleParentParameters);
        assertThat(foundMiddleParentFStep.enclosedUsageParameters).containsExactlyEntriesOf(middleParentExpectedDataSet);

        assertThat(foundParentFStep.builtInParameters).containsExactlyEntriesOf(parentParameters);
        assertThat(foundParentFStep.enclosedUsageParameters).containsExactlyEntriesOf(parentExpectedDataSet);
        assertThat(foundParentFStep.steps.get(0).builtInParameters).containsExactlyEntriesOf(middleParentParameters);
        assertThat(foundParentFStep.steps.get(0).enclosedUsageParameters).containsExactlyEntriesOf(firstMiddleParentInstanceDataSet);
        assertThat(foundParentFStep.steps.get(1).builtInParameters).containsExactlyEntriesOf(actionParameters);
        assertThat(foundParentFStep.steps.get(1).enclosedUsageParameters).containsExactlyEntriesOf(thirdActionInstanceDataSet);
        assertThat(foundParentFStep.steps.get(2).builtInParameters).containsExactlyEntriesOf(middleParentParameters);
        assertThat(foundParentFStep.steps.get(2).enclosedUsageParameters).containsExactlyEntriesOf(secondMiddleParentInstanceDataSet);
    }

    @Test
    public void should_update_parents_dataset_when_updated_with_parameters_change() {
        // Given
        final String deleted_param = "param with no default value";
        final String new_value_param = "param with default value";
        final String new_param = "new param";

        Map<String, String> stepParameters = Maps.of(
            deleted_param, "",
            new_value_param, "default value");
        ComposableStep step = saveAndReload(
            buildComposableStep("my step with parameters", stepParameters));

        ComposableStep parentWithNoParametersOverload = saveAndReload(
            buildComposableStep("parent with no parameters values overload", step)
        );

        Map<String, String> stepInsatnceDataSet = Maps.of(
            deleted_param, "parent value",
            new_value_param, "new parent value");
        ComposableStep stepInstance = ComposableStep.builder()
            .from(step)
            .overrideEnclosedUsageParametersWith(stepInsatnceDataSet)
            .build();
        ComposableStep parentWithParametersOverload = saveAndReload(
            buildComposableStep("parent with parameters values overload", stepInstance)
        );

        // When
        Map<String, String> newStepParameters = Maps.of(
            new_value_param, "another value",
            new_param, "new value");
        ComposableStep stepUpdate = ComposableStep.builder()
            .from(step)
            .withBuiltInParameters(newStepParameters)
            .build();
        sut.save(stepUpdate);

        // Then
        parentWithNoParametersOverload = findByName(parentWithNoParametersOverload.name);
        parentWithParametersOverload = findByName(parentWithParametersOverload.name);

        assertThat(parentWithNoParametersOverload.steps.get(0).builtInParameters).isEqualTo(newStepParameters);
        assertThat(parentWithNoParametersOverload.steps.get(0).enclosedUsageParameters).isEqualTo(newStepParameters);
        assertThat(parentWithNoParametersOverload.steps.get(0).builtInParameters).isEqualTo(newStepParameters);
        assertThat(parentWithParametersOverload.steps.get(0).enclosedUsageParameters).containsExactly(
            new AbstractMap.SimpleEntry<>(new_value_param, stepInsatnceDataSet.get(new_value_param)),
            new AbstractMap.SimpleEntry<>(new_param, newStepParameters.get(new_param))
        );
    }

    @Test
    public void should_delete_step_and_update_edges_when_deleteById_called() {
        // Given
        final ComposableStep step = saveAndReload(
            buildComposableStep("a step", Maps.of("param", "default value")));

        final ComposableStep stepInstance = ComposableStep.builder()
            .from(step)
            .overrideEnclosedUsageParametersWith(Maps.of("param", ""))
            .build();

        final ComposableStep stepInstanceB = ComposableStep.builder()
            .from(step)
            .overrideEnclosedUsageParametersWith(Maps.of("param", "hard value"))
            .build();

        final ComposableStep parentFStep = saveAndReload(
            buildComposableStep("a parent step", stepInstance, stepInstanceB)
        );

        // When
        sut.deleteById(step.id);
        ComposableStep parentFoundStep = sut.findById(parentFStep.id);

        // Then
        assertThat(loadById(step.id)).isNull();
        assertThat(parentFoundStep.steps).isEmpty();
    }


    @Test
    public void changelog_n5_should_update_selenium_tasks() {
        // G
        final ComposableStep step = saveAndReload(
            buildComposableStep("selenium-get", "{\n" +
                "            \"identifier\": \"selenium-get-text\",\n" +
                "            \"inputs\": [ \n" +
                "              {\"name\":\"web-driver\",\"value\":\"${#webDriver}\"},\n" +
                "              {\"name\":\"action\",\"value\":\"\"},\n" +
                "              {\"name\":\"selector\",\"value\":\"//chutney-main-menu/nav/ul/ul/div/div/chutney-menu-item[**menu**]/li/a\"},\n" +
                "              {\"name\":\"by\",\"value\":\"xpath\"},\n" +
                "              {\"name\":\"value\",\"value\":\"\"},\n" +
                "              {\"name\":\"wait\",\"value\":\"5\"},\n" +
                "              {\"name\":\"switchType\",\"value\":\"\"},\n" +
                "              {\"name\":\"menuItemSelector\",\"value\":\"\"}]\n" +
                "        }"));
        final ComposableStep stepOld = saveAndReload(
            buildComposableStep("selenium-get-old", "{\n" +
                "            \"identifier\": \"selenium-get-text\",\n" +
                "            \"inputs\": {\n" +
                "                \"web-driver\": \"${#webDriver}\",\n" +
                "                \"selector\": \"//icg-login//button[@type='submit']/span\",\n" +
                "                \"by\": \"xpath\",\n" +
                "                \"wait\": 2,\n" +
                "                \"value\": \"one value\",\n" +
                "                \"switchType\": \"type\",\n" +
                "                \"menuItemSelector\": \"selector\"\n" +
                "            }\n" +
                "        }"));

        // W
        try (ODatabaseSession dbSession = orientComponentDB.dbPool().acquire()) {
            OrientChangelog.updateSeleniumTaskParametersRight(dbSession);
        }

        // T
        ComposableStep step1 = sut.findById(step.id);
        assertThat(step1.implementation.get()).doesNotContain("name\":\"action", "name\":\"value", "name\":\"switchType", "name\":\"menuItemSelector");
        ComposableStep stepO = sut.findById(stepOld.id);
        assertThat(stepO.implementation.get()).doesNotContain("action", "value", "switchType", "menuItemSelector");
    }

    private ComposableStep saveAndReload(ComposableStep composableStep) {
        return saveAndReload(sut, composableStep);
    }

    private void assertScenarioRids(List<ComposableStep> scenario, List<String> rids) {
        IntStream.range(0, rids.size())
            .forEach(idx -> {
                String rid = rids.get(idx);
                ComposableStep fStep = sut.findById(rid);
                assertThat(fStep.id).isEqualTo(rid);
                assertComposableStep(scenario.get(idx), fStep);
            });
    }

    private void assertComposableStep(ComposableStep expectedFStep, ComposableStep step) {
        assertComposableStep(
            step,
            expectedFStep.name,
            expectedFStep.implementation,
            expectedFStep.steps.size());

        IntStream.range(0, expectedFStep.steps.size()).forEach(
            idx -> {
                ComposableStep subStep = step.steps.get(idx);
                assertComposableStep(expectedFStep.steps.get(idx), subStep);
            }
        );
    }

    private void assertComposableStep(ComposableStep step, String name, Optional<String> implementation, int functionalChildStepsSize) {
        assertThat(step).isInstanceOf(ComposableStep.class);
        assertThat(step.name).isEqualTo(name);

        if (implementation.isPresent()) {
            assertThat(step.implementation.orElseThrow(() -> new IllegalStateException("Implementation should be set"))).isEqualTo(implementation.get());
        } else {
            assertThat(step.implementation.isPresent()).isFalse();
        }
        assertThat(step.id).isNotEmpty();
        assertThat(step.steps.size()).isEqualTo(functionalChildStepsSize);
    }

    private PaginatedDto<ComposableStep> findWithPagination(long startElementIdx, long limit) {
        return sut.find(
            ImmutablePaginationRequestParametersDto.builder()
                .start(startElementIdx)
                .limit(limit)
                .build(),
            ImmutableSortRequestParametersDto.builder()
                .build(),
            ComposableStep.builder()
                .withName("")
                .withSteps(Collections.emptyList())
                .build()
        );
    }

    private PaginatedDto<ComposableStep> findWithFilters(String name, String sort, String desc) {
        return sut.find(
            ImmutablePaginationRequestParametersDto.builder()
                .start(1L)
                .limit(100L)
                .build(),
            ImmutableSortRequestParametersDto.builder()
                .sort(sort)
                .desc(desc)
                .build(),
            ComposableStep.builder()
                .withName(name)
                .withSteps(Collections.emptyList())
                .build()
        );
    }
}
