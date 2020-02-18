package com.chutneytesting.engine.infrastructure.delegation;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.any;
import static com.github.tomakehurst.wiremock.client.WireMock.anyUrl;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.chutneytesting.engine.api.execution.StatusDto;
import com.chutneytesting.engine.api.execution.StepExecutionReportDto;
import com.chutneytesting.engine.domain.delegation.CannotDelegateException;
import com.chutneytesting.engine.domain.delegation.NamedHostAndPort;
import com.chutneytesting.engine.domain.environment.ImmutableTarget;
import com.chutneytesting.engine.domain.environment.Target;
import com.chutneytesting.engine.domain.execution.StepDefinition;
import com.chutneytesting.engine.domain.execution.report.StepExecutionReport;
import com.chutneytesting.engine.domain.execution.strategies.StepStrategyDefinition;
import com.chutneytesting.engine.domain.execution.strategies.StrategyProperties;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.time.Instant;
import java.util.Collections;
import java.util.HashMap;
import java.util.Optional;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import org.assertj.core.api.Assertions;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.util.SocketUtils;

public class HttpClientTest {

    private WireMockServer server =
        new WireMockServer(wireMockConfig()
            .port(SocketUtils.findAvailableTcpPort())
            .httpsPort(SocketUtils.findAvailableTcpPort()));

    static {
        //for localhost testing only
        javax.net.ssl.HttpsURLConnection.setDefaultHostnameVerifier(
            (hostname, sslSession) -> hostname.equals("localhost"));
    }

    @Before
    public void setUp() {
        server.start();
        SSLContext.setDefault(sslContext(new TrustManager[]{TrustAllX509TrustManager.INSTANCE}));
    }

    @After
    public void tearDown() {
        server.stop();
    }

    @Test
    public void should_delagate_execution_to_endpoint() throws JsonProcessingException {
        //G
        StepDefinition stepDefinition = createFakeStepDefinition();
        NamedHostAndPort remoteHost = new NamedHostAndPort("name", "localhost", server.httpsPort());
        StepExecutionReportDto dto = createStepExecutionReportDto();
        String dtoAsString = objectMapper().writeValueAsString(dto);

        server.stubFor(any(anyUrl())
            .willReturn(aResponse()
                .withBody(dtoAsString)
                .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_UTF8_VALUE)
                .withStatus(200))
        );

        //W
        HttpClient client = new HttpClient();
        StepExecutionReport report = client.handDown(stepDefinition, remoteHost);

        //T
        assertThat(report).isNotNull();
    }

    @Test
    public void should_throw_exception_if_delegate_not_reachable() {
        //G
        NamedHostAndPort remoteHost = new NamedHostAndPort("name", "unknowhost", 0);

        //W + T
        HttpClient client = new HttpClient();
        Assertions.assertThatExceptionOfType(CannotDelegateException.class)
            .isThrownBy(() -> client.handDown(null, remoteHost));

    }

    private StepExecutionReportDto createStepExecutionReportDto() {
        return new StepExecutionReportDto(
            "name",
            Instant.now(),
            1L,
            StatusDto.NOT_EXECUTED,
            Collections.emptyList(),
            Collections.emptyList(),
            Collections.emptyList(),
            new StepExecutionReportDto.StepContextDto(),
            "type",
            "targetName",
            "TargetUrl",
            "strategy"
        );
    }

    private StepDefinition createFakeStepDefinition() {
        Target target = ImmutableTarget.of(Target.TargetId.of("name"), "url", Optional.empty());
        StepStrategyDefinition strategy = new StepStrategyDefinition("onestrategy", new StrategyProperties());
        return new StepDefinition("name",
            target,
            "success",
            strategy,
            new HashMap<>(),
            Collections.emptyList(),
            new HashMap<>());
    }

    public ObjectMapper objectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        objectMapper.findAndRegisterModules();
        return objectMapper;
    }

    private static SSLContext sslContext(TrustManager[] trustManagers) {
        try {
            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null,
                trustManagers,
                null);
            return sslContext;
        } catch (NoSuchAlgorithmException | KeyManagementException e) {
            throw new IllegalStateException("Couldn't init TLS context", e);
        }
    }

    private static class TrustAllX509TrustManager implements X509TrustManager {
        static final X509TrustManager INSTANCE = new TrustAllX509TrustManager();

        @Override
        public void checkClientTrusted(X509Certificate[] chain, String authType)
            throws CertificateException {
        }

        @Override
        public void checkServerTrusted(X509Certificate[] chain, String authType)
            throws CertificateException {
        }

        @Override
        public X509Certificate[] getAcceptedIssuers() {
            return new X509Certificate[0];
        }
    }
}
