package com.chutneytesting.jira.infra;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathMatching;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;

import com.chutneytesting.jira.domain.JiraTargetConfiguration;
import com.chutneytesting.jira.xrayapi.Xray;
import com.chutneytesting.jira.xrayapi.XrayInfo;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import java.util.Base64;
import java.util.List;
import org.junit.jupiter.api.Test;

public class HttpJiraXrayImplTest {

  @Test
  void should_use_proxy_without_auth() {

    // Given
    WireMockConfiguration wireMockConfiguration = wireMockConfig()
        .dynamicPort();

    WireMockServer wireMockServer = new WireMockServer(wireMockConfiguration);
    wireMockServer.start();

    JiraTargetConfiguration jiraTargetConfiguration = new JiraTargetConfiguration(
        "http://fake-server-jira",
        "user",
        "password",
        wireMockServer.baseUrl(),
        null,
        null);

    wireMockServer.stubFor(
        post(urlPathMatching("/rest/raven/1.0/import/execution"))
            .willReturn(
                aResponse()
                    .withHeader("Content-Type", "application/json")
                    .withBody("1234")
                    .withStatus(200))
    );

    // When
    HttpJiraXrayImpl httpJiraXray = new HttpJiraXrayImpl(jiraTargetConfiguration);
    httpJiraXray.updateRequest(new Xray("test", List.of(), new XrayInfo(List.of())));

    // Then
    wireMockServer.verify(
        1, postRequestedFor(urlPathMatching("/rest/raven/1.0/import/execution"))
            .withoutHeader("Proxy-Authorization")
    );
  }

    @Test
    void should_use_proxy_with_auth() {

        // Given
        WireMockConfiguration wireMockConfiguration = wireMockConfig()
            .dynamicPort();

        WireMockServer wireMockServer = new WireMockServer(wireMockConfiguration);
        wireMockServer.start();

        JiraTargetConfiguration jiraTargetConfiguration = new JiraTargetConfiguration(
            "http://fake-server-jira",
            "user",
            "password",
            wireMockServer.baseUrl(),
            "userProxy",
            "passwordProxy");

        wireMockServer.stubFor(
            post(urlPathMatching("/rest/raven/1.0/import/execution"))
                .willReturn(
                    aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBody("1234")
                        .withStatus(200))
        );

        String expectedProxyAuthorization = Base64.getEncoder()
            .encodeToString((jiraTargetConfiguration.userProxy() + ":" + jiraTargetConfiguration.passwordProxy()).getBytes());

        // When
        HttpJiraXrayImpl httpJiraXray = new HttpJiraXrayImpl(jiraTargetConfiguration);
        httpJiraXray.updateRequest(new Xray("test", List.of(), new XrayInfo(List.of())));

        // Then
        wireMockServer.verify(
            1, postRequestedFor(urlPathMatching("/rest/raven/1.0/import/execution"))
                .withHeader("Proxy-Authorization", equalTo("Basic " + expectedProxyAuthorization))
        );
  }
}
