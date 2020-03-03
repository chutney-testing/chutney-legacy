package com.chutneytesting.design.infra.storage.campaign;

import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;
import static java.util.Collections.singletonList;

import com.chutneytesting.design.domain.campaign.CampaignExecutionReport;
import com.chutneytesting.design.domain.campaign.ScenarioExecutionReportCampaign;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import java.util.HashMap;
import java.util.List;
import org.springframework.jdbc.core.SingleColumnRowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
class CampaignExecutionRepository {

    private final CampaignExecutionReportMapper campaignExecutionReportMapper;
    private final NamedParameterJdbcTemplate uiNamedParameterJdbcTemplate;
    private static final int LIMIT_BLOC_SIZE = 20;

    CampaignExecutionRepository(NamedParameterJdbcTemplate uiNamedParameterJdbcTemplate) {
        this.uiNamedParameterJdbcTemplate = uiNamedParameterJdbcTemplate;
        campaignExecutionReportMapper = new CampaignExecutionReportMapper();
    }

    private static final String QUERY_FIND_CAMPAIGN_EXECUTION_HISTORY =
        "SELECT C.CAMPAIGN_ID, C.ID, C.SCENARIO_ID, C.SCENARIO_EXECUTION_ID, C.PARTIAL_EXECUTION, C.EXECUTION_ENVIRONMENT, SEH.TEST_CASE_TITLE, "
            + "SEH.EXECUTION_TIME, SEH.DURATION, SEH.STATUS, SEH.INFORMATION, SEH.ERROR, CA.TITLE as CAMPAIGN_TITLE "
            + "FROM CAMPAIGN_EXECUTION_HISTORY C "
            + "INNER JOIN SCENARIO_EXECUTION_HISTORY SEH ON SEH.ID = C.SCENARIO_EXECUTION_ID "
            + "INNER JOIN CAMPAIGN CA ON CA.ID = C.CAMPAIGN_ID "
            + "WHERE C.CAMPAIGN_ID = :idCampaign "
            + "AND C.ID IN ("
            +       "SELECT DISTINCT CC.ID as ID "
            +       "FROM CAMPAIGN_EXECUTION_HISTORY CC "
            +       "WHERE CC.CAMPAIGN_ID = :idCampaign "
            +       "ORDER BY 1 DESC "
            +       "FETCH FIRST " + LIMIT_BLOC_SIZE + " ROWS ONLY"
            + ") "
            + "ORDER BY C.ID DESC";

    List<CampaignExecutionReport> findExecutionHistory(Long campaignId) {
        return uiNamedParameterJdbcTemplate.query(QUERY_FIND_CAMPAIGN_EXECUTION_HISTORY,
                                                  ImmutableMap.of("idCampaign", campaignId),
                                                  campaignExecutionReportMapper);
    }

    void saveCampaignReport(Long campaignId, CampaignExecutionReport report) {
        report.scenarioExecutionReports().forEach(
            scenarioExecutionReport -> saveScenarioExecutionReport(campaignId, report.executionId, report.partialExecution, scenarioExecutionReport, report.executionEnvironment)
        );
    }

    private static final String QUERY_FIND_LAST_EXECUTION_HISTORY = "select distinct C.ID " +
        "from CAMPAIGN_EXECUTION_HISTORY C " +
        "order by C.ID desc " +
        "LIMIT :numberexec";

    private static final String QUERY_FIND_EXECUTION_BY_EXEC_ID = "SELECT C.CAMPAIGN_ID, C.ID, C.SCENARIO_ID, C.SCENARIO_EXECUTION_ID, C.PARTIAL_EXECUTION, C.EXECUTION_ENVIRONMENT, SEH.TEST_CASE_TITLE, " +
        "SEH.EXECUTION_TIME, SEH.DURATION, SEH.STATUS, SEH.INFORMATION, SEH.ERROR, CA.TITLE as CAMPAIGN_TITLE " +
        "FROM CAMPAIGN_EXECUTION_HISTORY C " +
        "INNER JOIN SCENARIO_EXECUTION_HISTORY SEH ON SEH.ID = C.SCENARIO_EXECUTION_ID " +
        "INNER JOIN CAMPAIGN CA ON CA.ID = C.CAMPAIGN_ID " +
        "WHERE C.ID in (:idCampaignExecution) " +
        "ORDER BY C.ID DESC";
    List<CampaignExecutionReport> findLastExecutions(Long numberOfExecution) {
        List<Long> campaignExecIds = uiNamedParameterJdbcTemplate.query(QUERY_FIND_LAST_EXECUTION_HISTORY.replace(":numberexec", numberOfExecution.toString()),
            emptyMap(),
            new SingleColumnRowMapper<>());

        if (campaignExecIds.isEmpty()) {
            return emptyList();
        }

        return getCampaignExecutionReportsById(campaignExecIds);
    }

    CampaignExecutionReport getCampaignExecutionReportsById(Long campaignExecIds) {
        return getCampaignExecutionReportsById(singletonList(campaignExecIds)).get(0);
    }

    private List<CampaignExecutionReport> getCampaignExecutionReportsById(List<Long> campaignExecIds) {
        return uiNamedParameterJdbcTemplate.query(QUERY_FIND_EXECUTION_BY_EXEC_ID,
            ImmutableMap.of("idCampaignExecution", campaignExecIds),
            campaignExecutionReportMapper);
    }

    private static final String QUERY_DELETE_ALL_CAMPAIGN_EXECUTION_HISTORY =
        "DELETE FROM CAMPAIGN_EXECUTION_HISTORY "
            + "WHERE CAMPAIGN_ID = :idCampaign";

    void clearAllExecutionHistory(Long campaignId) {
            uiNamedParameterJdbcTemplate.update(QUERY_DELETE_ALL_CAMPAIGN_EXECUTION_HISTORY,
                                                ImmutableMap.of("idCampaign", campaignId));
    }

    private static final String QUERY_SAVE_CAMPAIGN_EXECUTION_HISTORY =
        "INSERT INTO CAMPAIGN_EXECUTION_HISTORY(CAMPAIGN_ID, ID, SCENARIO_ID, SCENARIO_EXECUTION_ID, PARTIAL_EXECUTION, EXECUTION_ENVIRONMENT) "
            + "VALUES (:idCampaign, :idCampaignExecution, :idScenario, :idScenarioExecution, :partialExecution, :executionEnvironment)";

    private int saveScenarioExecutionReport(Long campaignId, Long campaignExecutionId, boolean partialExecution, ScenarioExecutionReportCampaign scenarioExecutionReport, String executionEnvironment) {
        HashMap<String, Object> parameters = Maps.newHashMap();
        parameters.put("idCampaign", campaignId);
        parameters.put("idCampaignExecution", campaignExecutionId);
        parameters.put("idScenario", scenarioExecutionReport.scenarioId);
        parameters.put("idScenarioExecution", scenarioExecutionReport.execution.executionId());
        parameters.put("partialExecution", partialExecution);
        parameters.put("executionEnvironment", executionEnvironment);
        return uiNamedParameterJdbcTemplate.update(QUERY_SAVE_CAMPAIGN_EXECUTION_HISTORY,parameters);
    }

    Long generateCampaignExecutionId() {
        return uiNamedParameterJdbcTemplate.queryForObject("SELECT nextval('CAMPAIGN_EXECUTION_SEQ')", emptyMap(), Long.class);
    }
}
