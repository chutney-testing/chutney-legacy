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

package com.chutneytesting.campaign.infra;

import java.time.LocalDateTime;
import java.util.List;

public class SchedulingCampaignDto {
    public final String id;
    public final List<Long> campaignsId;
    public final List<String> campaignsTitle;
    public final LocalDateTime schedulingDate;
    public final String frequency;

    /**
     * for ObjectMapper only
     **/
    public SchedulingCampaignDto() {
        id = null;
        campaignsId = null;
        schedulingDate = null;
        campaignsTitle = null;
        frequency = null;
    }

    public SchedulingCampaignDto(String id,
                                 List<Long> campaignsId,
                                 List<String> campaignsTitle,
                                 LocalDateTime schedulingDate,
                                 String frequency) {
        this.id = id;
        this.campaignsId = campaignsId;
        this.campaignsTitle = campaignsTitle;
        this.schedulingDate = schedulingDate;
        this.frequency = frequency;
    }
}
