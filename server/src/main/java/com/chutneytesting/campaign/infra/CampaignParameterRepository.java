package com.chutneytesting.campaign.infra;

import static java.util.Collections.emptyMap;

import com.google.common.collect.ImmutableMap;
import java.util.List;
import java.util.Map;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
class CampaignParameterRepository {

    private final RowMapper<CampaignParameter> campaignParamaterRowMapper;
    private final NamedParameterJdbcTemplate uiNamedParameterJdbcTemplate;

    CampaignParameterRepository(NamedParameterJdbcTemplate uiNamedParameterJdbcTemplate) {
        this.uiNamedParameterJdbcTemplate = uiNamedParameterJdbcTemplate;
        campaignParamaterRowMapper = (resultSet, i) -> {
            Long campaignId = resultSet.getLong(1);
            String parameter = resultSet.getString(2);
            String value = resultSet.getString(3);
            return new CampaignParameter(campaignId, parameter, value);
        };
    }

    private static final String QUERY_FIND_CAMPAIGN_PARAMETERS =
        "SELECT CAMPAIGN_ID, PARAMETER, PARAMETER_VALUE "
            + "FROM CAMPAIGN_PARAMETER "
            + "WHERE CAMPAIGN_ID = :campaignId "
            + "ORDER BY ID DESC";

    List<CampaignParameter> findCampaignParameters(Long campaignId) {
        return uiNamedParameterJdbcTemplate.query(QUERY_FIND_CAMPAIGN_PARAMETERS,
                                                ImmutableMap.of("campaignId", campaignId),
            campaignParamaterRowMapper);
    }

    private static final String QUERY_SAVE_CAMPAIGN_PARAMETER =
        "INSERT INTO CAMPAIGN_PARAMETER(ID, CAMPAIGN_ID, PARAMETER, PARAMETER_VALUE) "
            + "VALUES (:id, :campaignId, :parameter, :value)";

    public void updateCampaignParameter(Long campaignId, Map<String,String> data) {
        clearAllCampaignParameters(campaignId);
        data.forEach((k,v) ->
            saveCampaignParameter(new CampaignParameter(campaignId, k, v))
        );
    }

    void clearAllCampaignParameters(Long campaignId) {
        uiNamedParameterJdbcTemplate.update("DELETE FROM CAMPAIGN_PARAMETER WHERE CAMPAIGN_ID = :campaignId", ImmutableMap.of("campaignId", campaignId));
    }

    private int saveCampaignParameter(CampaignParameter campaignParameter) {
        return uiNamedParameterJdbcTemplate.update(QUERY_SAVE_CAMPAIGN_PARAMETER,
            ImmutableMap.of(
                "id", generateCampaignParameterId(),
                "campaignId", campaignParameter.campaignId,
                "parameter", campaignParameter.parameter,
                "value", campaignParameter.value == null ? "" : campaignParameter.value));
    }

    private Long generateCampaignParameterId() {
        return uiNamedParameterJdbcTemplate.queryForObject("SELECT nextval('CAMPAIGN_PARAMETER_SEQ')", emptyMap(), Long.class);
    }


}
