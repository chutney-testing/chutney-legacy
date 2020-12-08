package com.chutneytesting.design.domain.campaign;

import java.util.List;

/**
 * CRUD for SchedulingCampaign
 */
public interface SchedulingCampaignRepository {

    SchedulingCampaign add(SchedulingCampaign schedulingCampaign);

    void removeById(Long id);

    List<SchedulingCampaign> getALl();
}
