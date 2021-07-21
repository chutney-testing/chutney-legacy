package com.chutneytesting.design.domain.campaign;

import java.util.List;

/**
 * CRUD for SchedulingCampaign
 */
public interface ScheduledCampaignRepository {

    ScheduledCampaign add(ScheduledCampaign scheduledCampaign);

    void removeById(Long id);

    List<ScheduledCampaign> getALl();
}
