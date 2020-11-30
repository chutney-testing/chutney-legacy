package com.chutneytesting.execution.domain.schedule;

import java.time.LocalDateTime;
import java.util.List;

public interface SchedulerRepository {

    /**
     * @param lastExecutionDateTime
     * @return ids of campaigns schedule between lastExecutionTime and now
     */
    List<Long> getCampaignScheduledAfter(LocalDateTime lastExecutionDateTime);

}
