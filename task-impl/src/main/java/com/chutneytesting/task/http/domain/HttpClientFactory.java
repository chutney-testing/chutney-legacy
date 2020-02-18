package com.chutneytesting.task.http.domain;

import com.chutneytesting.task.spi.injectable.SecurityInfo;
import com.chutneytesting.task.spi.injectable.Target;
import java.io.IOException;
import java.nio.file.Paths;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import javax.net.ssl.SSLContext;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContextBuilder;
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
        RestTemplate restTemplate = buildRestTemplate(target.security(), timeout);

        return (httpMethod, resource, input) -> restTemplate.exchange(target.url() + resource, httpMethod, input, responseType);
    }

    public HttpClient create(Target target, Class<String> responseType, int timeout) {
        RestTemplate restTemplate = buildRestTemplate(target.security(), timeout);

        return (httpMethod, resource, input) -> restTemplate.exchange(target.url() + resource, httpMethod, input, responseType);
    }

    private static RestTemplate buildRestTemplate(SecurityInfo securityInfo, int timeout) {
        SSLContext sslContext = buildSslContext(securityInfo);

        SSLConnectionSocketFactory socketFactory = new SSLConnectionSocketFactory(sslContext, NoopHostnameVerifier.INSTANCE);

        CloseableHttpClient httpClient = HttpClients.custom()
            .setSSLSocketFactory(socketFactory)
            .build();

        ClientHttpRequestFactory requestFactory = new HttpComponentsClientHttpRequestFactory(httpClient);
        ((HttpComponentsClientHttpRequestFactory) requestFactory).setReadTimeout(timeout);
        ((HttpComponentsClientHttpRequestFactory) requestFactory).setConnectTimeout(timeout);

        RestTemplate restTemplate = new RestTemplate(requestFactory);
        configureBasicAuth(securityInfo, restTemplate);
        removeErrorHandler(restTemplate);
        return restTemplate;
    }

    private static SSLContext buildSslContext(SecurityInfo securityInfo) {
        try {
            SSLContextBuilder sslContextBuilder = new SSLContextBuilder();
            configureTrustStore(securityInfo, sslContextBuilder);
            configureKeyStore(securityInfo, sslContextBuilder);
            return sslContextBuilder.build();
        } catch (GeneralSecurityException | IOException e) {
            throw new IllegalArgumentException(e);
        }
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

    private static void configureKeyStore(SecurityInfo securityInfo, SSLContextBuilder sslContextBuilder) throws KeyStoreException, IOException, NoSuchAlgorithmException, CertificateException, UnrecoverableKeyException {
        if (securityInfo.keyStore().isPresent()) {
            KeyStore keyMaterial = KeyStore.getInstance(KeyStore.getDefaultType());
            keyMaterial.load(Paths.get(securityInfo.keyStore().get()).toUri().toURL().openStream(), securityInfo.keyStorePassword().orElse("").toCharArray());
            sslContextBuilder.loadKeyMaterial(keyMaterial, securityInfo.keyStorePassword().orElse("").toCharArray());
        }
    }

    private static void configureTrustStore(SecurityInfo securityInfo, SSLContextBuilder sslContextBuilder) throws KeyStoreException, IOException, NoSuchAlgorithmException, CertificateException {
        if (securityInfo.trustStore().isPresent()) {
            KeyStore trustMaterial = KeyStore.getInstance(KeyStore.getDefaultType());
            trustMaterial.load(Paths.get(securityInfo.trustStore().get()).toUri().toURL().openStream(), securityInfo.trustStorePassword().orElse("").toCharArray());
            sslContextBuilder.loadTrustMaterial(trustMaterial, new TrustSelfSignedStrategy());
        } else {
            sslContextBuilder.loadTrustMaterial(null, (chain, authType) -> true);
        }
    }

    private static class NoOpResponseErrorHandler extends DefaultResponseErrorHandler {
        @Override
        public void handleError(ClientHttpResponse response) {
        }
    }
}
