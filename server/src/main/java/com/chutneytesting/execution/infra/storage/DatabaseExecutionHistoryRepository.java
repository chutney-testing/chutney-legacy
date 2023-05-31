package com.chutneytesting.execution.infra.storage;

import static com.google.common.base.Strings.isNullOrEmpty;
import static java.util.Collections.emptyList;
import static java.util.Optional.ofNullable;

import com.chutneytesting.campaign.infra.CampaignJpaRepository;
import com.chutneytesting.execution.infra.storage.jpa.ScenarioExecution;
import com.chutneytesting.execution.infra.storage.jpa.ScenarioExecutionReport;
import com.chutneytesting.scenario.infra.raw.ScenarioJpaRepository;
import com.chutneytesting.server.core.domain.execution.history.ExecutionHistory.DetachedExecution;
import com.chutneytesting.server.core.domain.execution.history.ExecutionHistory.Execution;
import com.chutneytesting.server.core.domain.execution.history.ExecutionHistory.ExecutionSummary;
import com.chutneytesting.server.core.domain.execution.history.ExecutionHistoryRepository;
import com.chutneytesting.server.core.domain.execution.history.ImmutableExecutionHistory;
import com.chutneytesting.server.core.domain.execution.report.ReportNotFoundException;
import com.chutneytesting.server.core.domain.execution.report.ServerReportStatus;
import com.chutneytesting.server.core.domain.scenario.TestCaseRepository;
import com.chutneytesting.server.core.domain.scenario.campaign.CampaignExecutionReport;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Transactional
class DatabaseExecutionHistoryRepository implements ExecutionHistoryRepository {

    private final DatabaseExecutionJpaRepository scenarioExecutionsJpaRepository;
    private final ScenarioExecutionReportJpaRepository scenarioExecutionReportJpaRepository;
    private final CampaignJpaRepository campaignJpaRepository;
    private final TestCaseRepository testCaseRepository;

    DatabaseExecutionHistoryRepository(
        DatabaseExecutionJpaRepository scenarioExecutionsJpaRepository,
        ScenarioExecutionReportJpaRepository scenarioExecutionReportJpaRepository,
        ScenarioJpaRepository scenarioJpaRepository,
        CampaignJpaRepository campaignJpaRepository, TestCaseRepository testCaseRepository) {
        this.scenarioExecutionsJpaRepository = scenarioExecutionsJpaRepository;
        this.scenarioExecutionReportJpaRepository = scenarioExecutionReportJpaRepository;
        this.campaignJpaRepository = campaignJpaRepository;
        this.testCaseRepository = testCaseRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, ExecutionSummary> getLastExecutions(List<String> scenarioIds) {
        List<String> scenarioIdL = scenarioIds.stream().filter(id -> !invalidScenarioId(id)).toList();
        Iterable<ScenarioExecution> lastExecutions = scenarioExecutionsJpaRepository.findAllById(
            scenarioExecutionsJpaRepository.findLastExecutionsByScenarioId(scenarioIdL)
                .stream().map(t -> t.get(0, Long.class)).toList()
        );
        return StreamSupport.stream(lastExecutions.spliterator(), true)
            .collect(Collectors.toMap(ScenarioExecution::scenarioId, ScenarioExecution::toDomain));
    }

    @Override
    @Transactional(readOnly = true)
    public List<ExecutionSummary> getExecutions(String scenarioId) {
        if (invalidScenarioId(scenarioId)) {
            return emptyList();
        }
        List<ScenarioExecution> scenarioExecutions = scenarioExecutionsJpaRepository.findFirst20ByScenarioIdOrderByIdDesc(scenarioId);
        return scenarioExecutions.stream()
            .map(this::scenarioExecutionToExecutionSummary)
            .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public ExecutionSummary getExecutionSummary(Long executionId) {
        return scenarioExecutionsJpaRepository.findById(executionId)
            .map(this::scenarioExecutionToExecutionSummary)
            .orElseThrow(
                () -> new ReportNotFoundException(executionId)
            );
    }

    private ExecutionSummary scenarioExecutionToExecutionSummary(ScenarioExecution scenarioExecution) {
        CampaignExecutionReport campaignExecutionReport = ofNullable(scenarioExecution.campaignExecution())
            .map(ce -> ce.toDomain(campaignJpaRepository.findById(ce.campaignId()).get(), false, null))
            .orElse(null);
        return scenarioExecution.toDomain(campaignExecutionReport);
    }

    @Override
    public Execution store(String scenarioId, DetachedExecution detachedExecution) throws IllegalStateException {
        if(invalidScenarioId(scenarioId)) {
            throw new IllegalStateException("Scenario id is null or empty");
        }
        ScenarioExecution scenarioExecution = ScenarioExecution.fromDomain(scenarioId, detachedExecution);
        scenarioExecution = scenarioExecutionsJpaRepository.save(scenarioExecution);
        scenarioExecutionReportJpaRepository.save(new ScenarioExecutionReport(scenarioExecution, detachedExecution.report()));
        Execution execution = detachedExecution.attach(scenarioExecution.id());
        return ImmutableExecutionHistory.Execution.builder().from(execution).build();
    }

    @Override
    @Transactional(readOnly = true)
    // TODO remove scenarioId params
    public Execution getExecution(String scenarioId, Long reportId) throws ReportNotFoundException {
        if (invalidScenarioId(scenarioId) || testCaseRepository.findById(scenarioId).isEmpty()) {
            throw new ReportNotFoundException(scenarioId, reportId);
        }
        return scenarioExecutionReportJpaRepository.findById(reportId).map(ScenarioExecutionReport::toDomain)
            .orElseThrow(
                () -> new ReportNotFoundException(scenarioId, reportId)
            );
    }

    @Override
    public void update(String scenarioId, Execution updatedExecution) throws ReportNotFoundException {
        if (!scenarioExecutionsJpaRepository.existsById(updatedExecution.executionId())) {
            throw new ReportNotFoundException(scenarioId, updatedExecution.executionId());
        }
        update(updatedExecution);
    }

    private void update(Execution updatedExecution) throws ReportNotFoundException {
        ScenarioExecution execution = scenarioExecutionsJpaRepository.findById(updatedExecution.executionId()).orElseThrow(
            () -> new ReportNotFoundException(updatedExecution.executionId())
        );

        execution.updateFromExecution(updatedExecution);
        scenarioExecutionsJpaRepository.save(execution);
        updateReport(updatedExecution);
    }

    private void updateReport(Execution execution) throws ReportNotFoundException {
        ScenarioExecutionReport scenarioExecutionReport = scenarioExecutionReportJpaRepository.findById(execution.executionId()).orElseThrow(
            () -> new ReportNotFoundException(execution.executionId())
        );
        scenarioExecutionReport.updateReport(execution);
        scenarioExecutionReportJpaRepository.save(scenarioExecutionReport);
    }

    @Override
    public int setAllRunningExecutionsToKO() {
        List<ExecutionSummary> runningExecutions = getExecutionsWithStatus(ServerReportStatus.RUNNING);
        updateExecutionsToKO(runningExecutions);

        List<ExecutionSummary> pausedExecutions = getExecutionsWithStatus(ServerReportStatus.PAUSED);
        updateExecutionsToKO(pausedExecutions);

        return runningExecutions.size() + pausedExecutions.size();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ExecutionSummary> getExecutionsWithStatus(ServerReportStatus status) {
        return scenarioExecutionsJpaRepository.findByStatus(status).stream().map(ScenarioExecution::toDomain).toList();
    }

    private void updateExecutionsToKO(List<ExecutionSummary> executions) {
        executions.stream()
            .map(this::buildKnockoutExecutionFrom)
            .forEach(this::update);
    }

    private ImmutableExecutionHistory.Execution buildKnockoutExecutionFrom(ExecutionSummary executionSummary) {
        return ImmutableExecutionHistory.Execution.builder()
            .executionId(executionSummary.executionId())
            .status(ServerReportStatus.FAILURE)
            .time(executionSummary.time())
            .duration(executionSummary.duration())
            .info(executionSummary.info())
            .error("Execution was interrupted !")
            .report("")
            .testCaseTitle(executionSummary.testCaseTitle())
            .environment(executionSummary.environment())
            .user(executionSummary.user())
            .build();
    }

    private boolean invalidScenarioId(String scenarioId) {
        return isNullOrEmpty(scenarioId);
    }
}
