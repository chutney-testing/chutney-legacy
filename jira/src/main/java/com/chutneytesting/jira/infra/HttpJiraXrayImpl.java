package com.chutneytesting.jira.infra;

import com.atlassian.jira.rest.client.api.JiraRestClient;
import com.atlassian.jira.rest.client.api.domain.BasicIssue;
import com.atlassian.jira.rest.client.api.domain.Issue;
import com.atlassian.jira.rest.client.api.domain.input.IssueInput;
import com.atlassian.jira.rest.client.api.domain.input.IssueInputBuilder;
import com.atlassian.jira.rest.client.internal.async.AsynchronousJiraRestClientFactory;
import com.chutneytesting.jira.domain.JiraTargetConfiguration;
import com.chutneytesting.jira.domain.JiraXrayApi;
import com.chutneytesting.jira.domain.exception.NoJiraConfigurationException;
import com.chutneytesting.jira.xrayapi.JiraIssueType;
import com.chutneytesting.jira.xrayapi.Xray;
import com.chutneytesting.jira.xrayapi.XrayTestExecTest;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import javax.net.ssl.SSLContext;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContextBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.client.support.BasicAuthenticationInterceptor;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

public class HttpJiraXrayImpl implements JiraXrayApi {

    private static final Logger LOGGER = LoggerFactory.getLogger(HttpJiraXrayImpl.class);

    private static final int MS_TIMEOUT = 10 * 1000; // 10 s

    private final JiraTargetConfiguration jiraTargetConfiguration;

    public HttpJiraXrayImpl(JiraTargetConfiguration jiraTargetConfiguration) {
        this.jiraTargetConfiguration = jiraTargetConfiguration;
        if (!jiraTargetConfiguration.isValid()) {
            throw new NoJiraConfigurationException();
        }
    }

    @Override
    public void updateRequest(Xray xray) {
        String updateUri = jiraTargetConfiguration.url() + "/rest/raven/1.0/import/execution";

        RestTemplate restTemplate = buildRestTemplate(jiraTargetConfiguration.username(), jiraTargetConfiguration.password());

        try {
            ResponseEntity<String> response = restTemplate.postForEntity(updateUri, xray, String.class);
            if (response.getStatusCode().equals(HttpStatus.OK)) {
                LOGGER.debug(response.toString());
                LOGGER.info("Xray successfully updated for " + xray.getTestExecutionKey());
            } else {
                LOGGER.error(response.toString());
            }
        } catch (RestClientException e) {
            throw new RuntimeException("Unable to update test execution [" + xray.getTestExecutionKey() + "] : ", e);
        }
    }

    @Override
    public List<XrayTestExecTest> getTestExecutionScenarios(String xrayId) {
        List<XrayTestExecTest> tests = new ArrayList<>();

        String uriTemplate = jiraTargetConfiguration.url() + "/rest/raven/1.0/api/%s/%s/test";
        String uri = String.format(uriTemplate, isTestPlan(xrayId) ? "testplan" : "testexec", xrayId);

        RestTemplate restTemplate = buildRestTemplate(jiraTargetConfiguration.username(), jiraTargetConfiguration.password());
        try {
            ResponseEntity<XrayTestExecTest[]> response = restTemplate.getForEntity(uri, XrayTestExecTest[].class);
            if (response.getStatusCode().equals(HttpStatus.OK) && response.getBody() != null) {
                tests = Arrays.stream(response.getBody())
                    .toList();
            } else {
                LOGGER.error(response.toString());
            }
        } catch (RestClientException e) {
            throw new RuntimeException("Unable to get xray test execution[" + xrayId + "] scenarios : ", e);
        }
        return tests;
    }

    @Override
    public void updateStatusByTestRunId(String testRuntId, String executionStatus) {
        String uriTemplate = jiraTargetConfiguration.url() + "/rest/raven/1.0/api/testrun/%s/status?status=%s";
        String uri = String.format(uriTemplate, testRuntId, executionStatus);

        RestTemplate restTemplate = buildRestTemplate(jiraTargetConfiguration.username(), jiraTargetConfiguration.password());
        try {
            restTemplate.put(uri, null);
        } catch (RestClientException e) {
            throw new RuntimeException("Unable to update xray testRuntId[" + testRuntId + "] with status[" + executionStatus + "] : ", e);
        }
    }

    @Override
    public void associateTestExecutionFromTestPlan(String testPlanId, String testExecutionId) {
        String uriTemplate = jiraTargetConfiguration.url() + "/rest/raven/1.0/api/testplan/%s/testexecution";
        String uri = String.format(uriTemplate, testPlanId);

        RestTemplate restTemplate = buildRestTemplate(jiraTargetConfiguration.username(), jiraTargetConfiguration.password());
        try {
            ResponseEntity<String> response = restTemplate.postForEntity(uri, Map.of("add", List.of(testExecutionId)), String.class);
            if (response.getStatusCode().equals(HttpStatus.OK)) {
                LOGGER.debug(response.toString());
                LOGGER.info("Xray successfully associate test execution [" + testExecutionId + "] from test plan [" + testPlanId + "]");
            } else {
                LOGGER.error(response.toString());
                throw new RuntimeException("Unable to associate test execution [" + testExecutionId + "] from test plan [" + testPlanId + "]");
            }
        } catch (RestClientException e) {
            throw new RuntimeException("Unable to associate test execution [" + testExecutionId + "] from test plan [" + testPlanId + "] : ", e);
        }
    }

    @Override
    public String createTestExecution(String testPlanId) {
        Issue parentIssue = getIssue(testPlanId);
        IssueInputBuilder issueInputBuilder = new IssueInputBuilder();

        IssueInput issueInput = issueInputBuilder
            .setProjectKey(parentIssue.getProject().getKey())
            .setIssueTypeId(getIssueTypeByName("Test Execution").getId())
            .setSummary(parentIssue.getSummary())
            .build();

        try(JiraRestClient jiraRestClient = getJiraRestClient()) {
            BasicIssue issue = jiraRestClient.getIssueClient().createIssue(issueInput).claim();
            associateTestExecutionFromTestPlan(testPlanId, issue.getKey());
            return issue.getKey();
        } catch (Exception e) {
            throw new RuntimeException("Unable to create test execution issue from test plan [" + testPlanId + "] : ", e);
        }
    }

    @Override
    public boolean isTestPlan(String issueId) {
        return getIssue(issueId).getIssueType().getId().equals(getIssueTypeByName("Test Plan").getId());
    }

    private Issue getIssue(String issueKey) {
        try(JiraRestClient jiraRestClient = getJiraRestClient()) {
            return jiraRestClient.getIssueClient().getIssue(issueKey).claim();
        } catch (Exception e) {
            throw new RuntimeException("Unable to get issue [" + issueKey + "] : ", e);
        }
    }

    private JiraIssueType getIssueTypeByName(String issueTypeName) {
        String uri = jiraTargetConfiguration.url() + "/rest/api/latest/issuetype";
        Optional<JiraIssueType> issueTypeOptional = Optional.empty();

        RestTemplate restTemplate = buildRestTemplate(jiraTargetConfiguration.username(), jiraTargetConfiguration.password());
        try {
            ResponseEntity<JiraIssueType[]> response = restTemplate.getForEntity(uri, JiraIssueType[].class);
            if (response.getStatusCode().equals(HttpStatus.OK) && response.getBody() != null) {
                issueTypeOptional = Arrays.stream(response.getBody())
                    .filter(issueType -> issueType.getName().equals(issueTypeName))
                    .findFirst();
            } else {
                LOGGER.error(response.toString());
            }
        } catch (RestClientException e) {
            throw new RuntimeException("Unable to get issues type list : ", e);
        }
        return issueTypeOptional.orElseThrow(() -> new RuntimeException("Unable to get issue type [" + issueTypeName + "]"));
    }

    private SSLContext buildSslContext() {
        try {
            SSLContextBuilder sslContextBuilder = new SSLContextBuilder();
            sslContextBuilder.loadTrustMaterial(null, (chain, authType) -> true);
            return sslContextBuilder.build();
        } catch (GeneralSecurityException e) {
            throw new IllegalArgumentException(e);
        }
    }

    private RestTemplate buildRestTemplate(String username, String password) {
        RestTemplate restTemplate;
        SSLContext sslContext = buildSslContext();
        SSLConnectionSocketFactory socketFactory = new SSLConnectionSocketFactory(sslContext, NoopHostnameVerifier.INSTANCE);
        CloseableHttpClient httpClient = HttpClients.custom()
            .setSSLSocketFactory(socketFactory)
            .build();

        HttpComponentsClientHttpRequestFactory requestFactory = new HttpComponentsClientHttpRequestFactory(httpClient);
        requestFactory.setReadTimeout(MS_TIMEOUT);
        requestFactory.setConnectTimeout(MS_TIMEOUT);

        restTemplate = new RestTemplate(requestFactory);
        restTemplate.getInterceptors().add(new BasicAuthenticationInterceptor(username, password));

        return restTemplate;
    }

    private JiraRestClient getJiraRestClient() {
        try {
            AsynchronousJiraRestClientFactory asynchronousJiraRestClientFactory = new AsynchronousJiraRestClientFactory();
            return asynchronousJiraRestClientFactory
                .createWithBasicHttpAuthentication(
                    new URI(jiraTargetConfiguration.url()),
                    jiraTargetConfiguration.username(),
                    jiraTargetConfiguration.password());
        } catch (URISyntaxException e) {
            throw new RuntimeException("Unable to instantiate Jira rest client from url [" + jiraTargetConfiguration.url() + "] : ", e);
        }
    }
}
