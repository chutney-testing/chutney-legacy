package com.chutneytesting.design.domain.campaign;

import java.time.LocalDateTime;
import java.util.Objects;

public class SchedulingCampaign {

    public final Long id;
    public final Long campaignId;
    public final String campaignTitle;
    private LocalDateTime schedulingDate;
    public final FREQUENCY frequency;

    public SchedulingCampaign(Long id, Long campaignId, String campaignTitle, LocalDateTime schedulingDate, FREQUENCY frequency) {
        this.id = id;
        this.campaignId = campaignId;
        this.campaignTitle = campaignTitle;
        this.schedulingDate = schedulingDate;
        this.frequency = frequency;
    }

    public SchedulingCampaign(Long id, Long campaignId, String campaignTitle, LocalDateTime schedulingDate) {
        this(id, campaignId, campaignTitle, schedulingDate, FREQUENCY.EMPTY);
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
        switch (this.frequency) {
            case HOURLY:
                return this.schedulingDate.plusHours(1);
            case DAILY:
                return this.schedulingDate.plusDays(1);
            case WEEKLY:
                return this.schedulingDate.plusWeeks(1);
            case MONTHLY:
                return this.schedulingDate.plusMonths(1);
            default:
                throw new IllegalStateException("Unexpected value: " + this.frequency);
        }
    }
}
