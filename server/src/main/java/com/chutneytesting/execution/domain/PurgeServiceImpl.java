package com.chutneytesting.execution.domain;

import static com.chutneytesting.server.core.domain.execution.report.ServerReportStatus.SUCCESS;
import static java.util.Collections.emptyList;
import static java.util.Collections.emptySet;
import static java.util.Comparator.comparing;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toSet;
import static java.util.stream.Collectors.toUnmodifiableSet;

import com.chutneytesting.campaign.domain.CampaignExecutionRepository;
import com.chutneytesting.campaign.domain.CampaignRepository;
import com.chutneytesting.server.core.domain.execution.history.ExecutionHistory;
import com.chutneytesting.server.core.domain.execution.history.ExecutionHistory.ExecutionProperties;
import com.chutneytesting.server.core.domain.execution.history.ExecutionHistory.ExecutionSummary;
import com.chutneytesting.server.core.domain.execution.history.ExecutionHistoryRepository;
import com.chutneytesting.server.core.domain.scenario.TestCaseMetadata;
import com.chutneytesting.server.core.domain.scenario.TestCaseRepository;
import com.chutneytesting.server.core.domain.scenario.campaign.CampaignExecutionReport;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class PurgeServiceImpl implements com.chutneytesting.server.core.domain.execution.history.PurgeService {
    private final TestCaseRepository testCaseRepository;
    private final ExecutionHistoryRepository executionsRepository;
    private final CampaignRepository campaignRepository;
    private final CampaignExecutionRepository campaignExecutionRepository;
    private final Integer maxScenarioExecutionsConfiguration;
    private final Integer maxCampaignExecutionsConfiguration;

    public PurgeServiceImpl(
        TestCaseRepository testCaseRepository,
        ExecutionHistoryRepository executionsRepository,
        CampaignRepository campaignRepository,
        CampaignExecutionRepository campaignExecutionRepository,
        Integer maxExecutionsConfiguration
    ) {
        this(testCaseRepository, executionsRepository, campaignRepository, campaignExecutionRepository, maxExecutionsConfiguration, maxExecutionsConfiguration);
    }

    public PurgeServiceImpl(
        TestCaseRepository testCaseRepository,
        ExecutionHistoryRepository executionsRepository,
        CampaignRepository campaignRepository,
        CampaignExecutionRepository campaignExecutionRepository,
        Integer maxScenarioExecutionsConfiguration,
        Integer maxCampaignExecutionsConfiguration
    ) {
        this.testCaseRepository = testCaseRepository;
        this.executionsRepository = executionsRepository;
        this.campaignRepository = campaignRepository;
        this.campaignExecutionRepository = campaignExecutionRepository;
        this.maxScenarioExecutionsConfiguration = maxScenarioExecutionsConfiguration;
        this.maxCampaignExecutionsConfiguration = maxCampaignExecutionsConfiguration;
    }

    @Override
    public PurgeReport purge() {
        Set<Long> deletedScenariosExecutionsIds = new HashSet<>();
        Set<Long> deletedCampaignsExecutionsIds = new HashSet<>();

        campaignRepository.findAll().stream()
            .map(c -> c.id)
            .map(campaignRepository::findExecutionsById)
            .forEach(campaignExecutionsReports -> {
                Map<String, List<CampaignExecutionReport>> campaignExecutionsByEnvironment = campaignExecutionsReports.stream()
                    .collect(groupingBy(cer -> cer.executionEnvironment));
                for (List<CampaignExecutionReport> campaignExecutionsOneEnvironment : campaignExecutionsByEnvironment.values()) {
                    deletedCampaignsExecutionsIds.addAll(
                        deleteCampaignExecutions(campaignExecutionsOneEnvironment)
                    );
                }
            });

        testCaseRepository.findAll().stream()
            .map(TestCaseMetadata::id)
            .map(executionsRepository::getExecutions)
            .forEach(scenarioExecutions -> {
                Map<String, List<ExecutionSummary>> scenarioExecutionsByEnvironment = scenarioExecutions.stream()
                    .filter(se -> se.campaignReport().isEmpty())
                    .collect(groupingBy(ExecutionProperties::environment));
                for (List<ExecutionSummary> scenarioExecutionsOneEnvironment : scenarioExecutionsByEnvironment.values()) {
                    deletedScenariosExecutionsIds.addAll(
                        deletedScenarioExecutions(scenarioExecutionsOneEnvironment)
                    );
                }
            });

        return new PurgeReport(deletedScenariosExecutionsIds, deletedCampaignsExecutionsIds);
    }

    private Set<Long> deleteCampaignExecutions(List<CampaignExecutionReport> campaignExecutionsOneEnvironment) {

        var campaignExecutionsByPartialExecution = campaignExecutionsOneEnvironment.stream()
            .collect(groupingBy(cer -> cer.partialExecution));

        if (campaignExecutionsByPartialExecution.get(false).size() <= maxCampaignExecutionsConfiguration) {
            return emptySet();
        }

        var timeSortedExecutions = campaignExecutionsByPartialExecution.get(false).stream()
            .sorted(Comparator.<CampaignExecutionReport, LocalDateTime>comparing(cer -> cer.startDate).reversed())
            .toList();

        Long youngestSuccessExecutionIdToDelete = youngestSuccessCampaignExecutionIdToDelete(timeSortedExecutions);
        var oldestCampaignExecutionToKept = timeSortedExecutions.get(maxCampaignExecutionsConfiguration - 1);

        Set<Long> deletedCampaignsExecutionsIdsTmp = timeSortedExecutions.stream()
            .skip(maxCampaignExecutionsConfiguration)
            .map(cer -> cer.executionId)
            .filter(id -> !youngestSuccessExecutionIdToDelete.equals(id))
            .collect(toSet());

        deletedCampaignsExecutionsIdsTmp.addAll(
            ofNullable(campaignExecutionsByPartialExecution.get(true)).orElse(emptyList()).stream()
                .filter(cer -> cer.startDate.isBefore(oldestCampaignExecutionToKept.startDate))
                .map(cer -> cer.executionId)
                .toList()
        );

        if (!deletedCampaignsExecutionsIdsTmp.isEmpty()) {
            campaignExecutionRepository.deleteExecutions(deletedCampaignsExecutionsIdsTmp);
        }
        return deletedCampaignsExecutionsIdsTmp;
    }

    private Set<Long> deletedScenarioExecutions(List<ExecutionSummary> scenarioExecutionsOneEnvironment) {
        if (scenarioExecutionsOneEnvironment.size() <= maxScenarioExecutionsConfiguration) {
            return emptySet();
        }

        List<ExecutionSummary> timeSortedExecutions = scenarioExecutionsOneEnvironment.stream()
            .sorted(comparing(ExecutionProperties::time).reversed())
            .toList();

        Long youngestSuccessExecutionIdToDelete = youngestSuccessScenarioExecutionIdToDelete(timeSortedExecutions);

        Set<Long> deletedExecutionsIdsTmp = timeSortedExecutions.stream()
            .skip(maxScenarioExecutionsConfiguration)
            .map(ExecutionSummary::executionId)
            .filter(id -> !youngestSuccessExecutionIdToDelete.equals(id))
            .collect(toUnmodifiableSet());

        if (!deletedExecutionsIdsTmp.isEmpty()) {
            executionsRepository.deleteExecutions(deletedExecutionsIdsTmp);
        }
        return deletedExecutionsIdsTmp;
    }

    // The list parameter must be sorted from younger to oldest
    private Long youngestSuccessScenarioExecutionIdToDelete(List<ExecutionSummary> timeSortedExecutions) {
        boolean isSuccessExecutionKept = timeSortedExecutions.stream()
            .limit(maxScenarioExecutionsConfiguration)
            .map(ExecutionProperties::status)
            .anyMatch(SUCCESS::equals);

        if (!isSuccessExecutionKept) {
            return timeSortedExecutions.stream()
                .skip(maxScenarioExecutionsConfiguration)
                .filter(es -> SUCCESS.equals(es.status()))
                .findFirst()
                .map(ExecutionHistory.Attached::executionId)
                .orElse(-1L);
        }
        return -1L;
    }

    // The list parameter must be sorted from younger to oldest
    private Long youngestSuccessCampaignExecutionIdToDelete(List<CampaignExecutionReport> timeSortedExecutions) {
        boolean isSuccessExecutionKept = timeSortedExecutions.stream()
            .limit(maxCampaignExecutionsConfiguration)
            .map(CampaignExecutionReport::status)
            .anyMatch(SUCCESS::equals);

        if (!isSuccessExecutionKept) {
            return timeSortedExecutions.stream()
                .skip(maxCampaignExecutionsConfiguration)
                .filter(es -> SUCCESS.equals(es.status()))
                .findFirst()
                .map(cer -> cer.executionId)
                .orElse(-1L);
        }
        return -1L;
    }
}
