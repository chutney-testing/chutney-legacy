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
import java.util.Collections;
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
            public Collection<Long> findExtraExecutionsIdsToDelete(List<CampaignExecutionReport> timeSortedExecutionsForOneEnvironment) {
                var oldestCampaignExecutionToKept = timeSortedExecutionsForOneEnvironment.get(maxCampaignExecutionsConfiguration - 1);
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

    /**
     * Core logic to purge executions.
     *
     * @see #purgeExecutions()
     */
    private static class PurgeExecutionService<ObjectType, ObjectIdType, ExecutionReportType> {
        /**
         * The configuration defining the number of executions to keep
         */
        private final int maxExecutionsToKeep;
        /**
         * A supplier of a domain object related to executions.
         *
         * @see TestCaseMetadata
         * @see Campaign
         */
        private final Supplier<List<ObjectType>> baseObject;
        /**
         * A mapper function extracting the domain object id
         */
        private final Function<ObjectType, ObjectIdType> idFunction;
        /**
         * A function returning all executions given an ObjectTypeId
         */
        private final Function<ObjectIdType, List<ExecutionReportType>> executionsFunction;
        /**
         * An optional executions filter used in {@link #purgeExecutions()} before grouping by environment
         */
        private final Predicate<ExecutionReportType> executionsFilter;
        /**
         * A mapper function extracting the execution id
         */
        private final Function<ExecutionReportType, Long> executionIdFunction;
        /**
         * A mapper function extracting the execution date for sorting
         */
        private final Function<ExecutionReportType, LocalDateTime> executionDateFunction;
        /**
         * A mapper function extracting the execution status for keeping the last success one
         */
        private final Function<ExecutionReportType, ServerReportStatus> statusFunction;
        /**
         * A mapper function extracting the execution environment
         */
        private final Function<ExecutionReportType, String> environmentFunction;
        /**
         * A function deleting executions by ids
         */
        private final Function<Set<Long>, Set<ExecutionReportType>> deleteFunction;

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

        /**
         * Purge executions.
         * <p> Find all base objects ids and map them to executions.</p>
         * <p>For each group of executions, filter then group them by environment.</p>
         * <p>For each group of executions by environment,</p>
         * <p>permits a specific handle via {@link #handleExecutionsForOneEnvironment(List)}</p>
         * <p>then call {@link #purgeOldestExecutionsFromOneEnvironment(List)}</p>
         *
         * @return The list of deleted executions ids.
         */
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

        /**
         * Purge the oldest executions according to {@link #maxExecutionsToKeep} configuration
         * by keeping the last success execution no matter what. <br/>
         * Check for existence.
         * Sort by date.
         * Call {@link #findOldestExecutionsIdsWhileKeepingTheLastSuccess(List)}.
         * Permits to add other executions ids via {@link #findExtraExecutionsIdsToDelete(List)}.
         * Delete executions.
         *
         * @return The list of deleted executions ids.
         */
        private Set<Long> purgeOldestExecutionsFromOneEnvironment(List<ExecutionReportType> executionsFromOneEnvironment) {
            if (executionsFromOneEnvironment.size() <= maxExecutionsToKeep) {
                return emptySet();
            }

            List<ExecutionReportType> timeSortedExecutionsForOneEnvironment = executionsFromOneEnvironment.stream()
                .sorted(comparing(executionDateFunction).reversed())
                .toList();

            Set<Long> deletedExecutionsIdsTmp = new HashSet<>();

            deletedExecutionsIdsTmp.addAll(
                findOldestExecutionsIdsWhileKeepingTheLastSuccess(timeSortedExecutionsForOneEnvironment)
            );

            deletedExecutionsIdsTmp.addAll(
                findExtraExecutionsIdsToDelete(timeSortedExecutionsForOneEnvironment)
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

        /**
         * To implement in order to control the list of executions of one {@link #baseObject} for one environment
         * to be passed down to {@link #purgeOldestExecutionsFromOneEnvironment(List)}. <br/>
         * This list is not sorted.
         *
         * @return Default implementation returns the same list.
         */
        protected List<ExecutionReportType> handleExecutionsForOneEnvironment(List<ExecutionReportType> executionsFromOneEnvironment) {
            return executionsFromOneEnvironment;
        }

        /**
         * To implement to find extra executions' ids to delete.
         *
         * @param timeSortedExecutionsForOneEnvironment The current list of executions processed by {@link #purgeOldestExecutionsFromOneEnvironment(List)}, sorted by {@link #executionDateFunction}
         * @return Default implementation returns a {@link Collections#emptySet()}
         */
        protected Collection<Long> findExtraExecutionsIdsToDelete(List<ExecutionReportType> timeSortedExecutionsForOneEnvironment) {
            return emptySet();
        }
    }
}
