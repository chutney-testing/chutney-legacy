package com.chutneytesting.execution.domain.schedule;

import static com.chutneytesting.design.domain.campaign.FREQUENCY.EMPTY;
import static com.chutneytesting.execution.domain.schedule.CampaignScheduler.SCHEDULER_EXECUTE_USER;
import static java.time.LocalDateTime.now;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.chutneytesting.design.domain.campaign.FREQUENCY;
import com.chutneytesting.design.domain.campaign.PeriodicScheduledCampaign;
import com.chutneytesting.design.domain.campaign.PeriodicScheduledCampaignRepository;
import com.chutneytesting.execution.domain.campaign.CampaignExecutionEngine;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;
import java.util.stream.IntStream;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.ArgumentCaptor;

public class CampaignSchedulerTest {

    private CampaignScheduler sut;

    private final DailyScheduledCampaignRepository dailyScheduledCampaignRepository = mock(DailyScheduledCampaignRepository.class);
    private final CampaignExecutionEngine campaignExecutionEngine = mock(CampaignExecutionEngine.class);
    private final PeriodicScheduledCampaignRepository periodicScheduledCampaignRepository = mock(PeriodicScheduledCampaignRepository.class);

    private Clock clock;

    @BeforeEach
    public void setUp() {
        clock = Clock.systemDefaultZone();
        sut = new CampaignScheduler(campaignExecutionEngine, dailyScheduledCampaignRepository, clock, periodicScheduledCampaignRepository);
    }

    @Test
    void should_init_last_execution_time_of_daily_scheduled_campaigns_10_minutes_before_init_time() {
        LocalDateTime dailyScheduledCampaignsLastExecutionInit = sut.dailyScheduledCampaignsLastExecution();
        assertThat(dailyScheduledCampaignsLastExecutionInit).isBeforeOrEqualTo(now(clock).minusMinutes(10));
    }

    @Test
    void should_use_and_update_last_execution_time_of_daily_scheduled_campaigns_when_execute() {
        LocalDateTime dailyScheduledCampaignsLastExecutionInit = sut.dailyScheduledCampaignsLastExecution();
        List<Long> dailyScheduledCampaignId = createDailyScheduledCampaignIds(1);
        when(dailyScheduledCampaignRepository.getCampaignScheduledAfter(any()))
            .thenReturn(dailyScheduledCampaignId);

        sut.executeScheduledCampaigns();

        ArgumentCaptor<LocalDateTime> afterDateTimeCapture = ArgumentCaptor.forClass(LocalDateTime.class);
        verify(dailyScheduledCampaignRepository).getCampaignScheduledAfter(afterDateTimeCapture.capture());
        List<LocalDateTime> values = afterDateTimeCapture.getAllValues();
        assertThat(values).hasSize(1);
        assertThat(values.get(0)).isEqualTo(dailyScheduledCampaignsLastExecutionInit);
        verify(campaignExecutionEngine).executeById(dailyScheduledCampaignId.get(0), SCHEDULER_EXECUTE_USER);
    }

    @ParameterizedTest()
    @EnumSource(FREQUENCY.class)
    void should_execute_campaign_as_internal_user_named_auto_when_executing_periodic_scheduled_campaign(FREQUENCY frequency) {
        List<PeriodicScheduledCampaign> periodicScheduledCampaign = createPeriodicScheduledCampaigns(singletonList(frequency));
        when(periodicScheduledCampaignRepository.getALl())
            .thenReturn(
                periodicScheduledCampaign
            );

        sut.executeScheduledCampaigns();

        verify(campaignExecutionEngine).executeById(periodicScheduledCampaign.get(0).campaignId, "auto");
    }

    @ParameterizedTest()
    @EnumSource(FREQUENCY.class)
    void should_remove_last_execution_when_executing_periodic_scheduled_campaign(FREQUENCY frequency) {
        List<PeriodicScheduledCampaign> periodicScheduledCampaign = createPeriodicScheduledCampaigns(singletonList(frequency));
        when(periodicScheduledCampaignRepository.getALl())
            .thenReturn(
                periodicScheduledCampaign
            );

        sut.executeScheduledCampaigns();

        verify(periodicScheduledCampaignRepository).removeById(periodicScheduledCampaign.get(0).id);
    }

    @ParameterizedTest()
    @EnumSource(FREQUENCY.class)
    void should_add_next_execution_when_executing_periodic_scheduled_campaign_except_for_EMPTY_frequency(FREQUENCY frequency) {
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
        when(dailyScheduledCampaignRepository.getCampaignScheduledAfter(any()))
            .thenThrow(new RuntimeException("timeScheduledCampaignRepository.getCampaignScheduledAfter()"));

        Assertions.assertDoesNotThrow(
            () -> sut.executeScheduledCampaigns()
        );

        verify(periodicScheduledCampaignRepository).getALl();
        verify(dailyScheduledCampaignRepository).getCampaignScheduledAfter(any());
    }

    @Test
    void should_not_explode_when_runtime_exceptions_occur_executing_campaigns() {
        List<PeriodicScheduledCampaign> periodicScheduledCampaigns = createPeriodicScheduledCampaigns(asList(FREQUENCY.MONTHLY, FREQUENCY.DAILY));
        when(periodicScheduledCampaignRepository.getALl())
            .thenReturn(
                periodicScheduledCampaigns
            );
        List<Long> dailyScheduledCampaigns = createDailyScheduledCampaignIds(2);
        when(dailyScheduledCampaignRepository.getCampaignScheduledAfter(any()))
            .thenReturn(dailyScheduledCampaigns);

        when(campaignExecutionEngine.executeById(periodicScheduledCampaigns.get(0).campaignId, SCHEDULER_EXECUTE_USER))
            .thenThrow(new RuntimeException("campaignExecutionEngine.executeById"));
        when(campaignExecutionEngine.executeById(dailyScheduledCampaigns.get(1), SCHEDULER_EXECUTE_USER))
            .thenThrow(new RuntimeException("campaignExecutionEngine.executeById"));

        Assertions.assertDoesNotThrow(
            () -> sut.executeScheduledCampaigns()
        );

        verify(campaignExecutionEngine, times(periodicScheduledCampaigns.size() + dailyScheduledCampaigns.size())).executeById(any(), any());
    }

    private List<Long> createDailyScheduledCampaignIds(int nbIds) {
        Random rand = new Random();
        return IntStream.range(0, nbIds)
            .mapToObj(i -> rand.nextLong())
            .collect(toList());
    }

    private List<PeriodicScheduledCampaign> createPeriodicScheduledCampaigns(List<FREQUENCY> frequencies) {
        Random rand = new Random();
        return frequencies.stream()
            .map(f ->
                new PeriodicScheduledCampaign(rand.nextLong(), rand.nextLong(), "title", now(clock).minusSeconds(5), f)
            )
            .collect(toList());
    }
}
