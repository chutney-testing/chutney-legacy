package com.chutneytesting.execution.domain.report;

import com.google.common.collect.Ordering;
import graphql.annotations.annotationTypes.GraphQLTypeResolver;
import graphql.kickstart.annotations.GraphQLInterfaceTypeResolver;
import graphql.schema.GraphQLEnumType;
import java.util.Objects;
import java.util.stream.StreamSupport;
public enum ServerReportStatus {
    SUCCESS, WARN, FAILURE, NOT_EXECUTED, STOPPED, PAUSED, RUNNING, EXECUTED;

    private static final Ordering<ServerReportStatus> EXECUTION_STATUS_STATUS_ORDERING = Ordering.explicit(PAUSED, RUNNING, STOPPED, FAILURE, WARN, NOT_EXECUTED, SUCCESS);

    public static ServerReportStatus worst(Iterable<ServerReportStatus> severalStatus) {
        return StreamSupport
            .stream(severalStatus.spliterator(), false)
            .filter(Objects::nonNull)
            .reduce(SUCCESS, EXECUTION_STATUS_STATUS_ORDERING::min);
    }

    public boolean isFinal() {
        return this.equals(FAILURE) || this.equals(SUCCESS) || this.equals(STOPPED);
    }

    public interface HavingStatus {
        ServerReportStatus getStatus();
    }
}
