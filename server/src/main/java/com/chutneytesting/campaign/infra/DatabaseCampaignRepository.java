package com.chutneytesting.campaign.infra;

import static com.google.common.base.Strings.isNullOrEmpty;
import static java.lang.Long.parseLong;
import static java.util.Collections.emptyList;
import static org.apache.commons.lang3.StringUtils.isNumeric;

import com.chutneytesting.campaign.domain.CampaignNotFoundException;
import com.chutneytesting.campaign.domain.CampaignRepository;
import com.chutneytesting.scenario.infra.jpa.Scenario;
import com.chutneytesting.scenario.infra.raw.DatabaseTestCaseJpaRepository;
import com.chutneytesting.server.core.domain.scenario.campaign.Campaign;
import com.chutneytesting.server.core.domain.scenario.campaign.CampaignExecutionReport;
import java.util.List;
import java.util.Optional;
import java.util.stream.StreamSupport;
import javax.transaction.Transactional;
import org.springframework.stereotype.Repository;

/**
 * Campaign persistence management.
 */
@Repository
public class DatabaseCampaignRepository implements CampaignRepository {

    private final CampaignJpaRepository campaignJpaRepository;
    private final DatabaseTestCaseJpaRepository scenarioJpaRepository;
    private final CampaignExecutionRepository campaignExecutionRepository;

    public DatabaseCampaignRepository(CampaignJpaRepository campaignJpaRepository,
                                      DatabaseTestCaseJpaRepository scenarioJpaRepository,
                                      CampaignExecutionRepository campaignExecutionRepository) {
        this.campaignJpaRepository = campaignJpaRepository;
        this.scenarioJpaRepository = scenarioJpaRepository;
        this.campaignExecutionRepository = campaignExecutionRepository;
    }

    @Override
    @Transactional
    public Campaign createOrUpdate(Campaign campaign) {
        List<Scenario> scenarios = campaign.scenarioIds.stream().map(Long::valueOf)
            .map(scenarioJpaRepository::findById)
            .flatMap(Optional::stream)
            .toList();
        if (campaign.scenarioIds.size() != scenarios.size()) {
            throw new RuntimeException("Some scenarios don't exist");
        }

        com.chutneytesting.campaign.infra.jpa.Campaign campaignJpa = campaignJpaRepository.save(com.chutneytesting.campaign.infra.jpa.Campaign.fromDomain(campaign, scenarios));
        return campaignJpa.toDomain();
    }

    @Override
    @Transactional
    public void saveReport(Long campaignId, CampaignExecutionReport report) {
        campaignExecutionRepository.saveCampaignReport(campaignId, report);
    }

    @Override
    @Transactional
    public boolean removeById(Long id) {
        if (campaignJpaRepository.existsById(id)) {
            campaignExecutionRepository.clearAllExecutionHistory(id);
            campaignJpaRepository.deleteById(id);
            return true;
        }
        return false;
    }

    @Override
    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public Campaign findById(Long campaignId) throws CampaignNotFoundException {
        return campaignJpaRepository.findById(campaignId)
            .map(com.chutneytesting.campaign.infra.jpa.Campaign::toDomain)
            .orElseThrow(() -> new CampaignNotFoundException(campaignId));
    }

    @Override
    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public List<Campaign> findByName(String campaignName) {
        return campaignJpaRepository.findAll((root, query, criteriaBuilder) ->
                criteriaBuilder.like(criteriaBuilder.lower(root.get("title")), campaignName.toLowerCase()))
            .stream()
            .map(com.chutneytesting.campaign.infra.jpa.Campaign::toDomain)
            .toList();
    }

    @Override
    public List<CampaignExecutionReport> findLastExecutions(Long numberOfExecution) {
        return campaignExecutionRepository.findLastExecutions(numberOfExecution);
    }

    @Override
    public List<String> findScenariosIds(Long campaignId) {
        return campaignJpaRepository.findById(campaignId)
            .map(c -> c.scenarios().stream().map(String::valueOf).toList())
            .orElseThrow(() -> new CampaignNotFoundException(campaignId));
    }

    @Override
    public Long newCampaignExecution() {
        return campaignExecutionRepository.generateCampaignExecutionId();
    }

    @Override
    public List<Campaign> findAll() {
        return StreamSupport.stream(campaignJpaRepository.findAll().spliterator(), false)
            .map(com.chutneytesting.campaign.infra.jpa.Campaign::toDomain)
            .toList();
    }

    @Override
    public List<CampaignExecutionReport> findExecutionsById(Long campaignId) {
        return campaignExecutionRepository.findExecutionHistory(campaignId);
    }

    @Override
    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public List<Campaign> findCampaignsByScenarioId(String scenarioId) {
        if (isNullOrEmpty(scenarioId) || !isNumeric(scenarioId)) {
            return emptyList();
        }

        long scenarioIdL = parseLong(scenarioId);
        return scenarioJpaRepository.findById(scenarioIdL).stream()
            .flatMap(s -> s.campaigns().stream())
            .map(com.chutneytesting.campaign.infra.jpa.Campaign::toDomain)
            .toList();
    }

    @Override
    public CampaignExecutionReport findByExecutionId(Long campaignExecutionId) {
        return campaignExecutionRepository.getCampaignExecutionReportsById(campaignExecutionId);
    }
}
