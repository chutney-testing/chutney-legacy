package com.chutneytesting.engine.domain.execution.report;

import com.google.common.collect.Ordering;
import java.util.List;
import java.util.Objects;

public enum Status {
    SUCCESS, WARN, FAILURE, NOT_EXECUTED, STOPPED, PAUSED, RUNNING, EXECUTED;

    private static final Ordering<Status> EXECUTION_STATUS_STATUS_ORDERING = Ordering.explicit(EXECUTED, PAUSED, RUNNING, STOPPED, FAILURE, WARN, NOT_EXECUTED, SUCCESS);

    public static Status worst(List<Status> severalStatus) {

        Status reducedStatus = severalStatus.stream()
            .filter(Objects::nonNull)
            .reduce(SUCCESS, EXECUTION_STATUS_STATUS_ORDERING::min);

        if(reducedStatus.equals(Status.NOT_EXECUTED)) {
            List<Status> notExecutedStatus = severalStatus.stream().filter(s -> !s.equals(NOT_EXECUTED)).toList();
            if(!notExecutedStatus.isEmpty()) {
                return RUNNING;
            }
        }
        return reducedStatus;
    }

    public interface HavingStatus {
        Status getStatus();
    }
}
