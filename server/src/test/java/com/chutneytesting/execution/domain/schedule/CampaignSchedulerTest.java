package com.chutneytesting.execution.domain.schedule;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.chutneytesting.design.domain.campaign.SchedulingCampaign;
import com.chutneytesting.design.domain.campaign.SchedulingCampaignRepository;
import com.chutneytesting.execution.domain.campaign.CampaignExecutionEngine;
import com.google.common.collect.Lists;
import java.time.Clock;
import java.time.LocalDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

public class CampaignSchedulerTest {

    private CampaignScheduler campaignScheduler;
    private SchedulerRepository schedulerRepository = mock(SchedulerRepository.class);
    private CampaignExecutionEngine campaignExecutionEngine = mock(CampaignExecutionEngine.class);
    private SchedulingCampaignRepository schedulingCampaignRepository = mock(SchedulingCampaignRepository.class);

    private Clock clock;

    @BeforeEach
    public void setUp() {
        clock = Clock.systemDefaultZone();
        campaignScheduler = new CampaignScheduler(campaignExecutionEngine, schedulerRepository, clock, schedulingCampaignRepository);
    }

    @Test
    public void verify_campaign_to_execute_periodically_are_retrieved_and_executed_if_date_is_in_last_interval() {
        campaignScheduler.executeScheduledCampaign();
        reset(schedulerRepository);

        when(schedulerRepository.getCampaignScheduledAfter(any()))
            .thenReturn(Lists.newArrayList(1L, 2L));

        campaignScheduler.executeScheduledCampaign();

        ArgumentCaptor<LocalDateTime> scheduledTime = ArgumentCaptor.forClass(LocalDateTime.class);
        verify(schedulerRepository).getCampaignScheduledAfter(scheduledTime.capture());
        assertThat(scheduledTime.getValue()).isBeforeOrEqualTo(LocalDateTime.now(clock));
        assertThat(scheduledTime.getValue()).isAfter(LocalDateTime.now(clock).minusMinutes(1));

        verify(campaignExecutionEngine).executeById(1L, "auto");
        verify(campaignExecutionEngine).executeById(2L, "auto");
    }

    @Test
    public void verify_campaign_scheduled_at_date_are_retrieved_and_executed_if_execution_date_is_in_the_past() {
        campaignScheduler.executeScheduledCampaign();
        when(schedulingCampaignRepository.getALl())
            .thenReturn(Lists.newArrayList(
                new SchedulingCampaign(1L, 3L, "title", LocalDateTime.now().minusMinutes(1)),
                new SchedulingCampaign(2L, 4L, "title", LocalDateTime.now().plusMinutes(1))
            ));

        campaignScheduler.executeScheduledCampaign();

        verify(schedulingCampaignRepository).removeById(1L);
        verify(campaignExecutionEngine).executeById(3L, "auto");
    }
}
