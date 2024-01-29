/*
 * Copyright 2017-2023 Enedis
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.chutneytesting.jira.infra;

import static com.github.tomakehurst.wiremock.client.WireMock.anyRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.anyUrl;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.okJson;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathMatching;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.assertj.core.api.Assertions.assertThat;

import com.chutneytesting.jira.domain.JiraTargetConfiguration;
import com.chutneytesting.jira.xrayapi.Xray;
import com.chutneytesting.jira.xrayapi.XrayInfo;
import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import java.util.Base64;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

public class HttpJiraXrayImplTest {

    @RegisterExtension
    static WireMockExtension proxyMock = WireMockExtension.newInstance()
        .options(
            wireMockConfig()
                .httpDisabled(true)
                .dynamicHttpsPort()
        )
        .failOnUnmatchedRequests(true)
        .build();

    @Nested
    @DisplayName("Proxy without authentication")
    class ProxyWithoutAuth {
        @Test
        void update_xray_execution() {
            // Given
            JiraTargetConfiguration config = new JiraTargetConfiguration(
                "http://fake-server-jira",
                "user",
                "password",
                proxyMock.baseUrl(),
                "",
                ""
            );

            proxyMock.stubFor(
                post(urlPathMatching("/rest/raven/1.0/import/execution"))
                    .willReturn(okJson("1234"))
            );

            // When
            HttpJiraXrayImpl httpJiraXray = new HttpJiraXrayImpl(config);
            httpJiraXray.updateRequest(new Xray("test", List.of(), new XrayInfo(List.of())));

            // Then
            proxyMock.verify(
                1, postRequestedFor(anyUrl())
                    .withHeader("Authorization", equalTo(expectedAuthorization(config)))
                    .withoutHeader("Proxy-Authorization")
            );
        }

        @Test
        void test_issue_as_test_plan() {
            // Given
            String issueId = "PRJ-666";

            var config = new JiraTargetConfiguration(
                "http://fake-server-jira",
                "user",
                "password",
                proxyMock.baseUrl(),
                "",
                ""
            );

            proxyMock.stubFor(
                get(urlPathMatching("/rest/api/latest/issue/" + issueId + ".*"))
                    .willReturn(okJson("""
                        {
                            "self": "...",
                            "key": "1234",
                            "id": 1234,
                            "expand": "one,two",
                            "fields": {
                                "summary": "",
                                "issuetype": {
                                    "self": "...",
                                    "id": 123,
                                    "name": "Test Plan",
                                    "subtask": false
                                },
                                "created": "2024-01-01T00:00:00.000Z",
                                "updated": "2024-01-01T00:00:00.000Z",
                                "project": {
                                    "self": "...",
                                    "key": ""
                                },
                                "status": {
                                    "self": "...",
                                    "name": "",
                                    "description": "",
                                    "iconUrl": "http://host/icon"
                                }
                            },
                            "names": {
                            },
                            "schema": {
                            }
                        }
                        """.stripIndent()
                    ))
            );

            proxyMock.stubFor(
                get(urlPathMatching("/rest/api/latest/issuetype"))
                    .willReturn(okJson("""
                        [
                            {
                                "self": "...",
                                "id": 321,
                                "name": "fakeType",
                                "subtask": false
                            },
                            {
                                "self": "...",
                                "id": 123,
                                "name": "Test Plan",
                                "subtask": false
                            }
                        ]
                        """.stripIndent()
                    ))
            );

            // When
            var sut = new HttpJiraXrayImpl(config);
            boolean isTestPlan = sut.isTestPlan(issueId);

            // Then
            proxyMock.verify(
                2, anyRequestedFor(anyUrl())
                    .withHeader("Authorization", equalTo(expectedAuthorization(config)))
                    .withoutHeader("Proxy-Authorization")
            );
            assertThat(isTestPlan).isTrue();
        }
    }

    @Nested
    @DisplayName("Proxy with authentication")
    class ProxyWithAuth {
        @Test
        void update_xray_execution() {
            // Given
            JiraTargetConfiguration config = new JiraTargetConfiguration(
                "http://fake-server-jira",
                "user",
                "password",
                proxyMock.baseUrl(),
                "userProxy",
                "passwordProxy"
            );

            proxyMock.stubFor(
                post(urlPathMatching("/rest/raven/1.0/import/execution"))
                    .willReturn(okJson("1234"))
            );

            // When
            HttpJiraXrayImpl httpJiraXray = new HttpJiraXrayImpl(config);
            httpJiraXray.updateRequest(new Xray("test", List.of(), new XrayInfo(List.of())));

            // Then
            proxyMock.verify(
                1, postRequestedFor(anyUrl())
                    .withHeader("Authorization", equalTo(expectedAuthorization(config)))
                    .withHeader("Proxy-Authorization", equalTo(expectedProxyAuthorization(config)))
            );
        }

        @Test
        void test_issue_as_test_plan() {
            // Given
            String issueId = "PRJ-666";

            var config = new JiraTargetConfiguration(
                "http://fake-server-jira",
                "user",
                "password",
                proxyMock.baseUrl(),
                "userProxy",
                "passwordProxy"
            );

            proxyMock.stubFor(
                get(urlPathMatching("/rest/api/latest/issue/" + issueId + ".*"))
                    .willReturn(okJson("""
                        {
                            "self": "...",
                            "key": "1234",
                            "id": 1234,
                            "expand": "one,two",
                            "fields": {
                                "summary": "",
                                "issuetype": {
                                    "self": "...",
                                    "id": 123,
                                    "name": "Test Plan",
                                    "subtask": false
                                },
                                "created": "2024-01-01T00:00:00.000Z",
                                "updated": "2024-01-01T00:00:00.000Z",
                                "project": {
                                    "self": "...",
                                    "key": ""
                                },
                                "status": {
                                    "self": "...",
                                    "name": "",
                                    "description": "",
                                    "iconUrl": "http://host/icon"
                                }
                            },
                            "names": {
                            },
                            "schema": {
                            }
                        }
                        """.stripIndent()
                    ))
            );

            proxyMock.stubFor(
                get(urlPathMatching("/rest/api/latest/issuetype"))
                    .willReturn(okJson("""
                        [
                            {
                                "self": "...",
                                "id": 321,
                                "name": "fakeType",
                                "subtask": false
                            },
                            {
                                "self": "...",
                                "id": 123,
                                "name": "Test Plan",
                                "subtask": false
                            }
                        ]
                        """.stripIndent()
                    ))
            );

            // When
            var sut = new HttpJiraXrayImpl(config);
            boolean isTestPlan = sut.isTestPlan(issueId);

            // Then
            proxyMock.verify(
                2, anyRequestedFor(anyUrl())
                    .withHeader("Authorization", equalTo(expectedAuthorization(config)))
                    .withHeader("Proxy-Authorization", equalTo(expectedProxyAuthorization(config)))
            );
            assertThat(isTestPlan).isTrue();
        }
    }

    private static String expectedProxyAuthorization(JiraTargetConfiguration config) {
        return "Basic " + Base64.getEncoder()
            .encodeToString((config.userProxy() + ":" + config.passwordProxy()).getBytes());
    }

    private static String expectedAuthorization(JiraTargetConfiguration config) {
        return "Basic " + Base64.getEncoder()
            .encodeToString((config.username() + ":" + config.password()).getBytes());
    }
}
