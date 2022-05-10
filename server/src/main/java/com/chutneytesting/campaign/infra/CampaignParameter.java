package com.chutneytesting.campaign.infra;

public class CampaignParameter {

    public final Long campaignId;
    public final String parameter;
    public final String value;

    CampaignParameter(Long campaignId, String parameter, String value) {
        this.campaignId = campaignId;
        this.parameter = parameter;
        this.value = value;
    }

    @Override
    public String toString() {
        return "CampaignParameter{" +
            "id=" + campaignId +
            ", parameter='" + parameter + '\'' +
            ", value='" + value + '\'' +
            '}';
    }
}
