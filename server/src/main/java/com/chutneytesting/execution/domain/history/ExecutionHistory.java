package com.chutneytesting.execution.domain.history;

import com.chutneytesting.execution.domain.report.ServerReportStatus;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.immutables.value.Value;

@Value.Immutable
@Value.Enclosing
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
