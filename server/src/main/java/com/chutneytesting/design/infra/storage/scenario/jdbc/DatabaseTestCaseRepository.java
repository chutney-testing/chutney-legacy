package com.chutneytesting.design.infra.storage.scenario.jdbc;

import static com.chutneytesting.design.domain.scenario.TestCaseRepository.DEFAULT_REPOSITORY_SOURCE;
import static java.time.temporal.ChronoUnit.MILLIS;

import com.chutneytesting.design.domain.scenario.TestCaseMetadata;
import com.chutneytesting.design.domain.scenario.TestCaseMetadataImpl;
import com.chutneytesting.design.infra.storage.scenario.DelegateScenarioRepository;
import com.chutneytesting.instrument.domain.Metrics;
import com.chutneytesting.tools.Try;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class DatabaseTestCaseRepository implements DelegateScenarioRepository {

    private static final ScenarioMetadataRowMapper SCENARIO_INDEX_ROW_MAPPER = new ScenarioMetadataRowMapper();
    private final ScenarioRowMapper scenario_row_mapper;

    private final NamedParameterJdbcTemplate uiNamedParameterJdbcTemplate;
    private final Metrics metrics;
    private final ObjectMapper mapper;

    public DatabaseTestCaseRepository(NamedParameterJdbcTemplate uiNamedParameterJdbcTemplate,
                                      Metrics metrics,
                                      @Qualifier("persistenceObjectMapper") ObjectMapper objectMapper) {

        this.uiNamedParameterJdbcTemplate = uiNamedParameterJdbcTemplate;
        this.metrics = metrics;
        this.mapper = objectMapper;
        this.scenario_row_mapper = new ScenarioRowMapper(mapper);
    }

    @Override
    public String alias() {
        return DEFAULT_REPOSITORY_SOURCE;
    }

    @Override
    public String save(TestCaseData scenario) {
        if (isNewScenario(scenario)) {
            return doSave(scenario);
        }
        return doUpdate(scenario);
    }

    @Override
    public Optional<TestCaseData> findById(String scenarioId) {
        try {
            return Optional.of(uiNamedParameterJdbcTemplate.queryForObject("SELECT * FROM SCENARIO WHERE ID = :id and ACTIVATED = TRUE", ImmutableMap.<String, Object>builder().put("id", scenarioId).build(), scenario_row_mapper));
        } catch (IncorrectResultSizeDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public List<TestCaseMetadata> findAll() {
        return uiNamedParameterJdbcTemplate.query("SELECT ID, TITLE, DESCRIPTION, TAGS, CREATION_DATE FROM SCENARIO where ACTIVATED is TRUE", Collections.emptyMap(), SCENARIO_INDEX_ROW_MAPPER);
    }

    @Override
    public void removeById(String scenarioId) {
        // TODO - Refactor - Use CampaignRepository up in callstack
        uiNamedParameterJdbcTemplate.update("DELETE FROM CAMPAIGN_EXECUTION_HISTORY WHERE SCENARIO_ID = :id", ImmutableMap.<String, Object>builder().put("id", scenarioId).build());
        uiNamedParameterJdbcTemplate.update("DELETE FROM CAMPAIGN_SCENARIOS WHERE SCENARIO_ID = :id", ImmutableMap.<String, Object>builder().put("id", scenarioId).build());
        uiNamedParameterJdbcTemplate.update("UPDATE SCENARIO SET ACTIVATED = FALSE WHERE ID = :id", ImmutableMap.<String, Object>builder().put("id", scenarioId).build());
    }

    private boolean isNewScenario(TestCaseData scenario) {
        return scenario.id == null || uiNamedParameterJdbcTemplate.queryForObject("SELECT COUNT(ID) FROM SCENARIO WHERE ID = :id", ImmutableMap.<String, Object>builder().put("id", scenario.id).build(), int.class) == 0;
    }

    private String doSave(TestCaseData scenario) {
        String nextId = uiNamedParameterJdbcTemplate.queryForObject("SELECT nextval('SCENARIO_SEQ')", Collections.emptyMap(), String.class);
        uiNamedParameterJdbcTemplate.update("INSERT INTO SCENARIO(VERSION, ID, TITLE, DESCRIPTION, CONTENT, TAGS, CREATION_DATE, DATASET, ACTIVATED) VALUES (:version, :id, :title, :description, :content, :tags, :creationDate, :dataSet, TRUE)",
            scenarioQueryParameterMap(nextId, scenario));
        metrics.onNewScenario(scenario.title, scenario.tags);
        return nextId;
    }

    private String doUpdate(TestCaseData scenario) {
        uiNamedParameterJdbcTemplate.update("UPDATE SCENARIO SET VERSION = :version, TITLE = :title, DESCRIPTION = :description, CONTENT = :content, TAGS = :tags, CREATION_DATE = :creationDate, DATASET = :dataSet WHERE ID = :id",
            scenarioQueryParameterMap(scenario.id, scenario));
        metrics.onScenarioChange(scenario.title, scenario.tags);
        return scenario.id;
    }

    private ImmutableMap<String, Object> scenarioQueryParameterMap(String nextId, TestCaseData scenario) {
        return Try.exec(() -> {
            return ImmutableMap.<String, Object>builder()
                .put("version", scenario.version)
                .put("id", nextId)
                .put("title", scenario.title)
                .put("description", scenario.description)
                .put("dataSet", mapper.writeValueAsString(scenario.dataSet))
                .put("content", scenario.rawScenario)
                .put("creationDate", Date.from(scenario.creationDate))
                .put("tags", ScenarioTagListMapper.tagsListToString(scenario.tags))
                .build();
            }).runtime();
    }

    private static class ScenarioMetadataRowMapper implements RowMapper<TestCaseMetadata> {
        @Override
        public TestCaseMetadata mapRow(ResultSet rs, int rowNum) throws SQLException {
            String id = rs.getString("ID");
            String title = rs.getString("TITLE");
            String description = rs.getString("DESCRIPTION");
            Timestamp creationDate = rs.getTimestamp("CREATION_DATE");
            List<String> tags = ScenarioTagListMapper.tagsStringToList(rs.getString("TAGS"));
            return TestCaseMetadataImpl.builder()
                .withId(id)
                .withTitle(title)
                .withDescription(description)
                .withTags(tags)
                .withCreationDate(creationDate != null ? creationDate.toInstant() : Instant.now().truncatedTo(MILLIS))
                .build();
        }
    }

    private static class ScenarioRowMapper implements RowMapper<TestCaseData> {
        private final ObjectMapper mapper;

        private ScenarioRowMapper(ObjectMapper mapper) {
            this.mapper = mapper;
        }

        @Override
        public TestCaseData mapRow(ResultSet rs, int rowNum) throws SQLException {
            TestCaseData.TestCaseDataBuilder testCaseDataBuilder = TestCaseData.builder()
                .withVersion(rs.getString("VERSION"))
                .withId(rs.getString("ID"))
                .withTitle(rs.getString("TITLE"))
                .withDescription(rs.getString("DESCRIPTION"))
                .withTags(ScenarioTagListMapper.tagsStringToList(rs.getString("TAGS")))
                .withRawScenario(rs.getString("CONTENT"));

            Try.exec(() ->  {
                TypeReference<Map<String, String>> typeRef = new TypeReference<Map<String, String>>() {};
                String dataSet = rs.getString("DATASET");
                return testCaseDataBuilder.withDataSet(mapper.readValue(dataSet != null ? dataSet : "{}", typeRef));
            }).runtime();

            Timestamp creationDate = rs.getTimestamp("CREATION_DATE");
            testCaseDataBuilder.withCreationDate(creationDate != null ? creationDate.toInstant() : Instant.now().truncatedTo(MILLIS));
            return testCaseDataBuilder.build();
        }
    }

}
