package com.chutneytesting.campaign.infra;

import static com.google.common.base.Strings.isNullOrEmpty;
import static java.util.Collections.emptyList;
import static java.util.Optional.ofNullable;

import com.chutneytesting.campaign.domain.CampaignNotFoundException;
import com.chutneytesting.campaign.domain.CampaignRepository;
import com.chutneytesting.campaign.infra.jpa.CampaignEntity;
import com.chutneytesting.campaign.infra.jpa.CampaignScenario;
import com.chutneytesting.server.core.domain.scenario.campaign.Campaign;
import com.chutneytesting.server.core.domain.scenario.campaign.CampaignExecutionReport;
import java.util.List;
import java.util.Optional;
import java.util.stream.StreamSupport;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

/**
 * Campaign persistence management.
 */
@Repository
@Transactional
public class DatabaseCampaignRepository implements CampaignRepository {

    private final CampaignJpaRepository campaignJpaRepository;
    private final CampaignScenarioJpaRepository campaignScenarioJpaRepository;
    private final CampaignExecutionDBRepository campaignExecutionRepository;

    public DatabaseCampaignRepository(CampaignJpaRepository campaignJpaRepository,
                                      CampaignScenarioJpaRepository campaignScenarioJpaRepository,
                                      CampaignExecutionDBRepository campaignExecutionRepository) {
        this.campaignJpaRepository = campaignJpaRepository;
        this.campaignScenarioJpaRepository = campaignScenarioJpaRepository;
        this.campaignExecutionRepository = campaignExecutionRepository;
    }

    @Override
    public Campaign createOrUpdate(Campaign campaign) {
        CampaignEntity campaignJpa =
            campaignJpaRepository.save(CampaignEntity.fromDomain(campaign, lastCampaignVersion(campaign.id)));
        return campaignJpa.toDomain();
    }

    private Integer lastCampaignVersion(Long id) {
        return ofNullable(id).flatMap(campaignJpaRepository::findById).map(CampaignEntity::version).orElse(null);
    }

    @Override
    public void saveReport(Long campaignId, CampaignExecutionReport report) {
        campaignExecutionRepository.saveCampaignReport(campaignId, report);
    }

    @Override
    public boolean removeById(Long id) {
        if (campaignJpaRepository.existsById(id)) {
            campaignExecutionRepository.clearAllExecutionHistory(id);
            campaignJpaRepository.deleteById(id);
            return true;
        }
        return false;
    }

    @Override
    @Transactional(readOnly = true)
    public Campaign findById(Long campaignId) throws CampaignNotFoundException {
        return campaignJpaRepository.findById(campaignId)
            .map(CampaignEntity::toDomain)
            .orElseThrow(() -> new CampaignNotFoundException(campaignId));
    }

    @Override
    @Transactional(readOnly = true)
    public List<Campaign> findByName(String campaignName) {
        return campaignJpaRepository.findAll((root, query, criteriaBuilder) ->
                criteriaBuilder.like(criteriaBuilder.lower(root.get("title")), campaignName.toLowerCase()))
            .stream()
            .map(CampaignEntity::toDomain)
            .toList();
    }

    @Override
    public Optional<Campaign> findByNameAndEnvironment(String campaignName, String environment) {
        return campaignJpaRepository
            .findByTitleAndEnvironment(campaignName, environment)
            .map(CampaignEntity::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CampaignExecutionReport> findLastExecutions(Long numberOfExecution) {
        return campaignExecutionRepository.findLastExecutions(numberOfExecution);
    }

    @Override
    @Transactional(readOnly = true)
    public List<String> findScenariosIds(Long campaignId) {
        return campaignJpaRepository.findById(campaignId)
            .map(c -> c.campaignScenarios().stream()
                .map(CampaignScenario::scenarioId)
                .toList()
            )
            .orElseThrow(() -> new CampaignNotFoundException(campaignId));
    }

    @Override
    public Long newCampaignExecution(Long campaignId) {
        return campaignExecutionRepository.generateCampaignExecutionId(campaignId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Campaign> findAll() {
        return StreamSupport.stream(campaignJpaRepository.findAll().spliterator(), false)
            .map(CampaignEntity::toDomain)
            .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<CampaignExecutionReport> findExecutionsById(Long campaignId) {
        return campaignExecutionRepository.findExecutionHistory(campaignId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Campaign> findCampaignsByScenarioId(String scenarioId) {
        if (isNullOrEmpty(scenarioId)) {
            return emptyList();
        }

        return campaignScenarioJpaRepository.findAllByScenarioId(scenarioId).stream()
            .map(CampaignScenario::campaign)
            .map(CampaignEntity::toDomain)
            .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public CampaignExecutionReport findByExecutionId(Long campaignExecutionId) {
        return campaignExecutionRepository.getCampaignExecutionReportsById(campaignExecutionId);
    }
}
