package com.chutneytesting.task.http;

import static com.chutneytesting.task.spi.TaskExecutionResult.Status.Success;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;

import com.chutneytesting.task.TestLogger;
import com.chutneytesting.task.spi.Task;
import com.chutneytesting.task.spi.TaskExecutionResult;
import com.chutneytesting.task.spi.injectable.FinallyActionRegistry;
import com.chutneytesting.task.spi.injectable.Logger;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.google.common.io.Resources;
import java.util.concurrent.TimeUnit;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mockito;
import org.springframework.util.SocketUtils;

public class HttpsServerStartTaskTest {

    private static int wireMockPort = SocketUtils.findAvailableTcpPort();
    private static final String TRUSTSTORE_JKS = Resources.getResource("security/truststore.jks").getPath();
    private static final String KEYSTORE_JKS = Resources.getResource("security/server.jks").getPath();
    private final static FinallyActionRegistry finallyActionRegistry = Mockito.mock(FinallyActionRegistry.class);

    public static Object[] parametersForShould_start_https_server() {
        Logger logger = new TestLogger();

        Task httpsServerStartTask = new HttpsServerStartTask(logger, finallyActionRegistry, String.valueOf(wireMockPort), TRUSTSTORE_JKS, "truststore", null, null);

        Task httpsServerStartTaskWithKeyStore = new HttpsServerStartTask(logger, finallyActionRegistry, String.valueOf(wireMockPort), TRUSTSTORE_JKS, "truststore", KEYSTORE_JKS, "server");

        return new Object[][]{
            {httpsServerStartTask},
            {httpsServerStartTaskWithKeyStore}
        };
    }

    @ParameterizedTest
    @MethodSource("parametersForShould_start_https_server")
    public void should_start_https_server(Task httpsServerStartTask) throws InterruptedException {
        reset(finallyActionRegistry);
        WireMockServer server = null;
        try {

            TaskExecutionResult result = httpsServerStartTask.execute();

            assertThat(result.status).isEqualTo(Success);
            assertThat(result.outputs).containsKey("httpsServer");
            assertThat(result.outputs.get("httpsServer")).isNotNull();
            verify(finallyActionRegistry).registerFinallyAction(any());
            server = (WireMockServer) result.outputs.get("httpsServer");
            Assertions.assertThat(server.isRunning()).isTrue();
        } finally {
            if (server != null) {
                server.stop();
                //Wait graceful stop
                TimeUnit.MILLISECONDS.sleep(500L);
            }
        }
    }

}
