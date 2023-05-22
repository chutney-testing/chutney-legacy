package com.chutneytesting.campaign.infra;

import com.chutneytesting.campaign.domain.CampaignNotFoundException;
import com.chutneytesting.campaign.infra.jpa.Campaign;
import com.chutneytesting.campaign.infra.jpa.CampaignExecution;
import com.chutneytesting.execution.domain.campaign.CampaignExecutionNotFoundException;
import com.chutneytesting.execution.infra.storage.DatabaseExecutionJpaRepository;
import com.chutneytesting.execution.infra.storage.jpa.ScenarioExecution;
import com.chutneytesting.server.core.domain.scenario.campaign.CampaignExecutionReport;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@Transactional
public class CampaignExecutionRepository {

    private final CampaignExecutionJpaRepository campaignExecutionJpaRepository;
    private final CampaignJpaRepository campaignJpaRepository;
    private final DatabaseExecutionJpaRepository scenarioExecutionJpaRepository;

    public CampaignExecutionRepository(
        CampaignExecutionJpaRepository campaignExecutionJpaRepository,
        CampaignJpaRepository campaignJpaRepository,
        DatabaseExecutionJpaRepository scenarioExecutionJpaRepository
    ) {
        this.campaignExecutionJpaRepository = campaignExecutionJpaRepository;
        this.campaignJpaRepository = campaignJpaRepository;
        this.scenarioExecutionJpaRepository = scenarioExecutionJpaRepository;
    }

    @Transactional(readOnly = true)
    public List<CampaignExecutionReport> findExecutionHistory(Long campaignId) {
        Campaign campaign = campaignJpaRepository.findById(campaignId).orElseThrow(() -> new CampaignNotFoundException(campaignId));
        return campaignExecutionJpaRepository.findFirst20ByCampaignIdOrderByIdDesc(campaignId).stream()
            .map(campaignExecution -> campaignExecution.toDomain(campaign))
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
            .map(campaignExecution -> campaignExecution.toDomain(campaignJpaRepository.findById(campaignExecution.campaignId()).get()))
            .toList();
    }

    @Transactional(readOnly = true)
    public CampaignExecutionReport getCampaignExecutionReportsById(Long campaignExecId) {
        return campaignExecutionJpaRepository.findById(campaignExecId)
            .map(campaignExecution -> campaignExecution.toDomain(campaignJpaRepository.findById(campaignExecution.campaignId()).get()))
            .orElseThrow(() -> new CampaignExecutionNotFoundException(campaignExecId));
    }

    public void clearAllExecutionHistory(Long campaignId) {
        campaignExecutionJpaRepository.deleteAllById(
            campaignExecutionJpaRepository.findAllByCampaignId(campaignId).stream().map(CampaignExecution::id).toList()
        );
    }

    public Long generateCampaignExecutionId(Long campaignId) {
        CampaignExecution newExecution = new CampaignExecution(campaignId);
        campaignExecutionJpaRepository.save(newExecution);
        return newExecution.id();
    }
}
