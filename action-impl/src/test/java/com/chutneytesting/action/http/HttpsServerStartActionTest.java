package com.chutneytesting.action.http;

import static com.chutneytesting.action.spi.ActionExecutionResult.Status.Success;
import static com.chutneytesting.action.tools.WaitUtils.awaitDuring;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.junit.jupiter.api.condition.OS.WINDOWS;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;

import com.chutneytesting.action.TestLogger;
import com.chutneytesting.action.spi.Action;
import com.chutneytesting.action.spi.ActionExecutionResult;
import com.chutneytesting.action.spi.injectable.FinallyActionRegistry;
import com.chutneytesting.action.spi.injectable.Logger;
import com.chutneytesting.tools.SocketUtils;
import com.github.tomakehurst.wiremock.WireMockServer;
import javax.net.ssl.SSLContext;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManagerBuilder;
import org.apache.hc.client5.http.io.HttpClientConnectionManager;
import org.apache.hc.client5.http.ssl.NoopHostnameVerifier;
import org.apache.hc.client5.http.ssl.SSLConnectionSocketFactory;
import org.apache.hc.core5.ssl.SSLContexts;
import org.apache.hc.core5.ssl.TrustStrategy;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledOnOs;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mockito;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

public class HttpsServerStartActionTest {

  private static final int wireMockPort = SocketUtils.findAvailableTcpPort();
  private static final String TRUSTSTORE_JKS = HttpsServerStartActionTest.class.getResource("/security/truststore.jks").getPath();
  private static final String KEYSTORE_JKS = HttpsServerStartActionTest.class.getResource("/security/server.jks").getPath();
  private static final String KEYSTORE_WITH_KEYPWD = HttpsServerStartActionTest.class.getResource("/security/keystore-with-keypwd.jks").getPath();
  private final static FinallyActionRegistry finallyActionRegistry = Mockito.mock(FinallyActionRegistry.class);

  public static Object[] parametersForShould_start_https_server() {
    Logger logger = new TestLogger();

    Action httpsServerStartAction = new HttpsServerStartAction(logger, finallyActionRegistry, String.valueOf(wireMockPort), TRUSTSTORE_JKS, "truststore", null, null, null);

    Action httpsServerStartActionWithKeyStore = new HttpsServerStartAction(logger, finallyActionRegistry, String.valueOf(wireMockPort), TRUSTSTORE_JKS, "truststore", KEYSTORE_JKS, "server", "server");

    Action httpsServerStartActionWithKeyStoreAndKeyPwd = new HttpsServerStartAction(logger, finallyActionRegistry, String.valueOf(wireMockPort), TRUSTSTORE_JKS, "truststore", KEYSTORE_WITH_KEYPWD, "server", "key_pwd");

    return new Object[][]{
        {httpsServerStartAction},
        {httpsServerStartActionWithKeyStore},
        {httpsServerStartActionWithKeyStoreAndKeyPwd}
    };
  }

  @ParameterizedTest
  @MethodSource("parametersForShould_start_https_server")
  @DisabledOnOs({ WINDOWS })
  public void should_start_https_server(Action httpsServerStartAction) {
    reset(finallyActionRegistry);
    WireMockServer server = null;
    try {

      ActionExecutionResult result = httpsServerStartAction.execute();

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
      Action httpsServerStartAction = new HttpsServerStartAction(new TestLogger(), finallyActionRegistry, String.valueOf(wireMockPort), noTrustStore, noTrustStore, null, null, null);
      server = (WireMockServer) httpsServerStartAction.execute().outputs.get("httpsServer");

      // When
      HttpStatusCode status = httpGet().getStatusCode();

      // Then
      assertThat(status).isEqualTo(HttpStatus.OK);

    } finally {
      stopServer(server);
    }
  }


  @Test
  @DisabledOnOs({ WINDOWS })
  public void should_reject_requests_when_truststore_is_defined() {
    reset(finallyActionRegistry);
    WireMockServer server = null;
    try {
      Action httpsServerStartAction = new HttpsServerStartAction(new TestLogger(), finallyActionRegistry, String.valueOf(wireMockPort), TRUSTSTORE_JKS, "truststore", null, null, null);
      server = (WireMockServer) httpsServerStartAction.execute().outputs.get("httpsServer");

      assertThatExceptionOfType(ResourceAccessException.class).isThrownBy(this::httpGet);

    } finally {
      stopServer(server);
    }
  }

  private ResponseEntity<String> httpGet() throws Exception {
    TrustStrategy acceptingTrustStrategy = (x509Certificates, s) -> true;
    SSLContext sslContext = SSLContexts.custom().loadTrustMaterial(null, acceptingTrustStrategy).build();
    SSLConnectionSocketFactory csf = new SSLConnectionSocketFactory(sslContext, new NoopHostnameVerifier());
    HttpClientConnectionManager connectionManager = PoolingHttpClientConnectionManagerBuilder.create().setSSLSocketFactory(csf).build();
    CloseableHttpClient httpClient = HttpClients.custom().setConnectionManager(connectionManager).build();
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
