package com.chutneytesting.task.http.domain;

import static com.chutneytesting.task.common.SecurityUtils.buildSslContext;
import static java.util.Optional.ofNullable;

import com.chutneytesting.task.spi.injectable.SecurityInfo;
import com.chutneytesting.task.spi.injectable.Target;
import java.net.ProxySelector;
import java.security.GeneralSecurityException;
import java.util.Map;
import java.util.Optional;
import javax.net.ssl.SSLContext;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.SystemDefaultRoutePlanner;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.client.support.BasicAuthorizationInterceptor;
import org.springframework.web.client.DefaultResponseErrorHandler;
import org.springframework.web.client.RestTemplate;

public class HttpClientFactory {

    /**
     * @return an {@link HttpClient} depending on given {@link Target} able to handle:
     * <ul>
     * <li>no security</li>
     * <li>basic-auth</li>
     * <li>TLS (trust everything by default, hostname verifier is disabled)
     * <ul>
     * <li>TLS one-way</li>
     * <li>TLS two-way</li>
     * </ul>
     * </li>
     * </ul>
     */
    public HttpClient create(Target target, ParameterizedTypeReference<String> responseType, int timeout) {
        RestTemplate restTemplate = buildRestTemplate(target.properties(), target.security(), timeout);

        return (httpMethod, resource, input) -> restTemplate.exchange(target.url() + resource, httpMethod, input, responseType);
    }

    public HttpClient create(Target target, Class<String> responseType, int timeout) {
        RestTemplate restTemplate = buildRestTemplate(target.properties(), target.security(), timeout);

        return (httpMethod, resource, input) -> restTemplate.exchange(target.url() + resource, httpMethod, input, responseType);
    }

    private static RestTemplate buildRestTemplate(Map<String, String> properties, SecurityInfo securityInfo, int timeout) {

        SSLContext sslContext;
        try {
            sslContext = buildSslContext(properties, securityInfo).build();
        } catch (GeneralSecurityException e) {
            throw new IllegalArgumentException(e.getMessage(), e);
        }

        SSLConnectionSocketFactory socketFactory = new SSLConnectionSocketFactory(sslContext, NoopHostnameVerifier.INSTANCE);

        HttpClientBuilder httpClient = HttpClients.custom()
            .setSSLSocketFactory(socketFactory);

        // Proxy
        Optional<String> proxyHost = ofNullable(System.getProperty("http.proxyHost"));
        proxyHost.ifPresent(host -> httpClient.setRoutePlanner(new SystemDefaultRoutePlanner(ProxySelector.getDefault())));

        ClientHttpRequestFactory requestFactory = new HttpComponentsClientHttpRequestFactory(httpClient.build());
        ((HttpComponentsClientHttpRequestFactory) requestFactory).setReadTimeout(timeout);
        ((HttpComponentsClientHttpRequestFactory) requestFactory).setConnectTimeout(timeout);

        RestTemplate restTemplate = new RestTemplate(requestFactory);
        configureBasicAuth(securityInfo, restTemplate);
        removeErrorHandler(restTemplate);
        return restTemplate;
    }

    private static void removeErrorHandler(RestTemplate restTemplate) {
        restTemplate.setErrorHandler(new NoOpResponseErrorHandler());
    }

    private static void configureBasicAuth(SecurityInfo securityInfo, RestTemplate restTemplate) {
        if (securityInfo.credential().isPresent()) {
            restTemplate.getInterceptors().add(
                new BasicAuthorizationInterceptor(securityInfo.credential().get().username(), securityInfo.credential().get().password()));
        }
    }

    private static class NoOpResponseErrorHandler extends DefaultResponseErrorHandler {
        @Override
        public void handleError(ClientHttpResponse response) {
        }
    }
}
