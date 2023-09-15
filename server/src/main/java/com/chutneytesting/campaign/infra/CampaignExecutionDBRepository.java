package com.chutneytesting.campaign.infra;

import com.chutneytesting.campaign.domain.CampaignExecutionRepository;
import com.chutneytesting.campaign.domain.CampaignNotFoundException;
import com.chutneytesting.campaign.infra.jpa.CampaignEntity;
import com.chutneytesting.campaign.infra.jpa.CampaignExecution;
import com.chutneytesting.execution.domain.campaign.CampaignExecutionNotFoundException;
import com.chutneytesting.execution.infra.storage.DatabaseExecutionJpaRepository;
import com.chutneytesting.execution.infra.storage.jpa.ScenarioExecution;
import com.chutneytesting.server.core.domain.scenario.TestCaseMetadata;
import com.chutneytesting.server.core.domain.scenario.TestCaseRepository;
import com.chutneytesting.server.core.domain.scenario.campaign.CampaignExecutionReport;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@Transactional
public class CampaignExecutionDBRepository implements CampaignExecutionRepository {

    private final CampaignExecutionJpaRepository campaignExecutionJpaRepository;
    private final CampaignJpaRepository campaignJpaRepository;
    private final DatabaseExecutionJpaRepository scenarioExecutionJpaRepository;
    private final TestCaseRepository testCaseRepository;
    private final Map<Long, CampaignExecutionReport> currentCampaignExecutions = new ConcurrentHashMap<>();

    public CampaignExecutionDBRepository(
        CampaignExecutionJpaRepository campaignExecutionJpaRepository,
        CampaignJpaRepository campaignJpaRepository,
        DatabaseExecutionJpaRepository scenarioExecutionJpaRepository,
        TestCaseRepository testCaseRepository) {
        this.campaignExecutionJpaRepository = campaignExecutionJpaRepository;
        this.campaignJpaRepository = campaignJpaRepository;
        this.scenarioExecutionJpaRepository = scenarioExecutionJpaRepository;
        this.testCaseRepository = testCaseRepository;
    }

    @Transactional(readOnly = true)
    public List<CampaignExecutionReport> findExecutionHistory(Long campaignId) {
        CampaignEntity campaign = campaignJpaRepository.findById(campaignId).orElseThrow(() -> new CampaignNotFoundException(campaignId));
        return campaignExecutionJpaRepository.findFirst20ByCampaignIdOrderByIdDesc(campaignId).stream()
            .map(ce -> toDomain(campaign, ce, false))
            .filter(Objects::nonNull)
            .collect(Collectors.toCollection(ArrayList::new));
    }

    public void saveCampaignReport(Long campaignId, CampaignExecutionReport report) {
        CampaignExecution execution = campaignExecutionJpaRepository.findById(report.executionId).orElseThrow(
            () -> new CampaignExecutionNotFoundException(report.executionId)
        );
        Iterable<ScenarioExecution> scenarioExecutions =
            scenarioExecutionJpaRepository.findAllById(report.scenarioExecutionReports().stream()
                .map(serc -> serc.execution.executionId())
                .toList());
        execution.updateFromDomain(report, scenarioExecutions);
        campaignExecutionJpaRepository.save(execution);
    }

    @Transactional(readOnly = true)
    public List<CampaignExecutionReport> findLastExecutions(Long numberOfExecution) {
        return campaignExecutionJpaRepository.findAll(
                null,
                PageRequest.of(0, numberOfExecution.intValue(), Sort.by(Sort.Direction.DESC, "id"))).stream()
            .map(ce -> toDomain(ce, false))
            .filter(Objects::nonNull)
            .toList();
    }

    @Transactional(readOnly = true)
    public CampaignExecutionReport getCampaignExecutionReportsById(Long campaignExecId) {
        return campaignExecutionJpaRepository.findById(campaignExecId)
            .map(ce -> toDomain(ce, true))
            .orElseThrow(() -> new CampaignExecutionNotFoundException(campaignExecId));
    }

    private CampaignExecutionReport toDomain(CampaignExecution campaignExecution, boolean withRunning) {
        return toDomain(campaignJpaRepository.findById(campaignExecution.campaignId()).get(), campaignExecution, withRunning);
    }

    private CampaignExecutionReport toDomain(CampaignEntity campaign, CampaignExecution campaignExecution, boolean withRunning) {
        if (!withRunning && isCampaignExecutionRunning(campaignExecution)) return null;
        return campaignExecution.toDomain(campaign, isCampaignExecutionRunning(campaignExecution), this::title);
    }

    public void clearAllExecutionHistory(Long campaignId) {
        List<CampaignExecution> campaignExecutions = campaignExecutionJpaRepository.findAllByCampaignId(campaignId);
        List<ScenarioExecution> scenarioExecutions = campaignExecutions.stream().flatMap(ce -> ce.scenarioExecutions().stream()).toList();
        scenarioExecutions.forEach(ScenarioExecution::clearCampaignExecution);
        scenarioExecutionJpaRepository.saveAll(scenarioExecutions);
        campaignExecutionJpaRepository.deleteAll(campaignExecutions);
    }

    public Long generateCampaignExecutionId(Long campaignId) {
        CampaignExecution newExecution = new CampaignExecution(campaignId);
        campaignExecutionJpaRepository.save(newExecution);
        return newExecution.id();
    }

    private String title(String scenarioId) {
        return testCaseRepository.findMetadataById(scenarioId).map(TestCaseMetadata::title).orElse("");
    }

    private boolean isCampaignExecutionRunning(CampaignExecution campaignExecution) {
        return currentExecution(campaignExecution.campaignId())
            .map(cer -> cer.executionId.equals(campaignExecution.id()))
            .orElse(false);
    }

    @Override
    public Optional<CampaignExecutionReport> currentExecution(Long campaignId) {
        return Optional.ofNullable(campaignId)
            .map(id -> currentCampaignExecutions.get(campaignId));
    }

    @Override
    public List<CampaignExecutionReport> currentExecutions() {
        return new ArrayList<>(currentCampaignExecutions.values());
    }

    @Override
    public void startExecution(Long campaignId, CampaignExecutionReport campaignExecutionReport) {
        currentCampaignExecutions.put(campaignId, campaignExecutionReport);
    }

    @Override
    public void stopExecution(Long campaignId) {
        currentCampaignExecutions.remove(campaignId);
    }
}
