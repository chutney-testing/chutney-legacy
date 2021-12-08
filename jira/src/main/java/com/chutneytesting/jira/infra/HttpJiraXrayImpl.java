package com.chutneytesting.jira.infra;

import static org.apache.commons.lang3.StringUtils.isNotBlank;

import com.chutneytesting.jira.domain.JiraTargetConfiguration;
import com.chutneytesting.jira.domain.JiraXrayApi;
import com.chutneytesting.jira.infra.xraymodelapi.Xray;
import com.chutneytesting.jira.infra.xraymodelapi.XrayTestExecTest;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
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

public class HttpJiraXrayImpl implements JiraXrayApi {

    private static final Logger LOGGER = LoggerFactory.getLogger(HttpJiraXrayImpl.class);

    private static final int MS_TIMEOUT = 10 * 1000; // 10 s

    public HttpJiraXrayImpl() {
    }

    @Override
    public void updateRequest(Xray xray, JiraTargetConfiguration jiraTargetConfiguration) {
        String updateUri = jiraTargetConfiguration.url + "/rest/raven/1.0/import/execution";

        if (isNotBlank(jiraTargetConfiguration.url)) {
            LOGGER.error("Unable to update xray, jira url is undefined");
            return;
        }

        RestTemplate restTemplate = buildRestTemplate(jiraTargetConfiguration.username, jiraTargetConfiguration.password);

        try {
            ResponseEntity response = restTemplate.postForEntity(updateUri, xray, String.class);
            if (response.getStatusCode().equals(HttpStatus.OK)) {
                LOGGER.debug(response.toString());
                LOGGER.info("Xray successfully updated for " + xray.getTestExecutionKey());
            } else {
                LOGGER.error(response.toString());
            }
        } catch (RestClientException e) {
            LOGGER.error("Unable to update xray : " + e);
        }
    }

    @Override
    public List<String> getTestExecutionScenarios(String testExecutionId, JiraTargetConfiguration jiraTargetConfiguration)  {
        List<String> scenarios = new ArrayList<>();

        String uriTemplate = jiraTargetConfiguration.url + "/rest/raven/1.0/api/testexec/%s/test";
        String uri = String.format(uriTemplate, testExecutionId);

        RestTemplate restTemplate = buildRestTemplate(jiraTargetConfiguration.username, jiraTargetConfiguration.password);
        try {
            ResponseEntity<XrayTestExecTest[]> response = restTemplate.getForEntity(uri, XrayTestExecTest[].class);
            if (response.getStatusCode().equals(HttpStatus.OK)) {
                scenarios = Arrays.stream(response.getBody())
                    .map(XrayTestExecTest::getKey)
                    .collect(Collectors.toList());
            } else {
                LOGGER.error(response.toString());
            }
        } catch (RestClientException e) {
            LOGGER.error("Unable to get xray test execution[" + testExecutionId + "] scenarios : " + e);
        }
        return scenarios;
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

        ClientHttpRequestFactory requestFactory = new HttpComponentsClientHttpRequestFactory(httpClient);
        ((HttpComponentsClientHttpRequestFactory) requestFactory).setReadTimeout(MS_TIMEOUT);
        ((HttpComponentsClientHttpRequestFactory) requestFactory).setConnectTimeout(MS_TIMEOUT);

        restTemplate = new RestTemplate(requestFactory);
        restTemplate.getInterceptors().add(new BasicAuthenticationInterceptor(username, password));

        return restTemplate;
    }
}
