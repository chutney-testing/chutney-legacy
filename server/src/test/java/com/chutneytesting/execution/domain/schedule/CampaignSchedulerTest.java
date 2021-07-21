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
import com.chutneytesting.design.domain.campaign.ScheduledCampaign;
import com.chutneytesting.design.domain.campaign.ScheduledCampaignRepository;
import com.chutneytesting.execution.domain.campaign.CampaignExecutionEngine;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;
import java.util.stream.IntStream;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.ArgumentCaptor;

public class CampaignSchedulerTest {

    private CampaignScheduler sut;

    private final TimeScheduledCampaignRepository timeScheduledCampaignRepository = mock(TimeScheduledCampaignRepository.class);
    private final CampaignExecutionEngine campaignExecutionEngine = mock(CampaignExecutionEngine.class);
    private final ScheduledCampaignRepository scheduledCampaignRepository = mock(ScheduledCampaignRepository.class);

    private Clock clock;

    @BeforeEach
    public void setUp() {
        clock = Clock.systemDefaultZone();
        sut = new CampaignScheduler(campaignExecutionEngine, timeScheduledCampaignRepository, clock, scheduledCampaignRepository);
    }

    @Test
    void should_accepts_10_minutes_late_when_executing_time_scheduled_campaigns_for_the_first_time() {
        LocalDateTime timeScheduledCampaignsLastExecutionInit = sut.timeScheduledCampaignsLastExecution();
        sut.executeScheduledCampaigns();
        LocalDateTime timeScheduledCampaignsLastExecution = sut.timeScheduledCampaignsLastExecution();

        LocalDateTime afterDateTime = capturedDateTimesToSelectTimeScheduledCampaigns(1).get(0);

        assertThat(afterDateTime).isEqualTo(timeScheduledCampaignsLastExecutionInit);
        assertThat(timeScheduledCampaignsLastExecution).isAfterOrEqualTo(timeScheduledCampaignsLastExecutionInit.plusMinutes(10));
    }

    @Test
    void should_execute_time_scheduled_campaigns_selecting_them_by_time_frames() {
        int nb_executions = 5;
        List<Long> timeScheduledCampaigns = stubTimeScheduledCampaigns(2);

        IntStream.range(0, nb_executions).forEach(
            i -> sut.executeScheduledCampaigns()
        );

        List<LocalDateTime> values = capturedDateTimesToSelectTimeScheduledCampaigns(nb_executions);
        assertThat(values).hasSize(nb_executions);
        assertThat(values).isSortedAccordingTo(LocalDateTime::compareTo);

        assertCampaignWasExecuted(timeScheduledCampaigns.get(0), nb_executions);
        assertCampaignWasExecuted(timeScheduledCampaigns.get(1), nb_executions);
    }

    @ParameterizedTest()
    @EnumSource(FREQUENCY.class)
    void should_execute_campaign_as_user_when_executing_scheduled_campaign(FREQUENCY frequency) {
        ScheduledCampaign lastScheduledCampaignExecution = stubScheduledCampaignsWith(singletonList(
            Pair.of(now(clock).minusSeconds(5), frequency)
        )).get(0);

        sut.executeScheduledCampaigns();

        assertCampaignWasExecuted(lastScheduledCampaignExecution.campaignId, 1);
    }

    @ParameterizedTest()
    @EnumSource(FREQUENCY.class)
    void should_remove_last_scheduled_execution_when_executing_scheduled_campaign(FREQUENCY frequency) {
        ScheduledCampaign lastScheduledCampaignExecution = stubScheduledCampaignsWith(singletonList(
            Pair.of(now(clock).minusSeconds(5), frequency)
        )).get(0);

        sut.executeScheduledCampaigns();

        verify(scheduledCampaignRepository).removeById(lastScheduledCampaignExecution.id);
    }

    @ParameterizedTest()
    @EnumSource(FREQUENCY.class)
    void should_add_next_scheduled_execution_when_executing_scheduled_campaign_except_for_EMPTY_frequency(FREQUENCY frequency) {
        clock = Clock.fixed(clock.instant(), clock.getZone());

        ScheduledCampaign lastScheduledCampaignExecution = stubScheduledCampaignsWith(singletonList(
            Pair.of(now(clock).minusSeconds(5), frequency)
        )).get(0);

        sut.executeScheduledCampaigns();

        if (EMPTY.equals(frequency)) {
            verify(scheduledCampaignRepository, times(0)).add(any());
        } else {
            verify(scheduledCampaignRepository).add(
                lastScheduledCampaignExecution.nextScheduledExecution()
            );
        }
    }

    @Test
    void should_not_explode_when_runtime_exceptions_occur_retrieving_campaigns_to_execute() {
        when(scheduledCampaignRepository.getALl())
            .thenThrow(new RuntimeException("scheduledCampaignRepository.getAll()"));
        when(timeScheduledCampaignRepository.getCampaignScheduledAfter(any()))
            .thenThrow(new RuntimeException("timeScheduledCampaignRepository.getCampaignScheduledAfter()"));

        Assertions.assertDoesNotThrow(
            () -> sut.executeScheduledCampaigns()
        );

        verify(scheduledCampaignRepository).getALl();
        verify(timeScheduledCampaignRepository).getCampaignScheduledAfter(any());
    }

    @Test
    void should_not_explode_when_runtime_exceptions_occur_executing_campaigns() {
        List<ScheduledCampaign> scheduledCampaigns = stubScheduledCampaignsWith(asList(
            Pair.of(now(clock).minusSeconds(5), FREQUENCY.MONTHLY),
            Pair.of(now(clock).minusSeconds(5), FREQUENCY.DAILY)
        ));
        List<Long> timeScheduledCampaigns = stubTimeScheduledCampaigns(2);

        when(campaignExecutionEngine.executeById(scheduledCampaigns.get(0).campaignId, SCHEDULER_EXECUTE_USER))
            .thenThrow(new RuntimeException("campaignExecutionEngine.executeById"));
        when(campaignExecutionEngine.executeById(timeScheduledCampaigns.get(1), SCHEDULER_EXECUTE_USER))
            .thenThrow(new RuntimeException("campaignExecutionEngine.executeById"));

        Assertions.assertDoesNotThrow(
            () -> sut.executeScheduledCampaigns()
        );

        assertCampaignWasExecuted(null, scheduledCampaigns.size() + timeScheduledCampaigns.size());
    }

    private void assertCampaignWasExecuted(Long campaignId, int nb_executions) {
        if (campaignId == null) {
            verify(campaignExecutionEngine, times(nb_executions)).executeById(any(), any());
        } else {
            verify(campaignExecutionEngine, times(nb_executions)).executeById(campaignId, SCHEDULER_EXECUTE_USER);
        }
    }

    private List<LocalDateTime> capturedDateTimesToSelectTimeScheduledCampaigns(int nbCalls) {
        ArgumentCaptor<LocalDateTime> afterDateTimeCapture = ArgumentCaptor.forClass(LocalDateTime.class);
        verify(timeScheduledCampaignRepository, times(nbCalls)).getCampaignScheduledAfter(afterDateTimeCapture.capture());
        return afterDateTimeCapture.getAllValues();
    }

    private List<Long> stubTimeScheduledCampaigns(int nbStubs) {
        Random rand = new Random();
        List<Long> campaignIds = IntStream.range(0, nbStubs)
            .mapToObj(i -> rand.nextLong())
            .collect(toList());
        when(timeScheduledCampaignRepository.getCampaignScheduledAfter(any()))
            .thenReturn(campaignIds);
        return campaignIds;
    }

    private List<ScheduledCampaign> stubScheduledCampaignsWith(List<Pair<LocalDateTime, FREQUENCY>> scheduledDatesAndFrequencies) {
        Random rand = new Random();

        List<ScheduledCampaign> scheduledCampaigns = scheduledDatesAndFrequencies.stream()
            .map(p ->
                new ScheduledCampaign(rand.nextLong(), rand.nextLong(), "title", p.getLeft(), p.getRight())
            )
            .collect(toList());

        when(scheduledCampaignRepository.getALl())
            .thenReturn(
                scheduledCampaigns
            );

        return scheduledCampaigns;
    }
}
