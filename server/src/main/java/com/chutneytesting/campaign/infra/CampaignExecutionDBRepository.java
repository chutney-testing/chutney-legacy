package com.chutneytesting.campaign.infra;

import com.chutneytesting.campaign.domain.CampaignExecutionRepository;
import com.chutneytesting.campaign.domain.CampaignNotFoundException;
import com.chutneytesting.campaign.infra.jpa.CampaignEntity;
import com.chutneytesting.campaign.infra.jpa.CampaignExecutionEntity;
import com.chutneytesting.execution.domain.campaign.CampaignExecutionNotFoundException;
import com.chutneytesting.execution.infra.storage.DatabaseExecutionJpaRepository;
import com.chutneytesting.execution.infra.storage.jpa.ScenarioExecutionEntity;
import com.chutneytesting.server.core.domain.scenario.TestCaseMetadata;
import com.chutneytesting.server.core.domain.scenario.TestCaseRepository;
import com.chutneytesting.server.core.domain.scenario.campaign.CampaignExecution;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
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
    private final Map<Long, CampaignExecution> currentCampaignExecutions = new ConcurrentHashMap<>();

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

    @Override
    public Optional<CampaignExecution> currentExecution(Long campaignId) {
        return Optional.ofNullable(campaignId)
            .map(id -> currentCampaignExecutions.get(campaignId));
    }

    @Override
    public List<CampaignExecution> currentExecutions() {
        return new ArrayList<>(currentCampaignExecutions.values());
    }

    @Override
    public void startExecution(Long campaignId, CampaignExecution campaignExecution) {
        currentCampaignExecutions.put(campaignId, campaignExecution);
    }

    @Override
    public void stopExecution(Long campaignId) {
        currentCampaignExecutions.remove(campaignId);
    }

    @Override
    public CampaignExecution getLastExecution(Long campaignId) {
        return campaignExecutionJpaRepository
            .findFirstByCampaignIdOrderByIdDesc(campaignId)
            .map(campaignExecution -> toDomain(campaignExecution, true))
            .orElseThrow(() -> new CampaignExecutionNotFoundException(campaignId));
    }

    @Override
    public void deleteExecutions(Set<Long> executionsIds) {
        List<CampaignExecutionEntity> executions = campaignExecutionJpaRepository.findAllById(executionsIds);
        List<ScenarioExecutionEntity> scenarioExecutions = executions.stream().flatMap(cer -> cer.scenarioExecutions().stream()).toList();
        scenarioExecutions.forEach(ScenarioExecutionEntity::clearCampaignExecution);
        scenarioExecutionJpaRepository.saveAll(scenarioExecutions);
        campaignExecutionJpaRepository.deleteAllInBatch(executions);
    }

    @Transactional(readOnly = true)
    public List<CampaignExecution> findExecutionHistory(Long campaignId) {
        CampaignEntity campaign = campaignJpaRepository.findById(campaignId).orElseThrow(() -> new CampaignNotFoundException(campaignId));
        return campaignExecutionJpaRepository.findByCampaignIdOrderByIdDesc(campaignId).stream()
            .map(ce -> toDomainWithCampaign(campaign, ce, false))
            .filter(Objects::nonNull)
            .collect(Collectors.toCollection(ArrayList::new));
    }

    public void saveCampaignExecution(Long campaignId, CampaignExecution campaignExecution) {
        CampaignExecutionEntity execution = campaignExecutionJpaRepository.findById(campaignExecution.executionId).orElseThrow(
            () -> new CampaignExecutionNotFoundException(campaignExecution.executionId, campaignId)
        );
        Iterable<ScenarioExecutionEntity> scenarioExecutions =
            scenarioExecutionJpaRepository.findAllById(campaignExecution.scenarioExecutionReports().stream()
                .map(serc -> serc.execution.executionId())
                .toList());
        execution.updateFromDomain(campaignExecution, scenarioExecutions);
        campaignExecutionJpaRepository.save(execution);
    }

    @Transactional(readOnly = true)
    public List<CampaignExecution> findLastExecutions(Long numberOfExecution) {
        return campaignExecutionJpaRepository.findAll(
                PageRequest.of(0, numberOfExecution.intValue(), Sort.by(Sort.Direction.DESC, "id"))).stream()
            .map(ce -> toDomain(ce, false))
            .filter(Objects::nonNull)
            .toList();
    }

    @Transactional(readOnly = true)
    public CampaignExecution getCampaignExecutionById(Long campaignExecId) {
        return campaignExecutionJpaRepository.findById(campaignExecId)
            .map(ce -> toDomain(ce, true))
            .orElseThrow(() -> new CampaignExecutionNotFoundException(campaignExecId, null));
    }

    private CampaignExecution toDomain(CampaignExecutionEntity campaignExecutionEntity, boolean withRunning) {
        return toDomainWithCampaign(campaignJpaRepository.findById(campaignExecutionEntity.campaignId()).get(), campaignExecutionEntity, withRunning);
    }

    private CampaignExecution toDomainWithCampaign(CampaignEntity campaign, CampaignExecutionEntity campaignExecutionEntity, boolean withRunning) {
        if (!withRunning && isCampaignExecutionRunning(campaignExecutionEntity)) return null;
        return campaignExecutionEntity.toDomain(campaign, isCampaignExecutionRunning(campaignExecutionEntity), this::title);
    }

    void clearAllExecutionHistory(Long campaignId) {
        List<CampaignExecutionEntity> campaignExecutionEntities = campaignExecutionJpaRepository.findAllByCampaignId(campaignId);
        List<ScenarioExecutionEntity> scenarioExecutions = campaignExecutionEntities.stream().flatMap(ce -> ce.scenarioExecutions().stream()).toList();
        scenarioExecutions.forEach(ScenarioExecutionEntity::clearCampaignExecution);
        scenarioExecutionJpaRepository.saveAll(scenarioExecutions);
        campaignExecutionJpaRepository.deleteAllInBatch(campaignExecutionEntities);
    }

    public Long generateCampaignExecutionId(Long campaignId) {
        CampaignExecutionEntity newExecution = new CampaignExecutionEntity(campaignId);
        campaignExecutionJpaRepository.save(newExecution);
        return newExecution.id();
    }

    private String title(String scenarioId) {
        return testCaseRepository.findMetadataById(scenarioId).map(TestCaseMetadata::title).orElse("");
    }

    private boolean isCampaignExecutionRunning(CampaignExecutionEntity campaignExecutionEntity) {
        return currentExecution(campaignExecutionEntity.campaignId())
            .map(cer -> cer.executionId.equals(campaignExecutionEntity.id()))
            .orElse(false);
    }
}
