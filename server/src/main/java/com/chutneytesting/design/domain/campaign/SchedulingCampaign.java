package com.chutneytesting.design.domain.campaign;

import java.time.LocalDateTime;
import java.util.Objects;

public class SchedulingCampaign {

    public final Long id;
    public final Long campaignId;
    public final String campaignTitle;
    public final LocalDateTime schedulingDate;

    public SchedulingCampaign(Long id,
                              Long campaignId,
                              String campaignTitle,
                              LocalDateTime schedulingDate) {
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
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
