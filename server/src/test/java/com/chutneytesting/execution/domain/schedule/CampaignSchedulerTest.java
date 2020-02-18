package com.chutneytesting.execution.domain.schedule;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.common.collect.Lists;
import com.chutneytesting.execution.domain.campaign.CampaignExecutionEngine;
import java.time.LocalTime;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

public class CampaignSchedulerTest {

    private CampaignScheduler campaignScheduler;
    private SchedulerRepository schedulerRepository = mock(SchedulerRepository.class);
    private CampaignExecutionEngine campaignExecutionEngine = mock(CampaignExecutionEngine.class);

    @Before
    public void setUp() {
        campaignScheduler = new CampaignScheduler(campaignExecutionEngine, schedulerRepository);
    }

    @Test
    public void executeScheduledCampaign() {
        campaignScheduler.executeScheduledCampaign();
        reset(schedulerRepository);
        when(schedulerRepository.getCampaignScheduledAfter(any()))
            .thenReturn(Lists.newArrayList(1L, 2L));

        campaignScheduler.executeScheduledCampaign();

        ArgumentCaptor<LocalTime> scheduledTime = ArgumentCaptor.forClass(LocalTime.class);
        verify(schedulerRepository).getCampaignScheduledAfter(scheduledTime.capture());
        assertThat(scheduledTime.getValue()).isBeforeOrEqualTo(LocalTime.now());
        assertThat(scheduledTime.getValue()).isAfter(LocalTime.now().minusMinutes(1));

        verify(campaignExecutionEngine).executeById(1L);
        verify(campaignExecutionEngine).executeById(2L);
    }
}
