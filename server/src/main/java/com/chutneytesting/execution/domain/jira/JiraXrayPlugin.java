package com.chutneytesting.execution.domain.jira;

import com.chutneytesting.design.domain.plugins.jira.JiraRepository;
import com.chutneytesting.design.domain.plugins.jira.JiraTargetConfiguration;
import com.chutneytesting.design.domain.plugins.jira.Xray;
import com.chutneytesting.design.domain.plugins.jira.XrayEvidence;
import com.chutneytesting.design.domain.plugins.jira.XrayInfo;
import com.chutneytesting.design.domain.plugins.jira.XrayTest;
import com.chutneytesting.execution.domain.report.ScenarioExecutionReport;
import com.chutneytesting.execution.domain.report.ServerReportStatus;
import com.chutneytesting.execution.domain.report.StepExecutionReportCore;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
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
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.client.support.BasicAuthenticationInterceptor;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

public class JiraXrayPlugin {

    private static final Logger LOGGER = LoggerFactory.getLogger(JiraXrayPlugin.class);
    private static final String SUCCESS_STATUS = "PASS";
    private static final String FAILED_STATUS = "FAIL";
    private static final int TIMEOUT = 10 * 1000;


    private final JiraRepository jiraRepository;
    private final ObjectMapper objectMapper;

    public JiraXrayPlugin(JiraRepository jiraRepository, ObjectMapper objectMapper) {
        this.jiraRepository = jiraRepository;
        this.objectMapper = objectMapper;
    }

    public void updateTestExecution(Long campaignId, String scenarioId, String stringReport) {
        ScenarioExecutionReport scenarioExecutionReport = formatReport(stringReport);
        String testKey = jiraRepository.getByScenarioId(scenarioId);
        String testExecutionKey = jiraRepository.getByCampaignId(campaignId.toString());
        if (!testKey.isEmpty() && !testExecutionKey.isEmpty()) {
            LOGGER.info("Update xray test {} of test execution {}", testKey, testExecutionKey);
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssZZZZZ");
            XrayTest xrayTest = new XrayTest(
                testKey,
                scenarioExecutionReport.report.startDate.atZone(ZoneId.systemDefault()).format(formatter),
                scenarioExecutionReport.report.startDate.plusNanos(scenarioExecutionReport.report.duration * 1000000).atZone(ZoneId.systemDefault()).format(formatter),
                getErrors(scenarioExecutionReport.report).toString(),
                scenarioExecutionReport.report.status.equals(ServerReportStatus.SUCCESS) ? SUCCESS_STATUS : FAILED_STATUS
            );

            xrayTest.setEvidences(getEvidences(scenarioExecutionReport.report, ""));
            XrayInfo info = new XrayInfo(Arrays.asList(scenarioExecutionReport.environment));
            Xray xray = new Xray(testExecutionKey, Arrays.asList(xrayTest), info);
            updateRequest(xray);
        }
    }

    private void updateRequest(Xray xray) {
        JiraTargetConfiguration jiraTargetConfiguration = jiraRepository.loadServerConfiguration();
        String updateUri = jiraTargetConfiguration.url + "/rest/raven/1.0/import/execution";

        if (jiraTargetConfiguration.url.isEmpty()) {
            LOGGER.error("Unable to update xray, jira url is undefined");
            return;
        }

        RestTemplate restTemplate = buildRestTemplate();
        configureBasicAuth(jiraTargetConfiguration.username, jiraTargetConfiguration.password, restTemplate);

        try {
            ResponseEntity response = restTemplate.postForEntity(updateUri, xray, String.class);
            if (response.getStatusCode().equals(HttpStatus.OK)) {
                LOGGER.debug(response.toString());
                LOGGER.info("Xray successfully updated");
            } else {
                LOGGER.error(response.toString());
            }
        } catch (RestClientException e) {
            LOGGER.error("Unable to update xray : " + e);
        }
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

    private RestTemplate buildRestTemplate() {
        RestTemplate restTemplate;
        SSLContext sslContext = buildSslContext();
        SSLConnectionSocketFactory socketFactory = new SSLConnectionSocketFactory(sslContext, NoopHostnameVerifier.INSTANCE);
        CloseableHttpClient httpClient = HttpClients.custom()
            .setSSLSocketFactory(socketFactory)
            .build();

        ClientHttpRequestFactory requestFactory = new HttpComponentsClientHttpRequestFactory(httpClient);
        ((HttpComponentsClientHttpRequestFactory) requestFactory).setReadTimeout(TIMEOUT);
        ((HttpComponentsClientHttpRequestFactory) requestFactory).setConnectTimeout(TIMEOUT);

        restTemplate = new RestTemplate(requestFactory);

        return restTemplate;
    }

    private ScenarioExecutionReport formatReport(String stringReport) {
        ScenarioExecutionReport report = null;
        try {
            report = objectMapper.readValue(stringReport, ScenarioExecutionReport.class);
        } catch (IOException e) {
            LOGGER.error(e.getMessage());
        }
        return report;
    }

    private List<String> getErrors(StepExecutionReportCore stepExecutionReportCore) {
        List<String> errors = new ArrayList<>();
        getErrors(stepExecutionReportCore, "").forEach((k, v) -> errors.add(k + " => " + v));
        return errors;
    }

    private Map<String, String> getErrors(StepExecutionReportCore stepExecutionReportCore, String parentStep) {
        Map<String, String> errors = new HashMap<>();
        if (!stepExecutionReportCore.errors.isEmpty()) {
            errors.put(parentStep + " > " + stepExecutionReportCore.name,
                stepExecutionReportCore.errors.stream().filter(s -> !s.startsWith("data:image/png")).collect(Collectors.toList()).toString());
        }
        if (!stepExecutionReportCore.steps.isEmpty()) {
            stepExecutionReportCore.steps
                .stream()
                .forEach(step -> errors.putAll(getErrors(step, parentStep + " > " + stepExecutionReportCore.name)));
        }
        return errors;
    }

    private List<XrayEvidence> getEvidences(StepExecutionReportCore stepExecutionReportCore, String parentStep) {
        List<XrayEvidence> evidences = new ArrayList<>();
        if (!stepExecutionReportCore.errors.isEmpty()) {
            evidences.addAll(
                stepExecutionReportCore.errors
                    .stream()
                    .filter(s -> s.startsWith("data:image/png"))
                    .map(s -> new XrayEvidence(s.replace("data:image/png;base64,", ""), formatEvidenceFilename(parentStep, stepExecutionReportCore.name) + ".png", "image/png"))
                    .collect(Collectors.toList())
            );
        }
        if (!stepExecutionReportCore.steps.isEmpty()) {
            stepExecutionReportCore.steps
                .stream()
                .forEach(step -> evidences.addAll(getEvidences(step, formatEvidenceFilename(parentStep, stepExecutionReportCore.name))));
        }
        return evidences;
    }

    private String formatEvidenceFilename(String parentStep, String stepName) {
        return parentStep.trim().replace(" ", "-")
            + (parentStep.trim().isEmpty() ? "" : "_")
            + stepName.trim().replace(" ", "-");
    }

    private void configureBasicAuth(String username, String password, RestTemplate restTemplate) {
        restTemplate.getInterceptors().add(new BasicAuthenticationInterceptor(username, password));
    }
}
