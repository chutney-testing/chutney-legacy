package com.chutneytesting.engine.domain.execution;

import static java.util.stream.Collectors.toList;

import com.chutneytesting.engine.domain.execution.report.Status;
import java.util.ArrayList;
import java.util.stream.Stream;
import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import junitparams.naming.TestCaseName;
import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(JUnitParamsRunner.class)
public class StatusTest {

    @Test
    public void worstWithoutStatussShouldReturnOkStatus() {
        Status status = Status.worst(new ArrayList<>());

        Assertions.assertThat(status).isEqualTo(Status.SUCCESS);
    }

    @Test
    @Parameters(method = "data")
    @TestCaseName("should return {0} when method parameters are {params}")
    public void worstWithStatusesShouldReturnTheWorstStatus(Status expected, Status... input) {
        Status status = Status.worst(Stream.of(input).collect(toList()));

        Assertions.assertThat(status).isEqualTo(expected);
    }

    Object data() {
        return new Object[] {
            new Object[] { Status.SUCCESS, Status.SUCCESS},
            new Object[] { Status.FAILURE, Status.FAILURE, Status.WARN },
            new Object[] { Status.WARN, Status.WARN, Status.NOT_EXECUTED },
            new Object[] { Status.RUNNING, Status.NOT_EXECUTED, Status.SUCCESS},
            new Object[] { Status.RUNNING, Status.NOT_EXECUTED, Status.RUNNING},
            new Object[] { Status.PAUSED, Status.PAUSED, Status.RUNNING}
        };
    }
}
