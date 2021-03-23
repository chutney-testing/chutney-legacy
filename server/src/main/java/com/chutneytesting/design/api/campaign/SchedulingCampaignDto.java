
package com.chutneytesting.design.api.campaign;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDateTime;

@JsonIgnoreProperties(ignoreUnknown = true)
public class SchedulingCampaignDto {

    private Long id;
    private Long campaignId;
    private String campaignTitle;
    private LocalDateTime schedulingDate;
    private String frequency;

    public SchedulingCampaignDto() {
    }

    public SchedulingCampaignDto(@JsonProperty("id") Long id,
                                 @JsonProperty("campaignId") Long campaignId,
                                 @JsonProperty("campaignTitle") String campaignTitle,
                                 @JsonProperty("schedulingDate") LocalDateTime schedulingDate,
                                 @JsonProperty("frequency") String frequency
                                 ){
        this.id = id;
        this.campaignId = campaignId;
        this.campaignTitle = campaignTitle;
        this.schedulingDate = schedulingDate;
        this.frequency = frequency;
    }

    public Long getId() {
        return id;
    }

    public Long getCampaignId() {
        return campaignId;
    }

    public String getCampaignTitle() {
        return campaignTitle;
    }

    public LocalDateTime getSchedulingDate() {
        return schedulingDate;
    }

    public String getFrequency() {
        return frequency;
    }
}
