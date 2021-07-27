package com.chutneytesting.design.domain.campaign;

import java.util.List;

/**
 * CRUD for SchedulingCampaign
 */
public interface PeriodicScheduledCampaignRepository {

    PeriodicScheduledCampaign add(PeriodicScheduledCampaign periodicScheduledCampaign);

    void removeById(Long id);

    List<PeriodicScheduledCampaign> getALl();
}
