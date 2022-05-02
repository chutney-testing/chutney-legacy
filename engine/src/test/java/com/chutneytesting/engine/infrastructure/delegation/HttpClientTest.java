package com.chutneytesting.engine.infrastructure.delegation;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.any;
import static com.github.tomakehurst.wiremock.client.WireMock.anyUrl;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static java.util.Optional.ofNullable;
import static org.assertj.core.api.Assertions.assertThat;

import com.chutneytesting.engine.api.execution.StatusDto;
import com.chutneytesting.engine.api.execution.StepExecutionReportDto;
import com.chutneytesting.engine.domain.delegation.CannotDelegateException;
import com.chutneytesting.engine.domain.delegation.NamedHostAndPort;
import com.chutneytesting.engine.domain.environment.TargetImpl;
import com.chutneytesting.engine.domain.execution.StepDefinition;
import com.chutneytesting.engine.domain.execution.report.StepExecutionReport;
import com.chutneytesting.engine.domain.execution.strategies.StepStrategyDefinition;
import com.chutneytesting.engine.domain.execution.strategies.StrategyProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.MappingBuilder;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.time.Instant;
import java.util.Collections;
import java.util.HashMap;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.util.SocketUtils;

public class HttpClientTest {

    private final WireMockServer server = new WireMockServer(
        wireMockConfig()
            .port(SocketUtils.findAvailableTcpPort())
            .httpsPort(SocketUtils.findAvailableTcpPort())
    );

    static {
        //for localhost testing only
        javax.net.ssl.HttpsURLConnection.setDefaultHostnameVerifier(
            (hostname, sslSession) -> "localhost".equals(hostname));
    }

    @BeforeEach
    public void setUp() {
        server.start();
        SSLContext.setDefault(sslContext(new TrustManager[]{TrustAllX509TrustManager.INSTANCE}));
    }

    @AfterEach
    public void tearDown() {
        server.stop();
    }

    @ParameterizedTest
    @MethodSource("credentials")
    public void should_delegate_execution_to_endpoint(String user, String password) throws JsonProcessingException {
        //G
        StepDefinition stepDefinition = createFakeStepDefinition();
        NamedHostAndPort remoteHost = new NamedHostAndPort("name", "localhost", server.httpsPort());
        StepExecutionReportDto dto = createStepExecutionReportDto();
        String dtoAsString = objectMapper().writeValueAsString(dto);

        MappingBuilder mappingBuilder = any(anyUrl())
            .willReturn(aResponse()
                .withBody(dtoAsString)
                .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .withStatus(200));

        ofNullable(user).ifPresent(u -> mappingBuilder.withBasicAuth(user, password));

        server.stubFor(mappingBuilder);

        //W
        HttpClient client = new HttpClient(user, password);
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
            "environment",
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
        TargetImpl target = TargetImpl.builder().withName("name").withUrl("url").build();
        StepStrategyDefinition strategy = new StepStrategyDefinition("onestrategy", new StrategyProperties());
        return new StepDefinition("name",
            target,
            "success",
            strategy,
            new HashMap<>(),
            Collections.emptyList(),
            new HashMap<>(),
            new HashMap<>(),
            "ENV");
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

    @SuppressWarnings("unused")
    private static Object[] credentials() {
        return new Object[]{
            new Object[]{null, null},
            new Object[]{"user", "password"}
        };
    }
}
