package com.chutneytesting.design.infra.storage.scenario.jdbc;

import static java.time.temporal.ChronoUnit.MILLIS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;
import static org.mockito.Mockito.mock;

import com.chutneytesting.design.domain.scenario.TestCaseMetadata;
import com.chutneytesting.design.domain.scenario.TestCaseMetadataImpl;
import com.chutneytesting.design.domain.scenario.gwt.GwtScenario;
import com.chutneytesting.design.domain.scenario.gwt.GwtStep;
import com.chutneytesting.design.domain.scenario.gwt.GwtTestCase;
import com.chutneytesting.instrument.domain.Metrics;
import com.chutneytesting.tests.AbstractLocalDatabaseTest;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;
import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.jdbc.core.RowMapper;

@RunWith(JUnitParamsRunner.class)
public class DatabaseTestCaseRepositoryTest extends AbstractLocalDatabaseTest {

    private static final TestCaseData.TestCaseDataBuilder TEST_CASE_DATA_BUILDER = TestCaseData.builder()
        .withVersion("v1.0")
        .withId("0")
        .withTitle("")
        .withCreationDate(Instant.now().truncatedTo(MILLIS))
        .withDescription("")
        .withTags(Collections.emptyList())
        .withDataSet(Collections.emptyMap())
        .withRawScenario("");

    private DatabaseTestCaseRepository repository = new DatabaseTestCaseRepository(namedParameterJdbcTemplate, mock(Metrics.class), new ObjectMapper());

    @Test
    public void should_generate_id_when_scenario_is_persisted() {
        // When: a scenarioTemplate is saved
        String scenarioID = repository.save(TEST_CASE_DATA_BUILDER.build());

        // Then: a non blank id is generated
        assertThat(scenarioID).isNotBlank();
    }

    @Test
    public void should_find_scenario_by_id() {
        // Given: a scenarioTemplate in the repository
        String scenarioID = repository.save(TEST_CASE_DATA_BUILDER.build());


        // When: we look for that scenarioTemplate
        Optional<TestCaseData> optScena = repository.findById(scenarioID);

        // Then: the scenarioTemplate is found
        assertThat(optScena).isPresent();
    }

    @Test
    public void should_retrieve_all_data_of_saved_testCase() {
        // Given: a scenarioTemplate in the repository
        Instant expectedTime = Instant.now().truncatedTo(MILLIS);
        TestCaseData aTestCase = TestCaseDataMapper.toDto(GwtTestCase.builder()
            .withMetadata(TestCaseMetadataImpl.builder()
                .withTitle("A Purpose")
                .withDescription("Description")
                .withTags(Collections.singletonList("TAG"))
                .withCreationDate(expectedTime)
                .build())
            .withScenario(GwtScenario.builder().withWhen(GwtStep.NONE).build())
            .withDataSet(Collections.singletonMap("aKey", "aValue"))
            .build()
        );

        String scenarioID = repository.save(aTestCase);

        // When: we look for that scenarioTemplate
        Optional<TestCaseData> optScena = repository.findById(scenarioID);

        // Then: the scenarioTemplate is found
        assertThat(optScena).isPresent();


        TestCaseData testCaseData = optScena.get();
        assertThat(testCaseData.title).isEqualTo("A Purpose");
        assertThat(testCaseData.description).isEqualTo("Description");
        assertThat(testCaseData.tags).containsExactly("TAG");
        assertThat(testCaseData.creationDate).isEqualTo(expectedTime);
        assertThat(testCaseData.rawScenario).isEqualTo("{\"when\":{}}");
        assertThat(testCaseData.dataSet).containsOnly(entry("aKey", "aValue"));
    }

    @Test
    public void should_not_find_scenario_if_id_is_not_present() {
        // When: we look for a not existed scenarioTemplate
        Optional<TestCaseData> optScenario = repository.findById("0");

        // Then: the scenarioTemplate is not found
        assertThat(optScenario).isEmpty();
    }

    @Test
    public void should_not_find_removed_standalone_scenario_but_still_exist_in_database() {
        // Given: a scenarioTemplate in the repository
        String scenarioID = repository.save(TEST_CASE_DATA_BUILDER.build());

        // When: the scenarioTemplate is removed
        repository.removeById(scenarioID);

        // Then: the scenarioTemplate is not found in the repository
        Optional<TestCaseData> optScena = repository.findById(scenarioID);
        assertThat(optScena).isEmpty();

        RowMapper<String> rowMapper = (rs, rowNum) -> rs.getString("ID");
        List<String> queryResult = namedParameterJdbcTemplate.query("SELECT ID FROM SCENARIO WHERE ID = :id", ImmutableMap.<String, Object>builder().put("id", scenarioID).build(), rowMapper);
        assertThat(queryResult).hasSize(1).containsExactly(scenarioID);
    }

    @Test
    public void should_not_find_removed_scenario_used_in_campaign() {
        // Given: a scenarioTemplate in the repository with campaign association and existing execution
        String scenarioID = repository.save(TEST_CASE_DATA_BUILDER.build());
        createCampaignWithScenarioExecution(1L, scenarioID, 1L, 1L);

        // When: the scenarioTemplate is removed
        repository.removeById(scenarioID);

        // Then: the scenarioTemplate is not found in the repository
        Optional<TestCaseData> optScena = repository.findById(scenarioID);
        assertThat(optScena).isEmpty();
    }

    @Test
    public void should_list_available_scenarios_metadata() {
        // Given: 2 scenarios in the repository
        TestCaseData.TestCaseDataBuilder anotherScenario = TEST_CASE_DATA_BUILDER.withDescription("Will be kept").withTags(Collections.singletonList("T1"));
        TestCaseData.TestCaseDataBuilder deletedScenario = TEST_CASE_DATA_BUILDER.withDescription("Will be deleted").withTags(Collections.singletonList("T2"));

        String anotherScenarioId = repository.save(anotherScenario.build());
        String deletedScenarioId = repository.save(deletedScenario.build());

        // When: the repository content is listed
        repository.removeById(deletedScenarioId);

        // Then: the list contains all repository's scenarios
        List<TestCaseMetadata> allIndexes = repository.findAll();

        assertThat(allIndexes).containsExactlyInAnyOrder(TestCaseDataMapper.fromDto(anotherScenario.withId(anotherScenarioId).build()).metadata());
    }

    public static Object[] parametersForShould_update_scenario_fields() {
        return new Object[]{
            new Object[]{ "Modified title", TEST_CASE_DATA_BUILDER.withTitle("New Title") },
            new Object[]{ "Modified content", TEST_CASE_DATA_BUILDER.withRawScenario("New content") },
            new Object[]{ "Modified description", TEST_CASE_DATA_BUILDER.withDescription("New desc") },
            new Object[]{ "Modified tags", TEST_CASE_DATA_BUILDER.withTags(Arrays.asList("Modif T1", "Modif T2")) } ,
            new Object[]{ "Modified dataSet", TEST_CASE_DATA_BUILDER.withDataSet(Collections.singletonMap("aKey", "aValue")) }
        };
    }

    @Test
    @Parameters
    public void should_update_scenario_fields(String testName, TestCaseData.TestCaseDataBuilder builder) {
        // Given: an existing scenarioTemplate in the repository
        final String scenarioId = repository.save(TEST_CASE_DATA_BUILDER.build());
        TestCaseData modifiedScenario = builder.withId(scenarioId).build();

        // When: the scenarioTemplate is updated in the repository
        repository.save(modifiedScenario);

        // Then: the modified scenarioTemplate is found in the repository
        TestCaseData repositoryScenario = repository.findById(scenarioId).get();

        assertThat(modifiedScenario)
            .as(testName)
            .isEqualToComparingFieldByField(repositoryScenario);
    }


}
