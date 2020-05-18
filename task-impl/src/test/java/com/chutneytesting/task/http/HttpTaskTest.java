package com.chutneytesting.task.http;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.put;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.github.tomakehurst.wiremock.http.Fault;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import com.github.tomakehurst.wiremock.matching.EqualToPattern;
import com.chutneytesting.task.spi.Task;
import com.chutneytesting.task.spi.TaskExecutionResult;
import com.chutneytesting.task.spi.injectable.Logger;
import com.chutneytesting.task.spi.injectable.SecurityInfo;
import com.chutneytesting.task.spi.injectable.Target;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.http.HttpHeaders;


@RunWith(JUnitParamsRunner.class)
public class HttpTaskTest {

    @Rule
    public WireMockRule wireMockRule = new WireMockRule(wireMockConfig().dynamicPort().dynamicHttpsPort());

    @Test
    public void should_succeed_with_status_200_when_requesting_existing_resource() {

        String uri = "/some/thing";
        int expectedStatus = 200;
        String expectedBody = "Resource Body";
        org.springframework.http.HttpHeaders expectedHeaders = new org.springframework.http.HttpHeaders();
        expectedHeaders.put("Server", Collections.singletonList("Jetty(9.2.z-SNAPSHOT)"));
        expectedHeaders.put("Transfer-Encoding", Collections.singletonList("chunked"));
        expectedHeaders.put("Vary", Collections.singletonList("Accept-Encoding, User-Agent"));

        stubFor(get(urlEqualTo(uri))
            .willReturn(aResponse().withStatus(expectedStatus)
                                   .withBody(expectedBody))
        );

        Logger logger = mock(Logger.class);
        Target targetMock = mockTarget("http://127.0.0.1:" + wireMockRule.port());

        // when
        Task httpGetTask = new HttpGetTask(targetMock, logger, uri, null, "1000 ms");
        TaskExecutionResult executionResult = httpGetTask.execute();

        // then
        assertThat(executionResult.status).isEqualTo(TaskExecutionResult.Status.Success);
        assertThat((Integer) executionResult.outputs.get("status")).isEqualTo(expectedStatus);
        assertThat((String) executionResult.outputs.get("body")).isEqualTo(expectedBody);
        assertThat((HttpHeaders)executionResult.outputs.get("headers")).containsAllEntriesOf(expectedHeaders);
    }

    @Test
    public void should_succeed_with_status_404_when_requesting_not_existing_resource() {

        String uri = "/some/nothing";
        int expectedStatus = 404;

        stubFor(post(urlEqualTo(uri))
            .willReturn(aResponse().withStatus(expectedStatus)
                .withBody("{}")));

        Logger logger = mock(Logger.class);
        Target targetMock = mockTarget("http://127.0.0.1:" + wireMockRule.port());

        // when
        Task httpPostTask = new HttpPostTask(targetMock, logger, uri,"some body", null, "1000 ms");
        TaskExecutionResult executionResult = httpPostTask.execute();

        // then
        assertThat(executionResult.status).isEqualTo(TaskExecutionResult.Status.Success);
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
        Target targetMock = mockTarget("http://127.0.0.1:" + wireMockRule.port());

        // when
        Task httpPostTask = new HttpPostTask(targetMock, logger, uri, null, null, "1000 ms");
        TaskExecutionResult executionResult = httpPostTask.execute();

        // then
        assertThat(executionResult.status).isEqualTo(TaskExecutionResult.Status.Success);
        assertThat((String) executionResult.outputs.get("body")).isEqualTo(expectedBody);

    }

    @Test
    public void should_fail_when_connect_request_times_out() {
        Logger logger = mock(Logger.class);

        Target targetMock = mockTarget("http://nowhere.com:42");

        // when
        Task httpDeleteTask = new HttpDeleteTask(targetMock, logger, "", null, "5 ms");
        TaskExecutionResult executionResult = httpDeleteTask.execute();

        // then
        assertThat(executionResult.status).isEqualTo(TaskExecutionResult.Status.Failure);
    }

    @Test
    @Parameters({"CONNECTION_RESET_BY_PEER", "MALFORMED_RESPONSE_CHUNK", "EMPTY_RESPONSE", "RANDOM_DATA_THEN_CLOSE"})
    public void should_fail_when_fault_occurs(String faultName) {

        String uri = "/some/thing";

        stubFor(get(urlEqualTo(uri))
            .willReturn(aResponse().withFault(Fault.valueOf(faultName))));

        Logger logger = mock(Logger.class);
        Target targetMock = mockTarget("http://127.0.0.1:" + wireMockRule.port());

        // when
        Task httpGetTask = new HttpGetTask(targetMock, logger, uri, null, "1000 ms");
        TaskExecutionResult executionResult = httpGetTask.execute();

        // then
        assertThat(executionResult.status).isEqualTo(TaskExecutionResult.Status.Failure);
    }

    @Test
    public void should_succeed_with_specified_headers() {
        String uri = "/some/thing";
        int expectedStatus = 200;

        stubFor(put(urlEqualTo(uri)).withHeader("CustomHeader", new EqualToPattern("toto"))
                                    .willReturn(aResponse().withStatus(expectedStatus))
        );

        Logger logger = mock(Logger.class);
        Target targetMock = mockTarget("http://127.0.0.1:" + wireMockRule.port());

        // when
        Map<String, String> headers = new HashMap<>();
        headers.put("CustomHeader", "toto");

        Task httpPutTask = new HttpPutTask(targetMock, logger, uri, "somebody",headers, "1000 ms");
        TaskExecutionResult executionResult = httpPutTask.execute();

        // then
        assertThat(executionResult.status).isEqualTo(TaskExecutionResult.Status.Success);
        assertThat((Integer) executionResult.outputs.get("status")).isEqualTo(expectedStatus);
    }

    private Target mockTarget(String targetUrl) {
        SecurityInfo securityInfoMock = mock(SecurityInfo.class);
        when(securityInfoMock.keyStore()).thenReturn(Optional.empty());
        when(securityInfoMock.keyStorePassword()).thenReturn(Optional.empty());
        when(securityInfoMock.trustStore()).thenReturn(Optional.empty());
        when(securityInfoMock.trustStorePassword()).thenReturn(Optional.empty());
        Target targetMock = mock(Target.class);
        when(targetMock.url()).thenReturn(targetUrl);
        when(targetMock.security()).thenReturn(securityInfoMock);
        return targetMock;
    }

}
