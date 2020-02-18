package com.chutneytesting.design.api.campaign;

import static org.assertj.core.api.Assertions.assertThat;

import com.chutneytesting.design.api.campaign.dto.CampaignExecutionReportDto;
import com.chutneytesting.design.api.campaign.dto.ScenarioExecutionReportOutlineDto;
import com.chutneytesting.execution.domain.history.ExecutionHistory;
import com.chutneytesting.execution.domain.report.ServerReportStatus;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.Test;
import org.mockito.Mockito;

public class CampaignExecutionReportDtoTest {

    @Test
    public void getDurationWithOneScenarioShouldReturnScenarioDuration() {
        long duration = 3;
        List<ScenarioExecutionReportOutlineDto> executions = generateScenarioExecutions(ExecutionHistory.ExecutionSummary::duration, duration);

        CampaignExecutionReportDto campaignExecutionReport = fakeCampaignReport(executions);

        assertThat(campaignExecutionReport.getDuration()).isEqualTo(duration);
    }

    @Test
    public void getDurationWithTwoScenarioShouldReturnSum() {
        long duration1 = 3;
        long duration2 = 3;
        List<ScenarioExecutionReportOutlineDto> executions = generateScenarioExecutions(ExecutionHistory.ExecutionSummary::duration, duration1, duration2);

        CampaignExecutionReportDto campaignExecutionReport = fakeCampaignReport(executions);

        assertThat(campaignExecutionReport.getDuration()).isEqualTo(6L);
    }

    @SuppressWarnings("unchecked")
    private <T> List<ScenarioExecutionReportOutlineDto> generateScenarioExecutions(Function<ExecutionHistory.ExecutionSummary, T> mockField, T... values) {
        return Stream.of(values).map(object -> {
            ExecutionHistory.ExecutionSummary execution = Mockito.mock(ExecutionHistory.ExecutionSummary.class);
            Mockito.when(mockField.apply(execution)).thenReturn(object);
            return execution;
        }).map(execution -> new ScenarioExecutionReportOutlineDto("0", UUID.randomUUID().toString(), execution)).collect(Collectors.toList());
    }

    private CampaignExecutionReportDto fakeCampaignReport(List<ScenarioExecutionReportOutlineDto> executions) {
        return new CampaignExecutionReportDto(1L, executions, "...", LocalDateTime.now().minusMinutes(1), ServerReportStatus.SUCCESS, false, "");
    }
}
