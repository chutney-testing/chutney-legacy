package com.chutneytesting.design.infra.storage.campaign;

import java.time.LocalDateTime;

public class SchedulingCampaignDto {
    public final String id;
    public final Long campaignId;
    public final String campaignTitle;
    public final LocalDateTime schedulingDate;

    /**
     * for ObjectMapper only
     **/
    public SchedulingCampaignDto() {
        id = null;
        campaignId = null;
        schedulingDate = null;
        campaignTitle = null;
    }

    public SchedulingCampaignDto(String id,
                                 Long campaignId,
                                 String campaignTitle,
                                 LocalDateTime schedulingDate) {
        this.id = id;
        this.campaignId = campaignId;
        this.campaignTitle = campaignTitle;
        this.schedulingDate = schedulingDate;
    }
}
