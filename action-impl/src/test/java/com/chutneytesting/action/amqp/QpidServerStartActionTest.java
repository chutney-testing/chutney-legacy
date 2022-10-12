package com.chutneytesting.action.amqp;

import static org.assertj.core.api.Assertions.assertThat;

import com.chutneytesting.action.TestFinallyActionRegistry;
import com.chutneytesting.action.TestLogger;
import com.chutneytesting.action.spi.FinallyAction;
import com.chutneytesting.action.spi.ActionExecutionResult;
import org.apache.qpid.server.SystemLauncher;
import org.junit.jupiter.api.Test;

class QpidServerStartActionTest {

    @Test
    void should_start_with_default_configuration() {
        ActionExecutionResult executionResult = null;
        try {
            TestLogger logger = new TestLogger();
            TestFinallyActionRegistry finallyActionRegistry = new TestFinallyActionRegistry();
            QpidServerStartAction sut = new QpidServerStartAction(logger, finallyActionRegistry, null);

            executionResult = sut.execute();

            assertThat(executionResult.status).isEqualTo(ActionExecutionResult.Status.Success);
            assertThat(executionResult.outputs)
                .hasSize(1)
                .extractingByKey("qpidLauncher").isInstanceOf(SystemLauncher.class);
            assertThat(logger.info).hasSize(2);
            assertThat(finallyActionRegistry.finallyActions)
                .hasSize(1)
                .hasOnlyElementsOfType(FinallyAction.class);

            assertThat(finallyActionRegistry.finallyActions.get(0).type())
                .isEqualTo("qpid-server-stop");
        } finally {
            if (executionResult != null) {
                SystemLauncher qpidServer = (SystemLauncher) executionResult.outputs.get("qpidLauncher");
                qpidServer.shutdown();
            }
        }
    }

}
