package com.chutneytesting.engine.domain.execution;

import static java.util.stream.Collectors.toList;

import com.chutneytesting.engine.domain.execution.report.Status;
import java.util.ArrayList;
import java.util.stream.Stream;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

public class StatusTest {

    @Test
    public void worstWithoutStatussShouldReturnOkStatus() {
        Status status = Status.worst(new ArrayList<>());

        Assertions.assertThat(status).isEqualTo(Status.SUCCESS);
    }

    @ParameterizedTest(name = "should return {0} when method parameters are {arguments}")
    @MethodSource("data")
    public void worstWithStatusesShouldReturnTheWorstStatus(Status expected, Status[] input) {
        Status status = Status.worst(Stream.of(input).collect(toList()));

        Assertions.assertThat(status).isEqualTo(expected);
    }

    private static Object[] data() {
        return new Object[] {
            new Object[]{Status.SUCCESS, new Status[]{Status.SUCCESS}},
            new Object[]{Status.FAILURE, new Status[]{Status.FAILURE, Status.WARN}},
            new Object[]{Status.WARN, new Status[]{Status.WARN, Status.NOT_EXECUTED}},
            new Object[]{Status.RUNNING, new Status[]{Status.NOT_EXECUTED, Status.SUCCESS}},
            new Object[]{Status.RUNNING, new Status[]{Status.NOT_EXECUTED, Status.RUNNING}},
            new Object[]{Status.PAUSED, new Status[]{Status.PAUSED, Status.RUNNING}}
        };
    }
}
