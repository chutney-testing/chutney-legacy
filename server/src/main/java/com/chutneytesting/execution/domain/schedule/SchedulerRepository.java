package com.chutneytesting.execution.domain.schedule;

import java.time.LocalTime;
import java.util.List;

public interface SchedulerRepository {

    /**
     * @param lastExecutionTime
     * @return ids of campaigns schedule between lastExecutionTime and now
     */
    List<Long> getCampaignScheduledAfter(LocalTime lastExecutionTime);

}
