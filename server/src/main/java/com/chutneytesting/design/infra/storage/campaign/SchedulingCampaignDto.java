package com.chutneytesting.design.infra.storage.campaign;

import com.chutneytesting.design.domain.campaign.FREQUENCY;
import java.time.LocalDateTime;

public class SchedulingCampaignDto {
    public final String id;
    public final Long campaignId;
    public final String campaignTitle;
    public final LocalDateTime schedulingDate;
    public final String frequency;

    /**
     * for ObjectMapper only
     **/
    public SchedulingCampaignDto() {
        id = null;
        campaignId = null;
        schedulingDate = null;
        campaignTitle = null;
        frequency = null;
    }

    public SchedulingCampaignDto(String id,
                                 Long campaignId,
                                 String campaignTitle,
                                 LocalDateTime schedulingDate,
                                 String frequency) {
        this.id = id;
        this.campaignId = campaignId;
        this.campaignTitle = campaignTitle;
        this.schedulingDate = schedulingDate;
        this.frequency = frequency;
    }
}
