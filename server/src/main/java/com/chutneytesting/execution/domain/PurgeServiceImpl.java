package com.chutneytesting.execution.domain;

import static com.chutneytesting.server.core.domain.execution.report.ServerReportStatus.SUCCESS;
import static java.util.Collections.emptyList;
import static java.util.Collections.emptySet;
import static java.util.Comparator.comparing;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toSet;

import com.chutneytesting.campaign.domain.CampaignExecutionRepository;
import com.chutneytesting.campaign.domain.CampaignRepository;
import com.chutneytesting.server.core.domain.execution.history.ExecutionHistory.ExecutionSummary;
import com.chutneytesting.server.core.domain.execution.history.ExecutionHistoryRepository;
import com.chutneytesting.server.core.domain.execution.history.PurgeService;
import com.chutneytesting.server.core.domain.execution.report.ServerReportStatus;
import com.chutneytesting.server.core.domain.scenario.TestCaseMetadata;
import com.chutneytesting.server.core.domain.scenario.TestCaseRepository;
import com.chutneytesting.server.core.domain.scenario.campaign.Campaign;
import com.chutneytesting.server.core.domain.scenario.campaign.CampaignExecutionReport;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PurgeServiceImpl implements PurgeService {
    private static final Logger LOGGER = LoggerFactory.getLogger(PurgeServiceImpl.class);
    private final PurgeExecutionService<Campaign, Long, CampaignExecutionReport> campaignPurgeService;
    private final PurgeExecutionService<TestCaseMetadata, String, ExecutionSummary> scenarioPurgeService;

    public PurgeServiceImpl(
        TestCaseRepository testCaseRepository,
        ExecutionHistoryRepository executionsRepository,
        CampaignRepository campaignRepository,
        CampaignExecutionRepository campaignExecutionRepository,
        Integer maxScenarioExecutionsConfiguration,
        Integer maxCampaignExecutionsConfiguration
    ) {
        validateConfigurationLimits(maxScenarioExecutionsConfiguration, maxCampaignExecutionsConfiguration);

        this.scenarioPurgeService = new PurgeExecutionService<>(
            maxScenarioExecutionsConfiguration,
            testCaseRepository::findAll,
            TestCaseMetadata::id,
            executionsRepository::getExecutions,
            es -> es.campaignReport().isEmpty(),
            ExecutionSummary::executionId,
            ExecutionSummary::time,
            ExecutionSummary::status,
            ExecutionSummary::environment,
            executionsRepository::deleteExecutions
        );

        this.campaignPurgeService = new PurgeExecutionService<>(
            maxCampaignExecutionsConfiguration,
            campaignRepository::findAll,
            campaign -> campaign.id,
            campaignRepository::findExecutionsById,
            cer -> true,
            cer -> cer.executionId,
            cer -> cer.startDate,
            CampaignExecutionReport::getStatus,
            cer -> cer.executionEnvironment,
            campaignExecutionRepository::deleteExecutions
        ) {
            private Map<Boolean, List<CampaignExecutionReport>> campaignExecutionsByPartialExecution;

            @Override
            public List<CampaignExecutionReport> handleExecutionsForOneEnvironment(List<CampaignExecutionReport> executionsFromOneEnvironment) {
                campaignExecutionsByPartialExecution = executionsFromOneEnvironment.stream()
                    .collect(groupingBy(cer -> cer.partialExecution));
                return campaignExecutionsByPartialExecution.get(false);
            }

            @Override
            public Collection<Long> findExtraExecutionsIdsToDelete() {
                var oldestCampaignExecutionToKept = currentTimeSortedExecutions().get(maxCampaignExecutionsConfiguration - 1);
                return ofNullable(campaignExecutionsByPartialExecution.get(true)).orElse(emptyList()).stream()
                    .filter(cer -> cer.startDate.isBefore(oldestCampaignExecutionToKept.startDate))
                    .map(cer -> cer.executionId)
                    .toList();
            }
        };
    }

    private void validateConfigurationLimits(Integer maxScenarioExecutionsConfiguration, Integer maxCampaignExecutionsConfiguration) {
        if (maxScenarioExecutionsConfiguration <= 0 || maxCampaignExecutionsConfiguration <= 0) {
            throw new IllegalArgumentException("Purge configuration limits must be positives.");
        }
    }

    @Override
    public PurgeReport purge() {
        Set<Long> purgedCampaignsExecutionsIds = campaignPurgeService.purgeExecutions();
        Set<Long> purgedScenariosExecutionsIds = scenarioPurgeService.purgeExecutions();
        LOGGER.info("Purge report : {} scenarios' executions deleted - {} campaigns' executions deleted", purgedScenariosExecutionsIds.size(), purgedCampaignsExecutionsIds.size());
        return new PurgeReport(purgedScenariosExecutionsIds, purgedCampaignsExecutionsIds);
    }

    private static class PurgeExecutionService<ObjectType, ObjectIdType, ExecutionReportType> {
        private final int maxExecutionsToKeep;
        private final Supplier<List<ObjectType>> baseObject;
        private final Function<ObjectType, ObjectIdType> idFunction;
        private final Function<ObjectIdType, List<ExecutionReportType>> executionsFunction;
        private final Predicate<ExecutionReportType> executionsFilter;
        private final Function<ExecutionReportType, Long> executionIdFunction;
        private final Function<ExecutionReportType, LocalDateTime> executionDateFunction;
        private final Function<ExecutionReportType, ServerReportStatus> statusFunction;
        private final Function<ExecutionReportType, String> environmentFunction;
        private final Function<Set<Long>, Set<ExecutionReportType>> deleteFunction;

        private List<ExecutionReportType> timeSortedExecutionsForOneEnvironment;

        private PurgeExecutionService(
            int maxExecutionsToKeep,
            Supplier<List<ObjectType>> baseObjectSupplier,
            Function<ObjectType, ObjectIdType> idFunction,
            Function<ObjectIdType, List<ExecutionReportType>> executionsFunction,
            Predicate<ExecutionReportType> executionsFilter,
            Function<ExecutionReportType, Long> executionIdFunction,
            Function<ExecutionReportType, LocalDateTime> executionDateFunction,
            Function<ExecutionReportType, ServerReportStatus> statusFunction,
            Function<ExecutionReportType, String> environmentFunction,
            Function<Set<Long>, Set<ExecutionReportType>> deleteFunction
        ) {
            this.maxExecutionsToKeep = maxExecutionsToKeep;
            this.baseObject = baseObjectSupplier;
            this.idFunction = idFunction;
            this.executionsFunction = executionsFunction;
            this.executionsFilter = executionsFilter;
            this.executionIdFunction = executionIdFunction;
            this.executionDateFunction = executionDateFunction;
            this.statusFunction = statusFunction;
            this.environmentFunction = environmentFunction;
            this.deleteFunction = deleteFunction;
        }

        Set<Long> purgeExecutions() {
            Set<Long> deletedExecutionsIds = new HashSet<>();
            baseObject.get().stream()
                .map(idFunction)
                .map(executionsFunction)
                .forEach(executionsReports -> {
                    var executionsByEnvironment = executionsReports.stream()
                        .filter(executionsFilter)
                        .collect(groupingBy(environmentFunction));
                    for (List<ExecutionReportType> executionsOneEnvironment : executionsByEnvironment.values()) {
                        deletedExecutionsIds.addAll(
                            purgeOldestExecutionsFromOneEnvironment(
                                handleExecutionsForOneEnvironment(executionsOneEnvironment)
                            )
                        );
                    }
                });
            return deletedExecutionsIds;
        }

        protected final List<ExecutionReportType> currentTimeSortedExecutions() {
            return timeSortedExecutionsForOneEnvironment;
        }

        private Set<Long> purgeOldestExecutionsFromOneEnvironment(List<ExecutionReportType> executionsFromOneEnvironment) {
            if (executionsFromOneEnvironment.size() <= maxExecutionsToKeep) {
                return emptySet();
            }

            timeSortedExecutionsForOneEnvironment = executionsFromOneEnvironment.stream()
                .sorted(comparing(executionDateFunction).reversed())
                .toList();

            Set<Long> deletedExecutionsIdsTmp = new HashSet<>();

            deletedExecutionsIdsTmp.addAll(
                findOldestExecutionsIdsWhileKeepingTheLastSuccess(timeSortedExecutionsForOneEnvironment)
            );

            deletedExecutionsIdsTmp.addAll(
                findExtraExecutionsIdsToDelete()
            );

            if (!deletedExecutionsIdsTmp.isEmpty()) {
                deleteFunction.apply(deletedExecutionsIdsTmp);
            }
            return deletedExecutionsIdsTmp;
        }

        private Collection<Long> findOldestExecutionsIdsWhileKeepingTheLastSuccess(List<ExecutionReportType> timeSortedExecutions) {
            Long youngestSuccessExecutionIdToDelete = youngestSuccessExecutionIdToDelete(timeSortedExecutions);
            return timeSortedExecutions.stream()
                .skip(maxExecutionsToKeep)
                .map(executionIdFunction)
                .filter(id -> !youngestSuccessExecutionIdToDelete.equals(id))
                .collect(toSet());
        }

        // The list parameter must be sorted from younger to oldest
        private Long youngestSuccessExecutionIdToDelete(List<ExecutionReportType> timeSortedExecutions) {
            boolean isSuccessExecutionKept = timeSortedExecutions.stream()
                .limit(maxExecutionsToKeep)
                .map(statusFunction)
                .anyMatch(SUCCESS::equals);

            if (!isSuccessExecutionKept) {
                return timeSortedExecutions.stream()
                    .skip(maxExecutionsToKeep)
                    .filter(es -> SUCCESS.equals(statusFunction.apply(es)))
                    .findFirst()
                    .map(executionIdFunction)
                    .orElse(-1L);
            }
            return -1L;
        }

        protected List<ExecutionReportType> handleExecutionsForOneEnvironment(List<ExecutionReportType> executionsFromOneEnvironment) {
            return executionsFromOneEnvironment;
        }

        protected Collection<Long> findExtraExecutionsIdsToDelete() {
            return emptySet();
        }
    }
}
