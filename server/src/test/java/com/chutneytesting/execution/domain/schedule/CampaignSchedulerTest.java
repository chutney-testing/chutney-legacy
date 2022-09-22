package com.chutneytesting.execution.domain.schedule;

import static com.chutneytesting.campaign.domain.Frequency.EMPTY;
import static com.chutneytesting.execution.domain.schedule.CampaignScheduler.SCHEDULER_EXECUTE_USER;
import static java.time.LocalDateTime.now;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toList;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.chutneytesting.campaign.domain.Frequency;
import com.chutneytesting.campaign.domain.PeriodicScheduledCampaign;
import com.chutneytesting.campaign.domain.PeriodicScheduledCampaignRepository;
import com.chutneytesting.execution.domain.campaign.CampaignExecutionEngine;
import java.time.Clock;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Executors;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

public class CampaignSchedulerTest {

    private CampaignScheduler sut;

    private final CampaignExecutionEngine campaignExecutionEngine = mock(CampaignExecutionEngine.class);
    private final PeriodicScheduledCampaignRepository periodicScheduledCampaignRepository = mock(PeriodicScheduledCampaignRepository.class);

    private Clock clock;

    @BeforeEach
    public void setUp() {
        clock = Clock.systemDefaultZone();
        sut = new CampaignScheduler(campaignExecutionEngine, clock, periodicScheduledCampaignRepository, Executors.newFixedThreadPool(2));
    }

    @ParameterizedTest()
    @EnumSource(Frequency.class)
    void should_execute_campaign_as_internal_user_named_auto_when_executing_periodic_scheduled_campaign(Frequency frequency) {
        List<PeriodicScheduledCampaign> periodicScheduledCampaign = createPeriodicScheduledCampaigns(singletonList(frequency));
        when(periodicScheduledCampaignRepository.getALl())
            .thenReturn(
                periodicScheduledCampaign
            );

        sut.executeScheduledCampaigns();

        verify(campaignExecutionEngine).executeById(periodicScheduledCampaign.get(0).campaignId, "auto");
    }

    @ParameterizedTest()
    @EnumSource(Frequency.class)
    void should_remove_last_execution_when_executing_periodic_scheduled_campaign(Frequency frequency) {
        List<PeriodicScheduledCampaign> periodicScheduledCampaign = createPeriodicScheduledCampaigns(singletonList(frequency));
        when(periodicScheduledCampaignRepository.getALl())
            .thenReturn(
                periodicScheduledCampaign
            );

        sut.executeScheduledCampaigns();

        verify(periodicScheduledCampaignRepository).removeById(periodicScheduledCampaign.get(0).id);
    }

    @ParameterizedTest()
    @EnumSource(Frequency.class)
    void should_add_next_execution_when_executing_periodic_scheduled_campaign_except_for_EMPTY_frequency(Frequency frequency) {
        List<PeriodicScheduledCampaign> periodicScheduledCampaign = createPeriodicScheduledCampaigns(singletonList(frequency));
        when(periodicScheduledCampaignRepository.getALl())
            .thenReturn(
                periodicScheduledCampaign
            );

        sut.executeScheduledCampaigns();

        if (EMPTY.equals(frequency)) {
            verify(periodicScheduledCampaignRepository, times(0)).add(any());
        } else {
            verify(periodicScheduledCampaignRepository).add(
                periodicScheduledCampaign.get(0).nextScheduledExecution()
            );
        }
    }

    @Test
    void should_not_explode_when_runtime_exceptions_occur_retrieving_campaigns_to_execute() {
        when(periodicScheduledCampaignRepository.getALl())
            .thenThrow(new RuntimeException("scheduledCampaignRepository.getAll()"));
        Assertions.assertDoesNotThrow(
            () -> sut.executeScheduledCampaigns()
        );

        verify(periodicScheduledCampaignRepository).getALl();
    }

    @Test
    void should_not_explode_when_runtime_exceptions_occur_executing_campaigns() {
        List<PeriodicScheduledCampaign> periodicScheduledCampaigns = createPeriodicScheduledCampaigns(asList(Frequency.MONTHLY, Frequency.DAILY));
        when(periodicScheduledCampaignRepository.getALl())
            .thenReturn(
                periodicScheduledCampaigns
            );
        when(campaignExecutionEngine.executeById(periodicScheduledCampaigns.get(0).campaignId, SCHEDULER_EXECUTE_USER))
            .thenThrow(new RuntimeException("campaignExecutionEngine.executeById"));

        Assertions.assertDoesNotThrow(
            () -> sut.executeScheduledCampaigns()
        );

        verify(campaignExecutionEngine, times(periodicScheduledCampaigns.size())).executeById(any(), any());
    }

    private List<PeriodicScheduledCampaign> createPeriodicScheduledCampaigns(List<Frequency> frequencies) {
        Random rand = new Random();
        return frequencies.stream()
            .map(f ->
                new PeriodicScheduledCampaign(rand.nextLong(), rand.nextLong(), "title", now(clock).minusSeconds(5), f)
            )
            .collect(toList());
    }
}
