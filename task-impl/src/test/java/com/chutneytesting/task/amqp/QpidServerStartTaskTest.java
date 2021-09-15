package com.chutneytesting.task.amqp;

import static org.assertj.core.api.Assertions.assertThat;

import com.chutneytesting.task.TestFinallyActionRegistry;
import com.chutneytesting.task.TestLogger;
import com.chutneytesting.task.spi.FinallyAction;
import com.chutneytesting.task.spi.TaskExecutionResult;
import org.apache.qpid.server.SystemLauncher;
import org.junit.jupiter.api.Test;

class QpidServerStartTaskTest {

    @Test
    void should_start_with_default_configuration() {
        TestLogger logger = new TestLogger();
        TestFinallyActionRegistry finallyActionRegistry = new TestFinallyActionRegistry();
        QpidServerStartTask sut = new QpidServerStartTask(logger, finallyActionRegistry, null);

        TaskExecutionResult executionResult = sut.execute();

        assertThat(executionResult.status).isEqualTo(TaskExecutionResult.Status.Success);
        assertThat(executionResult.outputs)
            .hasSize(1)
            .extractingByKey("qpidLauncher").isInstanceOf(SystemLauncher.class);
        assertThat(logger.info).hasSize(2);
        assertThat(finallyActionRegistry.finallyActions)
            .hasSize(1)
            .hasOnlyElementsOfType(FinallyAction.class);

        assertThat(finallyActionRegistry.finallyActions.get(0).type())
            .isEqualTo("qpid-server-stop");
    }

}
