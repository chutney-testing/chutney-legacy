package com.chutneytesting.design.domain.campaign;

import java.time.LocalDateTime;
import java.util.Objects;

public class PeriodicScheduledCampaign {

    public final Long id;
    public final Long campaignId;
    public final String campaignTitle;
    public final LocalDateTime nextExecutionDate;
    public final FREQUENCY frequency;

    public PeriodicScheduledCampaign(Long id, Long campaignId, String campaignTitle, LocalDateTime nextExecutionDate, FREQUENCY frequency) {
        this.id = id;
        this.campaignId = campaignId;
        this.campaignTitle = campaignTitle;
        this.nextExecutionDate = nextExecutionDate;
        this.frequency = frequency;
    }

    public PeriodicScheduledCampaign(Long id, Long campaignId, String campaignTitle, LocalDateTime nextExecutionDate) {
        this(id, campaignId, campaignTitle, nextExecutionDate, FREQUENCY.EMPTY);
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
        LocalDateTime scheduledDate;
        switch (this.frequency) {
            case HOURLY:
                scheduledDate = this.nextExecutionDate.plusHours(1);
                break;
            case DAILY:
                scheduledDate = this.nextExecutionDate.plusDays(1);
                break;
            case WEEKLY:
                scheduledDate = this.nextExecutionDate.plusWeeks(1);
                break;
            case MONTHLY:
                scheduledDate = this.nextExecutionDate.plusMonths(1);
                break;
            default:
                throw new IllegalStateException("Unexpected value: " + this.frequency);
        }
        return new PeriodicScheduledCampaign(id, campaignId, campaignTitle, scheduledDate, frequency);
    }
}
