package com.chutneytesting.design.api.campaign;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.chutneytesting.design.api.campaign.dto.CampaignExecutionReportDto;
import com.chutneytesting.design.api.campaign.dto.ScenarioExecutionReportOutlineDto;
import com.chutneytesting.execution.domain.history.ExecutionHistory;
import com.chutneytesting.execution.domain.report.ServerReportStatus;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.IntStream;
import org.junit.jupiter.api.Test;

public class CampaignExecutionReportDtoTest {

    @Test
    public void should_compute_duration_for_one_scenario_execution() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime campaignExecutionStartDate = now.minus(10, ChronoUnit.MILLIS);
        long duration = 3;

        List<ScenarioExecutionReportOutlineDto> executions = stubScenarioExecution(singletonList(now), singletonList(duration));
        CampaignExecutionReportDto sut = fakeCampaignReport(campaignExecutionStartDate, executions);

        assertThat(sut.getDuration()).isEqualTo(13);
    }

    @Test
    public void should_compute_duration_for_two_scenarios_sequential_executions() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime campaignExecutionStartDate = now.minus(10, ChronoUnit.MILLIS);
        long duration1 = 3;
        LocalDateTime scenarioExecutionStartDate2 = now.plus(duration1, ChronoUnit.MILLIS);
        long duration2 = 6;

        List<ScenarioExecutionReportOutlineDto> executions =
            stubScenarioExecution(
                asList(now, scenarioExecutionStartDate2),
                asList(duration1, duration2)
            );
        CampaignExecutionReportDto sut = fakeCampaignReport(campaignExecutionStartDate, executions);

        assertThat(sut.getDuration()).isEqualTo(19);
    }

    @Test
    public void should_compute_duration_for_two_scenarios_parallel_executions() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime campaignExecutionStartDate = now.minus(10, ChronoUnit.MILLIS);
        long duration1 = 3;
        LocalDateTime scenarioExecutionStartDate2 = now.plus(1, ChronoUnit.MILLIS);
        long duration2 = 6;

        List<ScenarioExecutionReportOutlineDto> executions =
            stubScenarioExecution(
                asList(now, scenarioExecutionStartDate2),
                asList(duration1, duration2)
            );
        CampaignExecutionReportDto sut = fakeCampaignReport(campaignExecutionStartDate, executions);

        assertThat(sut.getDuration()).isEqualTo(17);
    }

    private List<ScenarioExecutionReportOutlineDto> stubScenarioExecution(List<LocalDateTime> times, List<Long> durations) {
        ExecutionHistory.ExecutionSummary execution = mock(ExecutionHistory.ExecutionSummary.class);
        when(execution.time()).thenReturn(times.get(0), times.subList(1, durations.size()).toArray(new LocalDateTime[0]));
        when(execution.duration()).thenReturn(durations.get(0), durations.subList(1, durations.size()).toArray(new Long[0]));

        ScenarioExecutionReportOutlineDto dto = new ScenarioExecutionReportOutlineDto("0", UUID.randomUUID().toString(), execution);
        List<ScenarioExecutionReportOutlineDto> l = new ArrayList<>();
        IntStream.range(0, times.size()).forEach(i -> l.add(dto));
        return l;
    }

    private CampaignExecutionReportDto fakeCampaignReport(LocalDateTime startDate, List<ScenarioExecutionReportOutlineDto> executions) {
        return new CampaignExecutionReportDto(1L, executions, "...", startDate, ServerReportStatus.SUCCESS, false, "");
    }
}
