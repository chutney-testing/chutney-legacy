/*
 * Copyright 2017-2023 Enedis
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.chutneytesting.server.core.domain.execution.history;

import com.chutneytesting.server.core.domain.execution.report.ServerReportStatus;
import com.chutneytesting.server.core.domain.scenario.campaign.CampaignExecution;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.immutables.value.Value;

@Value.Immutable
@Value.Enclosing
@JsonSerialize(as = ImmutableExecutionHistory.class)
public interface ExecutionHistory {

    @Value.Parameter
    String scenarioId();

    @Value.Parameter
    List<Execution> history();

    interface ExecutionProperties {
        LocalDateTime time();

        long duration();

        ServerReportStatus status();

        Optional<String> info();

        Optional<String> error();

        String testCaseTitle();

        String environment();

        Optional<String> datasetId();

        Optional<Integer> datasetVersion();

        String user();

        Optional<CampaignExecution> campaignReport();
    }

    interface Attached {
        Long executionId();
    }

    @Value.Immutable
    interface DetachedExecution extends ExecutionProperties, HavingReport {

        default Execution attach(long executionId) {
            return ImmutableExecutionHistory.Execution.builder()
                .from((ExecutionProperties) this)
                .from((HavingReport) this)
                .executionId(executionId)
                .build();
        }
    }

    @Value.Immutable
    interface ExecutionSummary extends ExecutionProperties, Attached {
    }

    @Value.Immutable
    interface Execution extends ExecutionProperties, HavingReport, Attached {

        default ExecutionSummary summary() {
            return ImmutableExecutionHistory.ExecutionSummary.builder()
                .from((ExecutionProperties) this)
                .from((Attached) this)
                .build();
        }
    }
}
