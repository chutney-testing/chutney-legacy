package com.chutneytesting.design.domain.campaign;

import java.time.LocalDateTime;
import java.util.Objects;

public class SchedulingCampaign {

    public final Long id;
    public final Long campaignId;
    public final String campaignTitle;
    public LocalDateTime schedulingDate;
    public final String frequency;

    public SchedulingCampaign(Long id, Long campaignId, String campaignTitle, LocalDateTime schedulingDate, String frequency) {
        this.id = id;
        this.campaignId = campaignId;
        this.campaignTitle = campaignTitle;
        this.schedulingDate = schedulingDate;
        this.frequency = frequency;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SchedulingCampaign that = (SchedulingCampaign) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
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

    public LocalDateTime getSchedulingDatePerFrequency(String frequency) {
        ScheduledInterface scheduledInterface = f -> {
            if (f.equals("daily")) return this.schedulingDate.plusDays(1);
            else if (f.equals("weekly")) return this.schedulingDate.plusWeeks(1);
            else return this.schedulingDate.plusMonths(1);
        };
        return scheduledInterface.getDateTime(frequency);
    }
}

@FunctionalInterface
interface ScheduledInterface {
    LocalDateTime getDateTime(String freq);
}
