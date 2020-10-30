package com.chutneytesting.design.infra.storage.compose;

import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.chutneytesting.design.domain.compose.AlreadyExistingFunctionalStepException;
import com.chutneytesting.design.domain.compose.FunctionalStep;
import com.chutneytesting.design.domain.compose.FunctionalStepCyclicDependencyException;
import com.chutneytesting.design.domain.compose.ParentStepId;
import com.chutneytesting.design.domain.compose.StepRepository;
import com.chutneytesting.design.domain.compose.StepUsage;
import com.chutneytesting.design.domain.compose.Strategy;
import com.chutneytesting.design.infra.storage.db.orient.OrientComponentDB;
import com.chutneytesting.design.infra.storage.db.orient.changelog.OrientChangelog;
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
import org.assertj.core.api.Assertions;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

@SuppressWarnings({"SameParameterValue", "OptionalUsedAsFieldOrParameterType", "OptionalGetWithoutIsPresent"})
public class OrientFunctionalStepRepositoryTest extends AbstractOrientDatabaseTest {

    private static StepRepository sut;

    @BeforeClass
    public static void setUp() {
        OrientFunctionalStepRepositoryTest.initComponentDB(DATABASE_NAME);
        sut = new OrientFunctionalStepRepository(orientComponentDB);
        OLogManager.instance().setWarnEnabled(false);
    }

    @After
    public void after() {
        truncateCollection(DATABASE_NAME, OrientComponentDB.STEP_CLASS);
        truncateCollection(DATABASE_NAME, OrientComponentDB.GE_STEP_CLASS);
    }

    @AfterClass
    public static void tearDown() {
        OrientFunctionalStepRepositoryTest.destroyDB(DATABASE_NAME);
    }

    @Test
    public void should_find_saved_step_when_search_by_id() {
        // Given
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("timeOut", "10 s");
        parameters.put("retryDelay", "10 s");

        final FunctionalStep fStep = saveAndReload(
            buildFunctionalStep(
                "a thing is connected",
                new Strategy("retry-with-timeout", parameters)
            )
        );

        // When
        FunctionalStep foundFStep = sut.findById(fStep.id);

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
        final FunctionalStep fStep = saveAndReload(
            buildFunctionalStep(
                "a thing is connected",
                tags
            )
        );

        // When
        FunctionalStep foundFStep = sut.findById(fStep.id);

        // Then
        assertThat(foundFStep).isNotNull();
        assertThat(foundFStep.id).isEqualTo(fStep.id);
        assertThat(foundFStep.name).isEqualTo(fStep.name);
        assertThat(foundFStep.tags).isEqualTo(fStep.tags);
    }

    @Test
    public void should_find_saved_step_with_loop_strategy_when_search_by_id() {
        // Given
        final FunctionalStep fStep = saveAndReload(
            buildFunctionalStep(
                "a thing is connected",
                new Strategy("Loop", Collections.singletonMap("data", "someData"))
            )
        );

        // When
        FunctionalStep foundFStep = sut.findById(fStep.id);

        // Then
        assertThat(foundFStep).isNotNull();
        assertThat(foundFStep.id).isEqualTo(fStep.id);
        assertThat(foundFStep.name).isEqualTo(fStep.name);
        assertThat(foundFStep.strategy.type).isEqualToIgnoringCase("Loop");
        assertThat(foundFStep.strategy.parameters.get("data")).isEqualTo("someData");
    }

    @Test
    public void should_find_all_steps_when_findAll_called() {
        should_save_all_func_steps_with_multiple_step_types_when_save_scenario();
        List<FunctionalStep> all = sut.findAll();
        assertThat(all).hasSize(7);
    }

    @Test
    public void should_save_all_func_steps_with_multiple_step_types_when_save_scenario() {
        // Given
        final FunctionalStep f1 = saveAndReload(
            buildFunctionalStep("when", StepUsage.WHEN));
        final FunctionalStep f21 = saveAndReload(
            buildFunctionalStep("then sub 1", StepUsage.THEN));
        final FunctionalStep f22 = saveAndReload(
            buildFunctionalStep("then sub 2 with implementation", StepUsage.THEN, "{\"type\": \"debug\"}"));
        final FunctionalStep f23 = saveAndReload(
            buildFunctionalStep("then sub 3 with implementation", StepUsage.THEN, "  \"type\": \"debug\"  "));
        final FunctionalStep f24 = saveAndReload(
            buildFunctionalStep("then sub 4", StepUsage.THEN));
        final FunctionalStep f25 = saveAndReload(
            buildFunctionalStep("then sub 5 with implementation", StepUsage.THEN, " {\r\"type\": \"debug\"\r} "));
        final FunctionalStep f2 = buildFunctionalStep("then", StepUsage.THEN, f21, f22, f23, f24, f25);
        List<FunctionalStep> steps = Arrays.asList(f1, f2);

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
        sut.save(buildFunctionalStep(name));

        // When
        assertThatThrownBy(() -> sut.save(buildFunctionalStep(name)))
            .isInstanceOf(AlreadyExistingFunctionalStepException.class)
            .hasMessageContaining("found duplicated key");
    }

    @Test
    public void should_update_func_step_when_id_already_exists() {
        // Given
        final FunctionalStep subWithImplementation = saveAndReload(
            buildFunctionalStep("sub with implementation", "{\"type\": \"debug\"}"));
        String name = "a thing is connected";
        String fStepId = sut.save(
            buildFunctionalStep(name, subWithImplementation)
        );

        // When
        String newName = "another thing is connected";
        String newSubTechnicalContent = "{\"type\": \"success\"}";
        final FunctionalStep newSub_with_implementation = saveAndReload(
            buildFunctionalStep("sub with implementation", newSubTechnicalContent, subWithImplementation.id));
        String updateFStepId = sut.save(FunctionalStep.builder()
            .withName(newName)
            .withId(fStepId)
            .withSteps(Collections.singletonList(newSub_with_implementation))
            .build()
        );
        FunctionalStep updatedFStep = sut.findById(updateFStepId);

        // Then
        assertThat(fStepId).isEqualTo(updateFStepId);
        assertThat(updatedFStep.name).isEqualTo(newName);
        assertThat(updatedFStep.steps.get(0).implementation.get()).isEqualTo(newSubTechnicalContent);
    }

    @Test
    public void should_find_saved_func_steps_by_page_when_find_called() {
        // Given
        final FunctionalStep fStep_11 = saveAndReload(
            buildFunctionalStep("sub something happen 1.1"));
        final FunctionalStep fStep_12 = saveAndReload(
            buildFunctionalStep("sub something happen 1.2 with implementation", StepUsage.GIVEN, "{\"type\": \"debug\"}"));
        final FunctionalStep fStep_13 = saveAndReload(
            buildFunctionalStep("sub something happen 1.3"));
        final FunctionalStep fStep_1 = saveAndReload(
            buildFunctionalStep("sub something happen 1", fStep_11, fStep_12, fStep_13));
        final FunctionalStep fStep_2 = saveAndReload(
            buildFunctionalStep("another sub something 2 with implementation", "{\"type\": \"debug\"}"));
        final FunctionalStep fStep_3 = saveAndReload(
            buildFunctionalStep("sub something happen 3"));
        final FunctionalStep fStepRoot = saveAndReload(
            buildFunctionalStep("something happen", fStep_1, fStep_2, fStep_3));

        List<FunctionalStep> fSteps = Arrays.asList(fStepRoot, fStep_11, fStep_12, fStep_13, fStep_1, fStep_2, fStep_3);

        // When
        long elementPerPage = 2;
        PaginatedDto<FunctionalStep> foundFStepsPage1 = findWithPagination(1, elementPerPage);
        PaginatedDto<FunctionalStep> foundFStepsPage2 = findWithPagination(3, elementPerPage);
        PaginatedDto<FunctionalStep> foundFStepsPage3 = findWithPagination(5, elementPerPage);
        PaginatedDto<FunctionalStep> foundFStepsPage4 = findWithPagination(7, elementPerPage);

        // Then
        List<FunctionalStep> foundFSteps = new ArrayList<>();
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
        final FunctionalStep fStep_1 = saveAndReload(
            buildFunctionalStep("some thing is set", StepUsage.GIVEN));
        final FunctionalStep fStep_2 = saveAndReload(
            buildFunctionalStep("another thing happens", StepUsage.WHEN, "{\"type\": \"debug\"}"));
        final FunctionalStep fStep_3 = saveAndReload(
            buildFunctionalStep("the result is beauty", StepUsage.THEN));
        final FunctionalStep fStepRoot = saveAndReload(
            buildFunctionalStep("big root thing", fStep_1, fStep_2, fStep_3));

        // When
        PaginatedDto<FunctionalStep> filteredByNameFSteps = findWithFilters("beauty", null, null);
        PaginatedDto<FunctionalStep> sortedFSteps = findWithFilters("", "name", null);
        PaginatedDto<FunctionalStep> sortedDescendentFSteps = findWithFilters("", "name", "");
        PaginatedDto<FunctionalStep> filteredFSteps = findWithFilters("thing", "name", "name");

        // Then
        assertThat(filteredByNameFSteps.data()).containsExactly(fStep_3);
        assertThat(sortedFSteps.data()).containsExactly(fStep_2, fStepRoot, fStep_1, fStep_3);
        assertThat(sortedDescendentFSteps.data()).containsExactly(fStep_3, fStep_1, fStepRoot, fStep_2);
        assertThat(filteredFSteps.data()).containsExactly(fStep_1, fStepRoot, fStep_2);
    }

    @Test
    public void should_find_most_identical_func_steps_ordered_when_search_by_name() {
        // Given
        FunctionalStep fStep_1 = saveAndReload(
            buildFunctionalStep("I am a wonderful sub functional step"));
        FunctionalStep fStep_2 = saveAndReload(
            buildFunctionalStep("I'm the best sub functional step ever written"));
        FunctionalStep fStep_3 = saveAndReload(
            buildFunctionalStep("Another sub for special *escaping test, best"));
        FunctionalStep fStepRoot = saveAndReload(
            buildFunctionalStep("This is a nice root functional step", fStep_1, fStep_2, fStep_3));

        // When
        List<FunctionalStep> mostIdenticalRootFuncSteps = sut.queryByName("this root");
        List<FunctionalStep> mostIdenticalSubFuncSteps = sut.queryByName("best sub");
        List<FunctionalStep> mostIdenticalFuncSteps = sut.queryByName("step functional");
        List<FunctionalStep> mostIdenticalEscapeSteps = sut.queryByName("*escaping");

        // Then
        assertThat(mostIdenticalRootFuncSteps).extracting("name").containsExactlyInAnyOrder(fStepRoot.name);
        assertThat(mostIdenticalSubFuncSteps).extracting("name").containsExactlyInAnyOrder(fStep_2.name, fStep_3.name);
        assertThat(mostIdenticalFuncSteps).extracting("name").containsExactlyInAnyOrder(fStep_1.name, fStepRoot.name, fStep_2.name);
        assertThat(mostIdenticalEscapeSteps).extracting("name").containsExactlyInAnyOrder(fStep_3.name);
    }

    @Test
    public void should_find_at_most_10_most_identical_func_steps__when_search_by_name() {
        // Given
        List<FunctionalStep> subFSteps = new ArrayList<>();
        IntStream.range(1, 15).forEach(num -> {
            final FunctionalStep subF = saveAndReload(FunctionalStep.builder()
                .withName("I am the sub functional step n°" + num)
                .build());
            subFSteps.add(subF);
        });
        FunctionalStep fStepRoot = FunctionalStep.builder()
            .withName("This is a root functional step")
            .withSteps(subFSteps)
            .build();

        sut.save(fStepRoot);

        // When
        List<FunctionalStep> mostIdenticalFuncSteps = sut.queryByName("functional step");

        // Then
        assertThat(mostIdenticalFuncSteps.size()).isEqualTo(10);
    }

    @Test
    public void should_not_save_func_step_when_cyclic_dependency_found() {
        // Given
        FunctionalStep fStep = saveAndReload(buildFunctionalStep("that"));
        FunctionalStep fStepToUdpate = FunctionalStep.builder().from(fStep).withSteps(Collections.singletonList(fStep)).build();

        // When
        try {
            sut.save(fStepToUdpate);
        } catch (Exception e) {
            // Then
            assertThat(e).isInstanceOf(FunctionalStepCyclicDependencyException.class);
            assertThat(e.getMessage()).contains(fStep.name);
            return;
        }
        Assertions.fail("Should throw FunctionalStepCyclicDependencyException !!");
    }

    @Test
    public void should_find_func_step_parents_when_asked_for() {
        // Given
        FunctionalStep fStep_111 = saveAndReload(
            buildFunctionalStep("that 1"));
        FunctionalStep fStep_112 = saveAndReload(
            buildFunctionalStep("that 2"));
        FunctionalStep fStep_11 = saveAndReload(
            buildFunctionalStep("that", fStep_111, fStep_112));
        FunctionalStep fStep_12 = saveAndReload(
            buildFunctionalStep("inner that", fStep_11));
        FunctionalStep fStepRoot_1 = saveAndReload(
            buildFunctionalStep("a thing", fStep_11, fStep_12, fStep_11));
        FunctionalStep fStep_21 = saveAndReload(
            buildFunctionalStep("this", fStep_11));

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
        final FunctionalStep actionStep = saveAndReload(
            buildFunctionalStep(
                "action with parameters",
                actionParameters
            )
        );

        Map<String, String> firstActionInstanceDataSet = Maps.of(
            "action parameter with default value", "first action instance parameter value",
            "action parameter with no default value", "",
            "another action parameter with default value", "another default action parameter value");
        final FunctionalStep firstActionStepInstance = FunctionalStep.builder()
            .from(actionStep)
            .overrideDataSetWith(firstActionInstanceDataSet)
            .build();

        Map<String, String> secondActionInstanceDataSet = Maps.of(
            "action parameter with default value", "default action parameter value",
            "action parameter with no default value", "second action instance not default value value",
            "another action parameter with default value", "");
        final FunctionalStep secondActionStepInstance = FunctionalStep.builder()
            .from(actionStep)
            .overrideDataSetWith(secondActionInstanceDataSet)
            .build();

        Map<String, String> middleParentParameters = Maps.of(
            "middle parent parameter with default value", "default middle parent parameter value",
            "middle parent parameter with not default value", "",
            "another middle parent parameter with default value", "another default middle parent parameter value");
        final FunctionalStep middleParentStep = saveAndReload(
            buildFunctionalStep(
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
        final FunctionalStep firstMiddleParentStepInstance = FunctionalStep.builder()
            .from(middleParentStep)
            .overrideDataSetWith(firstMiddleParentInstanceDataSet)
            .build();

        Map<String, String> secondMiddleParentInstanceDataSet = Maps.of(
            "action parameter with no default value", "",
            "another action parameter with default value", "second middle parent action value",
            "middle parent parameter with default value", "second middle parent parameter value",
            "middle parent parameter with not default value", "",
            "another middle parent parameter with default value", "another second middle parent parameter value"
        );
        final FunctionalStep secondMiddleParentStepInstance = FunctionalStep.builder()
            .from(middleParentStep)
            .overrideDataSetWith(secondMiddleParentInstanceDataSet)
            .build();

        Map<String, String> thirdActionInstanceDataSet = Maps.of(
            "action parameter with default value", "third action instance parameter value",
            "action parameter with no default value", "yet another third action instance parameter value",
            "another action parameter with default value", "another third action instance parameter value");
        final FunctionalStep thirdActionStepInstance = FunctionalStep.builder()
            .from(actionStep)
            .overrideDataSetWith(thirdActionInstanceDataSet)
            .build();

        Map<String, String> parentParameters = Maps.of(
            "parent parameter", "parent parameter default value");
        final FunctionalStep parentStep = saveAndReload(
            buildFunctionalStep(
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
        FunctionalStep foundAction = sut.findById(actionStep.id);
        FunctionalStep foundMiddleParentFStep = sut.findById(middleParentStep.id);
        FunctionalStep foundParentFStep = sut.findById(parentStep.id);

        // Then
        assertThat(foundAction.parameters).containsExactlyEntriesOf(actionParameters);
        assertThat(foundAction.dataSet).containsExactlyEntriesOf(actionParameters);

        assertThat(foundMiddleParentFStep.steps.get(0).parameters).containsExactlyEntriesOf(actionParameters);
        assertThat(foundMiddleParentFStep.steps.get(0).dataSet).containsExactlyEntriesOf(firstActionInstanceDataSet);
        assertThat(foundMiddleParentFStep.steps.get(1).parameters).containsExactlyEntriesOf(actionParameters);
        assertThat(foundMiddleParentFStep.steps.get(1).dataSet).containsExactlyEntriesOf(secondActionInstanceDataSet);
        assertThat(foundMiddleParentFStep.parameters).containsExactlyEntriesOf(middleParentParameters);
        assertThat(foundMiddleParentFStep.dataSet).containsExactlyEntriesOf(middleParentExpectedDataSet);

        assertThat(foundParentFStep.parameters).containsExactlyEntriesOf(parentParameters);
        assertThat(foundParentFStep.dataSet).containsExactlyEntriesOf(parentExpectedDataSet);
        assertThat(foundParentFStep.steps.get(0).parameters).containsExactlyEntriesOf(middleParentParameters);
        assertThat(foundParentFStep.steps.get(0).dataSet).containsExactlyEntriesOf(firstMiddleParentInstanceDataSet);
        assertThat(foundParentFStep.steps.get(1).parameters).containsExactlyEntriesOf(actionParameters);
        assertThat(foundParentFStep.steps.get(1).dataSet).containsExactlyEntriesOf(thirdActionInstanceDataSet);
        assertThat(foundParentFStep.steps.get(2).parameters).containsExactlyEntriesOf(middleParentParameters);
        assertThat(foundParentFStep.steps.get(2).dataSet).containsExactlyEntriesOf(secondMiddleParentInstanceDataSet);
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
        FunctionalStep step = saveAndReload(
            buildFunctionalStep("my step with parameters", stepParameters));

        FunctionalStep parentWithNoParametersOverload = saveAndReload(
            buildFunctionalStep("parent with no parameters values overload", step)
        );

        Map<String, String> stepInsatnceDataSet = Maps.of(
            deleted_param, "parent value",
            new_value_param, "new parent value");
        FunctionalStep stepInstance = FunctionalStep.builder()
            .from(step)
            .overrideDataSetWith(stepInsatnceDataSet)
            .build();
        FunctionalStep parentWithParametersOverload = saveAndReload(
            buildFunctionalStep("parent with parameters values overload", stepInstance)
        );

        // When
        Map<String, String> newStepParameters = Maps.of(
            new_value_param, "another value",
            new_param, "new value");
        FunctionalStep stepUpdate = FunctionalStep.builder()
            .from(step)
            .withParameters(newStepParameters)
            .build();
        sut.save(stepUpdate);

        // Then
        parentWithNoParametersOverload = findByName(parentWithNoParametersOverload.name);
        parentWithParametersOverload = findByName(parentWithParametersOverload.name);

        assertThat(parentWithNoParametersOverload.steps.get(0).parameters).isEqualTo(newStepParameters);
        assertThat(parentWithNoParametersOverload.steps.get(0).dataSet).isEqualTo(newStepParameters);
        assertThat(parentWithNoParametersOverload.steps.get(0).parameters).isEqualTo(newStepParameters);
        assertThat(parentWithParametersOverload.steps.get(0).dataSet).containsExactly(
            new AbstractMap.SimpleEntry<>(new_value_param, stepInsatnceDataSet.get(new_value_param)),
            new AbstractMap.SimpleEntry<>(new_param, newStepParameters.get(new_param))
        );
    }

    @Test
    public void should_delete_step_and_update_edges_when_deleteById_called() {
        // Given
        final FunctionalStep step = saveAndReload(
            buildFunctionalStep("a step", Maps.of("param", "default value")));

        final FunctionalStep stepInstance = FunctionalStep.builder()
            .from(step)
            .overrideDataSetWith(Maps.of("param", ""))
            .build();

        final FunctionalStep stepInstanceB = FunctionalStep.builder()
            .from(step)
            .overrideDataSetWith(Maps.of("param", "hard value"))
            .build();

        final FunctionalStep parentFStep = saveAndReload(
            buildFunctionalStep("a parent step", stepInstance, stepInstanceB)
        );

        // When
        sut.deleteById(step.id);
        FunctionalStep parentFoundStep = sut.findById(parentFStep.id);

        // Then
        assertThat(loadById(step.id)).isNull();
        assertThat(parentFoundStep.steps).isEmpty();
    }


    @Test
    public void changelog_n5_should_update_selenium_tasks() {
        // G
        final FunctionalStep step = saveAndReload(
            buildFunctionalStep("selenium-get", StepUsage.THEN, "{\n" +
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
        final FunctionalStep stepOld = saveAndReload(
            buildFunctionalStep("selenium-get-old", StepUsage.THEN, "{\n" +
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
        FunctionalStep step1 = sut.findById(step.id);
        assertThat(step1.implementation.get()).doesNotContain("name\":\"action", "name\":\"value", "name\":\"switchType", "name\":\"menuItemSelector");
        FunctionalStep stepO = sut.findById(stepOld.id);
        assertThat(stepO.implementation.get()).doesNotContain("action", "value", "switchType", "menuItemSelector");
    }

    private FunctionalStep saveAndReload(FunctionalStep functionalStep) {
        return saveAndReload(sut, functionalStep);
    }

    private void assertScenarioRids(List<FunctionalStep> scenario, List<String> rids) {
        IntStream.range(0, rids.size())
            .forEach(idx -> {
                String rid = rids.get(idx);
                FunctionalStep fStep = sut.findById(rid);
                assertThat(fStep.id).isEqualTo(rid);
                assertFunctionalStep(scenario.get(idx), fStep);
            });
    }

    private void assertFunctionalStep(FunctionalStep expectedFStep, FunctionalStep step) {
        assertFunctionalStep(
            step,
            expectedFStep.name,
            expectedFStep.usage.orElseThrow(() -> new IllegalStateException("Usage should be set")),
            expectedFStep.implementation,
            expectedFStep.steps.size());

        IntStream.range(0, expectedFStep.steps.size()).forEach(
            idx -> {
                FunctionalStep subStep = step.steps.get(idx);
                assertFunctionalStep(expectedFStep.steps.get(idx), subStep);
            }
        );
    }

    private void assertFunctionalStep(FunctionalStep step, String name, StepUsage usage, Optional<String> implementation, int functionalChildStepsSize) {
        assertThat(step).isInstanceOf(FunctionalStep.class);
        assertThat(step.name).isEqualTo(name);

        if (implementation.isPresent()) {
            assertThat(step.implementation.orElseThrow(() -> new IllegalStateException("Implementation should be set"))).isEqualTo(implementation.get());
        } else {
            assertThat(step.implementation.isPresent()).isFalse();
        }
        assertThat(step.id).isNotEmpty();
        assertThat(step.usage.get()).isEqualTo(usage);
        assertThat(step.steps.size()).isEqualTo(functionalChildStepsSize);
    }

    private PaginatedDto<FunctionalStep> findWithPagination(long startElementIdx, long limit) {
        return sut.find(
            ImmutablePaginationRequestParametersDto.builder()
                .start(startElementIdx)
                .limit(limit)
                .build(),
            ImmutableSortRequestParametersDto.builder()
                .build(),
            FunctionalStep.builder()
                .withName("")
                .withSteps(Collections.emptyList())
                .build()
        );
    }

    private PaginatedDto<FunctionalStep> findWithFilters(String name, String sort, String desc) {
        return sut.find(
            ImmutablePaginationRequestParametersDto.builder()
                .start(1L)
                .limit(100L)
                .build(),
            ImmutableSortRequestParametersDto.builder()
                .sort(sort)
                .desc(desc)
                .build(),
            FunctionalStep.builder()
                .withName(name)
                .withSteps(Collections.emptyList())
                .build()
        );
    }
}
