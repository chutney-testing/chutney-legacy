package com.chutneytesting.execution.domain.schedule;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.chutneytesting.execution.domain.campaign.CampaignExecutionEngine;
import com.google.common.collect.Lists;
import java.time.Clock;
import java.time.LocalDateTime;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

public class CampaignSchedulerTest {

    private CampaignScheduler campaignScheduler;
    private SchedulerRepository schedulerRepository = mock(SchedulerRepository.class);
    private CampaignExecutionEngine campaignExecutionEngine = mock(CampaignExecutionEngine.class);
    private Clock clock;

    @Before
    public void setUp() {
        clock = Clock.systemDefaultZone();
        campaignScheduler = new CampaignScheduler(campaignExecutionEngine, schedulerRepository, clock);
    }

    @Test
    public void executeScheduledCampaign() {
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
}
