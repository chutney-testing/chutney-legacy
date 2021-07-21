package com.chutneytesting.design.domain.campaign;

import java.time.LocalDateTime;
import java.util.Objects;

public class ScheduledCampaign {

    public final Long id;
    public final Long campaignId;
    public final String campaignTitle;
    public final LocalDateTime scheduledDate;
    public final FREQUENCY frequency;

    public ScheduledCampaign(Long id, Long campaignId, String campaignTitle, LocalDateTime scheduledDate, FREQUENCY frequency) {
        this.id = id;
        this.campaignId = campaignId;
        this.campaignTitle = campaignTitle;
        this.scheduledDate = scheduledDate;
        this.frequency = frequency;
    }

    public ScheduledCampaign(Long id, Long campaignId, String campaignTitle, LocalDateTime scheduledDate) {
        this(id, campaignId, campaignTitle, scheduledDate, FREQUENCY.EMPTY);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ScheduledCampaign that = (ScheduledCampaign) o;
        return Objects.equals(id, that.id) && Objects.equals(campaignId, that.campaignId) && Objects.equals(campaignTitle, that.campaignTitle) && Objects.equals(scheduledDate, that.scheduledDate) && Objects.equals(frequency, that.frequency);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, campaignId, campaignTitle, scheduledDate, frequency);
    }

    @Override
    public String toString() {
        return "SchedulingCampaign{" +
            "id=" + id +
            ", campaignId=" + campaignId +
            ", campaignTitle='" + campaignTitle + '\'' +
            ", schedulingDate=" + scheduledDate +
            ", frequency='" + frequency + '\'' +
            '}';
    }

    public ScheduledCampaign nextScheduledExecution() {
        LocalDateTime scheduledDate;
        switch (this.frequency) {
            case HOURLY:
                scheduledDate = this.scheduledDate.plusHours(1);
                break;
            case DAILY:
                scheduledDate = this.scheduledDate.plusDays(1);
                break;
            case WEEKLY:
                scheduledDate = this.scheduledDate.plusWeeks(1);
                break;
            case MONTHLY:
                scheduledDate = this.scheduledDate.plusMonths(1);
                break;
            default:
                throw new IllegalStateException("Unexpected value: " + this.frequency);
        }
        return new ScheduledCampaign(id, campaignId, campaignTitle, scheduledDate, frequency);
    }
}
