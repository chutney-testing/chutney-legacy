package com.chutneytesting.design.infra.storage.campaign;

import static java.util.Collections.emptyMap;
import static java.util.Optional.ofNullable;

import com.google.common.collect.ImmutableMap;
import com.chutneytesting.design.domain.campaign.Campaign;
import com.chutneytesting.design.domain.campaign.CampaignExecutionReport;
import com.chutneytesting.design.domain.campaign.CampaignNotFoundException;
import com.chutneytesting.design.domain.campaign.CampaignRepository;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

/**
 * Campaign persistence management.
 */
@Repository
public class DatabaseCampaignRepository implements CampaignRepository {
    private static final CampaignRepositoryRowMapper CAMPAIGN_ENTITY_ROW_MAPPER = new CampaignRepositoryRowMapper();

    private final NamedParameterJdbcTemplate uiNamedParameterJdbcTemplate;
    private final CampaignExecutionRepository campaignExecutionRepository;
    private final CampaignParameterRepository campaignParameterRepository;

    public DatabaseCampaignRepository(NamedParameterJdbcTemplate uiNamedParameterJdbcTemplate,
                                      CampaignExecutionRepository campaignExecutionRepository,
                                      CampaignParameterRepository campaignParameterRepository) {
        this.uiNamedParameterJdbcTemplate = uiNamedParameterJdbcTemplate;
        this.campaignExecutionRepository = campaignExecutionRepository;
        this.campaignParameterRepository = campaignParameterRepository;
    }

    /**
     * Insert a new campaign into the data base, or update existing one.
     *
     * @param campaign The campaign to add or update.
     * @return Inserted instance.
     */
    @Override
    public Campaign createOrUpdate(Campaign campaign) {
        final Long id;
        if (!isCampaignExists(campaign.id)) {
            id = doSave(campaign);
        } else {
            id = doUpdate(campaign);
        }
        return findById(id);
    }

    @Override
    public void saveReport(Long campaignId, CampaignExecutionReport report) {
        campaignExecutionRepository.saveCampaignReport(campaignId, report);
    }

    /**
     * Remove a campaign from its id.
     *
     * @param id The campaign id to remove.
     * @return <tt>true</tt> if, and only if, the campaign has been removed.
     */
    @Override
    public boolean removeById(Long id) {
        if (isCampaignExists(id)) {
            campaignExecutionRepository.clearAllExecutionHistory(id);
            clearAllAssociationToScenario(id);
            campaignParameterRepository.clearAllCampaignParameters(id);
            deleteCampaign(id);
            return true;
        }

        return false;
    }

    /**
     * Find a campaign from its ID.
     *
     * @param campaignId The campaign id to retrieve.
     * @return Found campaign.
     * @throws CampaignNotFoundException This exception is throws if no campaign is found.
     */
    @Override
    public Campaign findById(Long campaignId) throws CampaignNotFoundException {
        Campaign campaign = findByIdWithoutExecutions(campaignId);

        Map<String, String> parameters = campaignParameterRepository.findCampaignParameters(campaign.id).stream()
            .collect(Collectors.toMap(cp -> cp.parameter, cp -> cp.value));

        return new Campaign(campaign.id, campaign.title, campaign.description, campaign.scenarioIds, parameters, campaign.getScheduleTime(), campaign.executionEnvironment(), campaign.parallelRun, campaign.retryAuto, campaign.datasetId);
    }

    @Override
    public List<Campaign> findByName(String campaignName) {
        String sql = "SELECT C.ID " +
            "FROM CAMPAIGN C " +
            "LEFT JOIN CAMPAIGN_SCENARIOS CS ON CS.CAMPAIGN_ID = C.ID " +
            "WHERE LOWER(C.TITLE) LIKE LOWER(:campaignName) " +
            "GROUP BY C.ID ";
        return uiNamedParameterJdbcTemplate.queryForList(sql, ImmutableMap.of("campaignName", campaignName), Long.class)
            .stream()
            .map(this::findById)
            .collect(Collectors.toList());
    }

    @Override
    public List<CampaignExecutionReport> findLastExecutions(Long numberOfExecution) {
        return campaignExecutionRepository.findLastExecutions(numberOfExecution);
    }

    @Override
    public List<String> findScenariosIds(Long campaignId) {
        return uiNamedParameterJdbcTemplate.queryForList("SELECT SCENARIO_ID FROM CAMPAIGN_SCENARIOS WHERE CAMPAIGN_ID = :campaignId order by INDEX asc", ImmutableMap.of("campaignId", campaignId), String.class);
    }

    @Override
    public Long newCampaignExecution() {
        return campaignExecutionRepository.generateCampaignExecutionId();
    }

    @Override
    public List<Campaign> findAll() {
        String sql = "SELECT DISTINCT C.ID FROM CAMPAIGN C";
        return uiNamedParameterJdbcTemplate.queryForList(sql, emptyMap(), Long.class)
            .stream()
            .map(this::findByIdWithoutExecutions) // TODO - Scenario ids are not used
            .collect(Collectors.toList());
    }

    @Override
    public List<CampaignExecutionReport> findExecutionsById(Long campaignId) {
        return campaignExecutionRepository.findExecutionHistory(campaignId);
    }


    @SuppressWarnings("unchecked")
    private Long doUpdate(Campaign campaign) {
        uiNamedParameterJdbcTemplate.update("UPDATE CAMPAIGN SET " +
                "TITLE = :title, " +
                "DESCRIPTION = :description, " +
                "SCHEDULE_TIME = :scheduletime, " +
                "ENVIRONMENT = :environment, " +
                "PARALLEL_RUN = :paralellRun, " +
                "RETRY_AUTO = :retryAuto, " +
                "DATASET_ID = :datasetId " +
                "WHERE ID = :id"
            , map(Pair.of("id", campaign.id)
                , Pair.of("title", campaign.title)
                , Pair.of("description", campaign.description)
                , Pair.of("scheduletime", campaign.getStringScheduleTime())
                , Pair.of("environment", campaign.executionEnvironment())
                , Pair.of("paralellRun", campaign.parallelRun)
                , Pair.of("retryAuto", campaign.retryAuto)
                , Pair.of("datasetId", campaign.datasetId)
            ));

        updateScenarioReferences(campaign.id, campaign.scenarioIds);
        campaignParameterRepository.updateCampaignParameter(campaign.id, campaign.dataSet);
        return campaign.id;
    }

    @SuppressWarnings("unchecked")
    private Long doSave(final Campaign unsavedCampaign) {
        final Long id = uiNamedParameterJdbcTemplate.queryForObject("SELECT nextval('CAMPAIGN_SEQ')", emptyMap(), Long.class);

        uiNamedParameterJdbcTemplate.update("INSERT INTO CAMPAIGN(ID, TITLE, DESCRIPTION, SCHEDULE_TIME, ENVIRONMENT, PARALLEL_RUN, RETRY_AUTO, DATASET_ID) " +
                "VALUES (:id, :title, :description, :scheduletime, :environment, :paralellRun, :retryAuto, :datasetId)"
            , map(Pair.of("id", id)
                , Pair.of("title", unsavedCampaign.title)
                , Pair.of("description", ofNullable(unsavedCampaign.description).orElse(""))
                , Pair.of("scheduletime", unsavedCampaign.getStringScheduleTime())
                , Pair.of("environment", unsavedCampaign.executionEnvironment())
                , Pair.of("paralellRun", unsavedCampaign.parallelRun)
                , Pair.of("retryAuto", unsavedCampaign.retryAuto)
                , Pair.of("datasetId", unsavedCampaign.datasetId)
            ));

        updateScenarioReferences(id, unsavedCampaign.scenarioIds);
        campaignParameterRepository.updateCampaignParameter(id, unsavedCampaign.dataSet);
        return id;
    }

    @SuppressWarnings("unchecked")
    private void updateScenarioReferences(Long campaignId, List<String> scenarioId) {
        clearAllAssociationToScenario(campaignId);
        final AtomicInteger index = new AtomicInteger(0);
        scenarioId.forEach(id -> {
            uiNamedParameterJdbcTemplate
                .update("INSERT INTO CAMPAIGN_SCENARIOS(CAMPAIGN_ID, SCENARIO_ID, INDEX) VALUES (:campaignId, :scenarioId, :index)"
                    , map(Pair.of("campaignId", campaignId)
                        , Pair.of("scenarioId", id)
                        , Pair.of("index", index.incrementAndGet())
                    )
                );
        });
    }

    private boolean isCampaignExists(Long campaignId) {
        return ofNullable(campaignId)
            .map(id -> uiNamedParameterJdbcTemplate.queryForObject("SELECT COUNT(ID) FROM CAMPAIGN WHERE ID = :id"
                , ImmutableMap.<String, Object>builder().put("id", id).build(), int.class) > 0)
            .orElse(false);
    }

    private void deleteCampaign(Long id) {
        uiNamedParameterJdbcTemplate.update("DELETE FROM CAMPAIGN WHERE ID = :id", ImmutableMap.of("id", id));
    }

    private void clearAllAssociationToScenario(Long id) {
        uiNamedParameterJdbcTemplate.update("DELETE FROM CAMPAIGN_SCENARIOS WHERE CAMPAIGN_ID = :campaignId", ImmutableMap.of("campaignId", id));
    }

    private Campaign findByIdWithoutExecutions(Long campaignId) {
        String sql = "SELECT C.* FROM CAMPAIGN C WHERE C.ID = :campaignId ";
        try {
            Campaign campaign = uiNamedParameterJdbcTemplate.queryForObject(sql
                , ImmutableMap.of("campaignId", campaignId)
                , CAMPAIGN_ENTITY_ROW_MAPPER);

            ofNullable(campaign).ifPresent(
                c -> findScenariosIds(c.id).forEach(c::addScenario)
            );

            return campaign;
        } catch (EmptyResultDataAccessException e) {
            throw new CampaignNotFoundException(campaignId);
        }
    }

    public List<Campaign> findCampaignsByScenarioId(String scenarioId) {
        String sqlQuery = "SELECT C.* FROM campaign C INNER JOIN campaign_scenarios ON campaign_scenarios.campaign_id = C.id WHERE scenario_id= :scenarioId";
        return uiNamedParameterJdbcTemplate.query(sqlQuery,
            ImmutableMap.of("scenarioId", scenarioId),
            CAMPAIGN_ENTITY_ROW_MAPPER)
            .stream()
            .collect(Collectors.toList());
    }

    public CampaignExecutionReport findByExecutionId(Long campaignExecutionId) {
        return campaignExecutionRepository.getCampaignExecutionReportsById(campaignExecutionId);
    }

    @SuppressWarnings("unchecked")
    private static <K, V> Map<K, V> map(Pair<K, V>... entries) {
        Map<K, V> ret = new HashMap<>();
        Arrays.stream(entries).forEach(e -> ret.put(e.getKey(), e.getRight()));
        return ret;
    }

    private static class CampaignRepositoryRowMapper implements RowMapper<Campaign> {

        @Override
        public Campaign mapRow(ResultSet rs, int rowNum) throws SQLException {
            Long id = rs.getLong("ID");
            String title = rs.getString("TITLE");
            String description = rs.getString("DESCRIPTION");
            String scheduleTimeAsString = rs.getString("SCHEDULE_TIME");
            String environment = rs.getString("ENVIRONMENT");
            String datasetId = rs.getString("DATASET_ID");
            Boolean parallelRun = rs.getBoolean("PARALLEL_RUN");
            Boolean retryAuto = rs.getBoolean("RETRY_AUTO");
            LocalTime localTime = scheduleTimeAsString != null ? LocalTime.parse(scheduleTimeAsString, Campaign.formatter) : null;
            return new Campaign(id, title, description, null, null, localTime, environment, parallelRun, retryAuto, datasetId);
        }
    }
}
