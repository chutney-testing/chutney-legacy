package com.chutneytesting.execution.domain.jira;

import com.chutneytesting.design.domain.campaign.ScenarioExecutionReportCampaign;
import com.chutneytesting.design.domain.jira.JiraRepository;
import com.chutneytesting.design.domain.jira.JiraTargetConfiguration;
import com.chutneytesting.design.domain.jira.Xray;
import com.chutneytesting.design.domain.jira.XrayTest;
import com.chutneytesting.execution.domain.report.ServerReportStatus;
import java.security.GeneralSecurityException;
import java.util.Arrays;
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
import org.springframework.web.client.RestTemplate;

public class JiraExecutionEngine {

    private static final Logger LOGGER = LoggerFactory.getLogger(JiraExecutionEngine.class);
    private static final String SUCCESS_STATUS = "PASS";
    private static final String FAILED_STATUS = "FAIL";
    private static final int TIMEOUT = 10 * 1000;


    private final JiraRepository jiraRepository;

    public JiraExecutionEngine(JiraRepository jiraRepository) {
        this.jiraRepository = jiraRepository;
    }

    public void updateTestExecution(Long campaignId, ScenarioExecutionReportCampaign scenarioExecutionReportCampaign) {

        String testKey = jiraRepository.getByScenarioId(scenarioExecutionReportCampaign.scenarioId);
        String testExecutionKey = jiraRepository.getByCampaignId(campaignId.toString());
        if (!testKey.isEmpty() && !testExecutionKey.isEmpty()) {
            XrayTest xrayTest = new XrayTest(
                testKey,
                scenarioExecutionReportCampaign.execution.time().toString(),
                scenarioExecutionReportCampaign.execution.time()
                    .plusNanos(scenarioExecutionReportCampaign.execution.duration() * 1000000)
                    .toString(),
                scenarioExecutionReportCampaign.execution.error().orElse(""),
                scenarioExecutionReportCampaign.status().equals(ServerReportStatus.SUCCESS) ? SUCCESS_STATUS : FAILED_STATUS
            );
            Xray xray = new Xray(testExecutionKey.toString(), Arrays.asList(xrayTest));
            updateRequest(xray);
            LOGGER.info("Update xray test {} of test execution {}", testKey, testExecutionKey);
        }
    }

    private void updateRequest(Xray xray) {
        JiraTargetConfiguration jiraTargetConfiguration = jiraRepository.loadServerConfiguration();
        String updateUri = jiraTargetConfiguration.url + "/rest/raven/1.0/import/execution";

        RestTemplate restTemplate = buildRestTemplate();
        configureBasicAuth(jiraTargetConfiguration.username, jiraTargetConfiguration.password, restTemplate);

        ResponseEntity response = restTemplate.postForEntity(updateUri, xray, String.class);
        if (response.getStatusCode().equals(HttpStatus.OK)) {
            LOGGER.debug(response.toString());
        } else {
            LOGGER.error(response.toString());
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

    private void configureBasicAuth(String username, String password, RestTemplate restTemplate) {
        restTemplate.getInterceptors().add(new BasicAuthenticationInterceptor(username, password));
    }
}
