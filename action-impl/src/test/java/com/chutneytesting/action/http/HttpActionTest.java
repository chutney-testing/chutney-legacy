package com.chutneytesting.action.http;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.any;
import static com.github.tomakehurst.wiremock.client.WireMock.anyUrl;
import static com.github.tomakehurst.wiremock.client.WireMock.configureFor;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.patch;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.put;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static java.util.Collections.emptyMap;
import static java.util.Optional.empty;
import static java.util.Optional.of;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.chutneytesting.action.TestTarget;
import com.chutneytesting.action.spi.Action;
import com.chutneytesting.action.spi.ActionExecutionResult;
import com.chutneytesting.action.spi.injectable.Logger;
import com.chutneytesting.action.spi.injectable.Target;
import com.chutneytesting.tools.SocketUtils;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.http.Fault;
import com.github.tomakehurst.wiremock.matching.EqualToPattern;
import java.net.URI;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.http.HttpHeaders;


public class HttpActionTest {


  private final WireMockServer wireMockServer = new WireMockServer(wireMockConfig()
        .dynamicPort()
        .httpsPort(SocketUtils.findAvailableTcpPort())
        .keystorePath(KEYSTORE_JKS)
        .keystorePassword("server")
        .keyManagerPassword("server"));

    private static final String KEYSTORE_JKS = HttpsServerStartActionTest.class.getResource("/security/server.jks").getPath();

  @BeforeEach
    public void setUp() {
        wireMockServer.start();
        configureFor("localhost", wireMockServer.port());
    }

    @AfterEach
    public void tearDown() {
        wireMockServer.stop();
        wireMockServer.resetAll();
    }

  @Test
  public void should_accept_requests_when_truststore_is_defined_in_target() {

    // Given
    Target target = TestTarget
        .TestTargetBuilder
        .builder()
        .withTargetId("test target")
        .withUrl("https://localhost:" + wireMockServer.httpsPort() + "/")
        .withProperty("trustStore", "C:\\Users\\AD13A0CL\\Downloads\\jdk-17.0.7\\lib\\security\\cacerts")
        .withProperty("trustStorePassword", "changeit")
        .build();

    wireMockServer.stubFor(any(anyUrl())
        .willReturn(aResponse().withStatus(200))
    );
    Logger logger = mock(Logger.class);
    Action httpGetAction = new HttpGetAction(target, logger,  "/some/thing", null, "3000 ms");

    // When
    ActionExecutionResult executionResult = httpGetAction.execute();

    // Then
    assertThat(executionResult.status).isEqualTo(ActionExecutionResult.Status.Failure);
  }

  @Test
  public void should_reject_requests_when_truststore_is_not_defined_in_target() {

    // Given
    Target target = TestTarget
        .TestTargetBuilder
        .builder()
        .withTargetId("test target")
        .withUrl("https://localhost:" + wireMockServer.httpsPort() + "/")
        .build();

    wireMockServer.stubFor(any(anyUrl())
        .willReturn(aResponse().withStatus(200))
    );
    Logger logger = mock(Logger.class);
    Action httpGetAction = new HttpGetAction(target, logger,  "/some/thing", null, "3000 ms");

    // When
    ActionExecutionResult executionResult = httpGetAction.execute();

    // Then
    assertThat(executionResult.status).isEqualTo(ActionExecutionResult.Status.Success);
  }

    @Test
    public void should_succeed_with_status_200_when_requesting_existing_resource() {

        String uri = "/some/thing";
        int expectedStatus = 200;
        String expectedBody = "Resource Body";
        org.springframework.http.HttpHeaders expectedHeaders = new org.springframework.http.HttpHeaders();
        expectedHeaders.put("Transfer-Encoding", Collections.singletonList("chunked"));

        stubFor(get(urlEqualTo(uri))
            .willReturn(aResponse().withStatus(expectedStatus)
                .withBody(expectedBody))
        );

        Logger logger = mock(Logger.class);
        Target targetMock = mockTarget("http://127.0.0.1:" + wireMockServer.port());

        // when
        Action httpGetAction = new HttpGetAction(targetMock, logger, uri, null, "1000 ms");
        ActionExecutionResult executionResult = httpGetAction.execute();

        // then
        assertThat(executionResult.status).isEqualTo(ActionExecutionResult.Status.Success);
        assertThat((Integer) executionResult.outputs.get("status")).isEqualTo(expectedStatus);
        assertThat((String) executionResult.outputs.get("body")).isEqualTo(expectedBody);
        assertThat((HttpHeaders) executionResult.outputs.get("headers")).containsAllEntriesOf(expectedHeaders);
    }

    @Test
    public void should_succeed_with_status_404_when_requesting_not_existing_resource() {

        String uri = "/some/nothing";
        int expectedStatus = 404;

        stubFor(post(urlEqualTo(uri))
            .willReturn(aResponse().withStatus(expectedStatus)
                .withBody("{}")));

        Logger logger = mock(Logger.class);
        Target targetMock = mockTarget("http://127.0.0.1:" + wireMockServer.port());

        // when
        Action httpPostAction = new HttpPostAction(targetMock, logger, uri, "some body", null, "1000 ms");
        ActionExecutionResult executionResult = httpPostAction.execute();

        // then
        assertThat(executionResult.status).isEqualTo(ActionExecutionResult.Status.Success);
        assertThat((Integer) executionResult.outputs.get("status")).isEqualTo(expectedStatus);
    }

    @Test
    public void should_replace_by_empty_object_when_body_is_null() {

        // given
        String uri = "/some/thing";
        String expectedBody = "{}";

        stubFor(post(urlEqualTo(uri)).willReturn(aResponse()
            .withBody(expectedBody))
        );

        Logger logger = mock(Logger.class);
        Target targetMock = mockTarget("http://127.0.0.1:" + wireMockServer.port());

        // when
        Action httpPostAction = new HttpPostAction(targetMock, logger, uri, null, null, "1000 ms");
        ActionExecutionResult executionResult = httpPostAction.execute();

        // then
        assertThat(executionResult.status).isEqualTo(ActionExecutionResult.Status.Success);
        assertThat((String) executionResult.outputs.get("body")).isEqualTo(expectedBody);

    }

    @Test
    public void should_fail_when_connect_request_times_out() {
        Logger logger = mock(Logger.class);

        Target targetMock = mockTarget("http://nowhere.com:42");

        // when
        Action httpDeleteAction = new HttpDeleteAction(targetMock, logger, "", null, "5 ms");
        ActionExecutionResult executionResult = httpDeleteAction.execute();

        // then
        assertThat(executionResult.status).isEqualTo(ActionExecutionResult.Status.Failure);
    }

    @ParameterizedTest
    @ValueSource(strings = {"CONNECTION_RESET_BY_PEER", "MALFORMED_RESPONSE_CHUNK", "EMPTY_RESPONSE", "RANDOM_DATA_THEN_CLOSE"})
    public void should_fail_when_fault_occurs(String faultName) {

        String uri = "/some/thing";

        stubFor(get(urlEqualTo(uri))
            .willReturn(aResponse().withFault(Fault.valueOf(faultName))));

        Logger logger = mock(Logger.class);
        Target targetMock = mockTarget("http://127.0.0.1:" + wireMockServer.port());

        // when
        Action httpGetAction = new HttpGetAction(targetMock, logger, uri, null, "1000 ms");
        ActionExecutionResult executionResult = httpGetAction.execute();

        // then
        assertThat(executionResult.status).isEqualTo(ActionExecutionResult.Status.Failure);
    }

    @Test
    public void should_succeed_with_specified_headers() {
        String uri = "/some/thing";
        int expectedStatus = 200;

        stubFor(put(urlEqualTo(uri)).withHeader("CustomHeader", new EqualToPattern("toto"))
            .willReturn(aResponse().withStatus(expectedStatus))
        );

        Logger logger = mock(Logger.class);
        Target targetMock = mockTarget("http://127.0.0.1:" + wireMockServer.port());

        // when
        Map<String, String> headers = new HashMap<>();
        headers.put("CustomHeader", "toto");

        Action httpPutAction = new HttpPutAction(targetMock, logger, uri, "somebody", headers, "1000 ms");
        ActionExecutionResult executionResult = httpPutAction.execute();

        // then
        assertThat(executionResult.status).isEqualTo(ActionExecutionResult.Status.Success);
        assertThat((Integer) executionResult.outputs.get("status")).isEqualTo(expectedStatus);
    }

    @Test
    public void should_patch_succeed() {
        String uri = "/some/thing";
        int expectedStatus = 200;

        stubFor(patch(urlEqualTo(uri)).withHeader("CustomHeader", new EqualToPattern("toto"))
            .willReturn(aResponse().withStatus(expectedStatus))
        );

        Logger logger = mock(Logger.class);
        Target targetMock = mockTarget("http://127.0.0.1:" + wireMockServer.port());

        // when
        Map<String, String> headers = new HashMap<>();
        headers.put("CustomHeader", "toto");

        Action httpPutAction = new HttpPatchAction(targetMock, logger, uri, "somebody", headers, "1000 ms");
        ActionExecutionResult executionResult = httpPutAction.execute();

        // then
        assertThat(executionResult.status).isEqualTo(ActionExecutionResult.Status.Success);
        assertThat((Integer) executionResult.outputs.get("status")).isEqualTo(expectedStatus);
    }

    /**
     * It test that the proxy is set.
     * It doesnt test DefaultProxyRoutePlanner class.
     */
    @Test
    public void should_use_target_proxy() {
        String uri = "/some/thing";
        int expectedStatus = 200;

        stubFor(get(urlEqualTo(uri))
            .willReturn(aResponse().withStatus(expectedStatus))
        );

        Logger logger = mock(Logger.class);
        Target targetMock = mockTarget("http://123.456.789.123:45678");
        when(targetMock.property("proxy")).thenReturn(of("https://127.0.0.1:" + wireMockServer.httpsPort()));

        // when
        Action httpPutTask = new HttpGetAction(targetMock, logger, uri, emptyMap(), "1000 ms");
        ActionExecutionResult executionResult = httpPutTask.execute();

        // then
        assertThat(executionResult.status).isEqualTo(ActionExecutionResult.Status.Success);
        assertThat((Integer) executionResult.outputs.get("status")).isEqualTo(expectedStatus);
    }

    private Target mockTarget(String targetUrl) {
        Target targetMock = mock(Target.class);
        when(targetMock.uri()).thenReturn(URI.create(targetUrl));
        when(targetMock.keyStore()).thenReturn(empty());
        when(targetMock.keyStorePassword()).thenReturn(empty());
        when(targetMock.trustStore()).thenReturn(empty());
        when(targetMock.trustStorePassword()).thenReturn(empty());
        return targetMock;
    }
}
