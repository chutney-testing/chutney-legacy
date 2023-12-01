/*
 * Copyright 2017-2023 Enedis
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


package com.chutneytesting.campaign.api.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
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

    public SchedulingCampaignDto(Long id,
                                 Long campaignId,
                                 String campaignTitle,
                                 LocalDateTime schedulingDate,
                                 String frequency
    ) {
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
