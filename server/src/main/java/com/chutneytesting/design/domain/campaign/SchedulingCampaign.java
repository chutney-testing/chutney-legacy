package com.chutneytesting.design.domain.campaign;

import static com.chutneytesting.design.domain.campaign.FREQUENCY.DAILY;
import static com.chutneytesting.design.domain.campaign.FREQUENCY.HOURLY;
import static com.chutneytesting.design.domain.campaign.FREQUENCY.WEEKLY;

import java.time.LocalDateTime;
import java.util.Objects;

public class SchedulingCampaign {

    public final Long id;
    public final Long campaignId;
    public final String campaignTitle;
    private LocalDateTime schedulingDate;
    public String frequency;

    public SchedulingCampaign(Long id, Long campaignId, String campaignTitle, LocalDateTime schedulingDate, String frequency) {
        this.id = id;
        this.campaignId = campaignId;
        this.campaignTitle = campaignTitle;
        this.schedulingDate = schedulingDate;
        this.frequency = frequency;
    }

    public SchedulingCampaign(Long id, Long campaignId, String campaignTitle, LocalDateTime schedulingDate) {
        this.id = id;
        this.campaignId = campaignId;
        this.campaignTitle = campaignTitle;
        this.schedulingDate = schedulingDate;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SchedulingCampaign that = (SchedulingCampaign) o;
        return Objects.equals(id, that.id) && Objects.equals(campaignId, that.campaignId) && Objects.equals(campaignTitle, that.campaignTitle) && Objects.equals(schedulingDate, that.schedulingDate) && Objects.equals(frequency, that.frequency);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, campaignId, campaignTitle, schedulingDate, frequency);
    }

    public void setSchedulingDate(LocalDateTime schedulingDate) {
        this.schedulingDate = schedulingDate;
    }

    @Override
    public String toString() {
        return "SchedulingCampaign{" +
            "id=" + id +
            ", campaignId=" + campaignId +
            ", campaignTitle='" + campaignTitle + '\'' +
            ", schedulingDate=" + schedulingDate +
            ", frequency='" + frequency + '\'' +
            '}';
    }

    public LocalDateTime getSchedulingDate() {
        return schedulingDate;
    }

    public LocalDateTime getNextSchedulingDate() {
        if (this.frequency.equals(HOURLY.toString())) {
            return this.schedulingDate.plusHours(1);
        } else if (this.frequency.equals(DAILY.toString())) {
            return this.schedulingDate.plusDays(1);
        } else if (this.frequency.equals(WEEKLY.toString())) {
            return this.schedulingDate.plusWeeks(1);
        } else {
            return this.schedulingDate.plusMonths(1);
        }
    }
}
