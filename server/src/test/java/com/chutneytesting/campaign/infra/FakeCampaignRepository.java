package com.chutneytesting.campaign.infra;

import static java.util.Collections.emptyMap;
import static java.util.Optional.ofNullable;
import static org.assertj.core.util.Lists.newArrayList;

import com.chutneytesting.campaign.domain.CampaignNotFoundException;
import com.chutneytesting.campaign.domain.CampaignRepository;
import com.chutneytesting.server.core.domain.scenario.campaign.Campaign;
import com.chutneytesting.server.core.domain.scenario.campaign.CampaignExecutionReport;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;
import org.apache.commons.lang3.NotImplementedException;
import org.assertj.core.util.Lists;

public class FakeCampaignRepository implements CampaignRepository {

    private final AtomicLong sequence = new AtomicLong();
    private final Map<Long, Campaign> campaignsById = new HashMap<>();
    private final Map<Long, List<CampaignExecutionReport>> campaignsExecutionById = new HashMap<>();
    private final Multimap<String, Campaign> campaignsByName = ArrayListMultimap.create();

    @Override
    public Campaign createOrUpdate(Campaign campaign) {
        final Campaign saved;
        if (campaign.id != null && campaignsById.containsKey(campaign.id)) {
            saved = campaign;
        } else {
            saved = new Campaign(sequence.incrementAndGet(), campaign.title, campaign.description, campaign.scenarioIds, emptyMap(), "env", false, false, null, campaign.tags);
        }
        campaignsById.put(saved.id, saved);
        campaignsByName.put(saved.title, saved);

        return saved;
    }

    @Override
    public void saveReport(Long campaignId, CampaignExecutionReport report) {
        ofNullable(campaignsById.get(campaignId)).ifPresent(campaign -> {
            Campaign c = new Campaign(campaign.id, campaign.title, campaign.title, campaign.scenarioIds, campaign.executionParameters, campaign.executionEnvironment(), false, false, null, null);
            createOrUpdate(c);
        });

        List<CampaignExecutionReport> foundReport = campaignsExecutionById.get(campaignId);
        if (foundReport == null) {
            foundReport = Lists.newArrayList();
        }
        foundReport.add(report);
        campaignsExecutionById.put(campaignId, foundReport);

    }

    @Override
    public boolean removeById(Long id) {
        return campaignsById.remove(id) != null;
    }

    @Override
    public Campaign findById(Long campaignId) throws CampaignNotFoundException {
        if (!campaignsById.containsKey(campaignId)) {
            throw new CampaignNotFoundException(campaignId);
        }
        return campaignsById.get(campaignId);
    }

    @Override
    public List<Campaign> findAll() {
        return newArrayList(campaignsById.values());
    }

    @Override
    public List<Campaign> findByName(String campaignName) {
        return newArrayList(campaignsByName.get(campaignName));
    }

    @Override
    public Optional<Campaign> findByNameAndEnvironment(String campaignName, String environment) {
        return campaignsByName
            .get(campaignName)
            .stream()
            .filter(campaign -> campaign.executionEnvironment().equals(environment))
            .findFirst();
    }

    @Override
    public List<CampaignExecutionReport> findExecutionsById(Long campaignId) {
        return ofNullable(campaignsExecutionById.get(campaignId)).orElse(newArrayList());
    }

    @Override
    public List<CampaignExecutionReport> findLastExecutions(Long numberOfExecution) {
        List<CampaignExecutionReport> allExecutions = campaignsExecutionById.entrySet().stream()
            .flatMap(e -> e.getValue().stream())
            .sorted(executionComparatorReportByExecutionId())
            .collect(Collectors.toList());

        if (numberOfExecution < allExecutions.size()) {
            return allExecutions.subList(0, numberOfExecution.intValue());
        } else {
            return allExecutions;
        }
    }

    @Override
    public List<String> findScenariosIds(Long campaignId) {
        return campaignsById.get(campaignId).scenarioIds;
    }

    @Override
    public Long newCampaignExecution(Long campaignId) {
        return new Random(100).nextLong();
    }

    @Override
    public CampaignExecutionReport findByExecutionId(Long campaignExecutionId) {
        throw new NotImplementedException();
    }

    @Override
    public List<Campaign> findCampaignsByScenarioId(String scenarioId) {
        return campaignsById.values().stream()
            .filter(campaign -> campaign.scenarioIds.contains(scenarioId))
            .collect(Collectors.toList());
    }

    // Duplicate of com.chutneytesting.design.api.campaign.CampaignController#executionComparatorReportByExecutionId
    private static Comparator<CampaignExecutionReport> executionComparatorReportByExecutionId() {
        return Comparator.<CampaignExecutionReport>comparingLong(value -> value.executionId).reversed();
    }
}
