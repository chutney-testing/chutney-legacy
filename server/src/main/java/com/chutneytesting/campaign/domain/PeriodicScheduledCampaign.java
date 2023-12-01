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

package com.chutneytesting.campaign.domain;

import java.time.LocalDateTime;
import java.util.Objects;

public class PeriodicScheduledCampaign {

    public final Long id;
    public final Long campaignId;
    public final String campaignTitle;
    public final LocalDateTime nextExecutionDate;
    public final Frequency frequency;

    public PeriodicScheduledCampaign(Long id, Long campaignId, String campaignTitle, LocalDateTime nextExecutionDate, Frequency frequency) {
        this.id = id;
        this.campaignId = campaignId;
        this.campaignTitle = campaignTitle;
        this.nextExecutionDate = nextExecutionDate;
        this.frequency = frequency;
    }

    public PeriodicScheduledCampaign(Long id, Long campaignId, String campaignTitle, LocalDateTime nextExecutionDate) {
        this(id, campaignId, campaignTitle, nextExecutionDate, Frequency.EMPTY);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PeriodicScheduledCampaign that = (PeriodicScheduledCampaign) o;
        return Objects.equals(id, that.id) && Objects.equals(campaignId, that.campaignId) && Objects.equals(campaignTitle, that.campaignTitle) && Objects.equals(nextExecutionDate, that.nextExecutionDate) && Objects.equals(frequency, that.frequency);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, campaignId, campaignTitle, nextExecutionDate, frequency);
    }

    @Override
    public String toString() {
        return "SchedulingCampaign{" +
            "id=" + id +
            ", campaignId=" + campaignId +
            ", campaignTitle='" + campaignTitle + '\'' +
            ", schedulingDate=" + nextExecutionDate +
            ", frequency='" + frequency + '\'' +
            '}';
    }

    public PeriodicScheduledCampaign nextScheduledExecution() {
        LocalDateTime scheduledDate = switch (this.frequency) {
            case HOURLY -> this.nextExecutionDate.plusHours(1);
            case DAILY -> this.nextExecutionDate.plusDays(1);
            case WEEKLY -> this.nextExecutionDate.plusWeeks(1);
            case MONTHLY -> this.nextExecutionDate.plusMonths(1);
            default -> throw new IllegalStateException("Unexpected value: " + this.frequency);
        };
        return new PeriodicScheduledCampaign(id, campaignId, campaignTitle, scheduledDate, frequency);
    }
}
