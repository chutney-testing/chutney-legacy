package com.chutneytesting.task.http;

import static com.chutneytesting.task.spi.TaskExecutionResult.Status.Success;
import static com.chutneytesting.task.tools.WaitUtils.awaitDuring;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;

import com.chutneytesting.task.TestLogger;
import com.chutneytesting.task.spi.Task;
import com.chutneytesting.task.spi.TaskExecutionResult;
import com.chutneytesting.task.spi.injectable.FinallyActionRegistry;
import com.chutneytesting.task.spi.injectable.Logger;
import com.github.tomakehurst.wiremock.WireMockServer;
import javax.net.ssl.SSLContext;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustStrategy;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContexts;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mockito;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.util.SocketUtils;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

public class HttpsServerStartTaskTest {

    private static final int wireMockPort = SocketUtils.findAvailableTcpPort();
    private static final String TRUSTSTORE_JKS = HttpsServerStartTaskTest.class.getResource("/security/truststore.jks").getPath();
    private static final String KEYSTORE_JKS = HttpsServerStartTaskTest.class.getResource("/security/server.jks").getPath();
    private static final String KEYSTORE_WITH_KEYPWD = HttpsServerStartTaskTest.class.getResource("/security/keystore-with-keypwd.jks").getPath();
    private final static FinallyActionRegistry finallyActionRegistry = Mockito.mock(FinallyActionRegistry.class);

    public static Object[] parametersForShould_start_https_server() {
        Logger logger = new TestLogger();

        Task httpsServerStartTask = new HttpsServerStartTask(logger, finallyActionRegistry, String.valueOf(wireMockPort), TRUSTSTORE_JKS, "truststore", null, null, null);

        Task httpsServerStartTaskWithKeyStore = new HttpsServerStartTask(logger, finallyActionRegistry, String.valueOf(wireMockPort), TRUSTSTORE_JKS, "truststore", KEYSTORE_JKS, "server", "server");

        Task httpsServerStartTaskWithKeyStoreAndKeyPwd = new HttpsServerStartTask(logger, finallyActionRegistry, String.valueOf(wireMockPort), TRUSTSTORE_JKS, "truststore", KEYSTORE_WITH_KEYPWD, "server", "key_pwd");

        return new Object[][]{
            {httpsServerStartTask},
            {httpsServerStartTaskWithKeyStore},
            {httpsServerStartTaskWithKeyStoreAndKeyPwd}
        };
    }

    @ParameterizedTest
    @MethodSource("parametersForShould_start_https_server")
    public void should_start_https_server(Task httpsServerStartTask) {
        reset(finallyActionRegistry);
        WireMockServer server = null;
        try {

            TaskExecutionResult result = httpsServerStartTask.execute();

            assertThat(result.status).isEqualTo(Success);
            assertThat(result.outputs).containsKey("httpsServer");
            assertThat(result.outputs.get("httpsServer")).isNotNull();
            verify(finallyActionRegistry).registerFinallyAction(any());
            server = (WireMockServer) result.outputs.get("httpsServer");
            assertThat(server.isRunning()).isTrue();
        } finally {
            stopServer(server);
        }
    }

    @Test
    public void should_trust_all_requests_when_no_truststore_is_defined() throws Exception {
        reset(finallyActionRegistry);
        WireMockServer server = null;
        try {
            String noTrustStore = null;
            Task httpsServerStartTask = new HttpsServerStartTask(new TestLogger(), finallyActionRegistry, String.valueOf(wireMockPort), noTrustStore, noTrustStore, null, null, null);
            server = (WireMockServer) httpsServerStartTask.execute().outputs.get("httpsServer");

            // When
            HttpStatus status = httpGet().getStatusCode();

            // Then
            assertThat(status).isEqualTo(HttpStatus.OK);

        } finally {
            stopServer(server);
        }
    }

    @Test
    public void should_reject_requests_when_truststore_is_defined() {
        reset(finallyActionRegistry);
        WireMockServer server = null;
        try {
            Task httpsServerStartTask = new HttpsServerStartTask(new TestLogger(), finallyActionRegistry, String.valueOf(wireMockPort), TRUSTSTORE_JKS, "truststore", null, null, null);
            server = (WireMockServer) httpsServerStartTask.execute().outputs.get("httpsServer");

            assertThatExceptionOfType(ResourceAccessException.class).isThrownBy(this::httpGet);

        } finally {
            stopServer(server);
        }
    }

    private ResponseEntity<String> httpGet() throws Exception {
        TrustStrategy acceptingTrustStrategy = (x509Certificates, s) -> true;
        SSLContext sslContext = SSLContexts.custom().loadTrustMaterial(null, acceptingTrustStrategy).build();
        SSLConnectionSocketFactory csf = new SSLConnectionSocketFactory(sslContext, new NoopHostnameVerifier());
        CloseableHttpClient httpClient = HttpClients.custom().setSSLSocketFactory(csf).build();
        HttpComponentsClientHttpRequestFactory requestFactory = new HttpComponentsClientHttpRequestFactory();
        requestFactory.setHttpClient(httpClient);
        return new RestTemplate(requestFactory).exchange("https://localhost:" + wireMockPort + "/", HttpMethod.GET, new HttpEntity<>(null), String.class);
    }

    private void stopServer(WireMockServer server) {
        if (server != null) {
            server.stop();
            //Wait graceful stop
            awaitDuring(500, MILLISECONDS);
        }
    }

}
