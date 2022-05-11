package com.chutneytesting.task.http.domain;

import com.chutneytesting.task.spi.injectable.SecurityInfo;
import com.chutneytesting.task.spi.injectable.Target;
import java.io.IOException;
import java.net.ProxySelector;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;
import javax.net.ssl.SSLContext;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.SystemDefaultRoutePlanner;
import org.apache.http.ssl.SSLContextBuilder;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.client.support.BasicAuthenticationInterceptor;
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
        SSLContext sslContext = buildSslContext(properties, securityInfo);

        SSLConnectionSocketFactory socketFactory = new SSLConnectionSocketFactory(sslContext, NoopHostnameVerifier.INSTANCE);

        HttpClientBuilder httpClient = HttpClients.custom()
            .setSSLSocketFactory(socketFactory);

        if (isSystemProxySet()) {
            httpClient.setRoutePlanner(new SystemDefaultRoutePlanner(ProxySelector.getDefault()));
        }

        HttpComponentsClientHttpRequestFactory requestFactory = new HttpComponentsClientHttpRequestFactory(httpClient.build());
        requestFactory.setReadTimeout(timeout);
        requestFactory.setConnectTimeout(timeout);

        RestTemplate restTemplate = new RestTemplate(requestFactory);
        configureBasicAuth(securityInfo, restTemplate);
        removeErrorHandler(restTemplate);
        return restTemplate;
    }

    private static SSLContext buildSslContext(Map<String, String> properties, SecurityInfo securityInfo) {
        try {
            SSLContextBuilder sslContextBuilder = new SSLContextBuilder();
            configureTrustStore(properties, securityInfo, sslContextBuilder);
            configureKeyStore(properties, securityInfo, sslContextBuilder);
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
                new BasicAuthenticationInterceptor(securityInfo.credential().get().username(), securityInfo.credential().get().password(), StandardCharsets.UTF_8)
            );
        }
    }

    static void configureKeyStore(Map<String, String> properties, SecurityInfo securityInfo, SSLContextBuilder sslContextBuilder) throws KeyStoreException, IOException, NoSuchAlgorithmException, CertificateException, UnrecoverableKeyException {
        Optional<String> keystore = securityInfo.keyStore().or(() -> findProperty(properties, "keystore"));
        String keystorePassword = securityInfo.keyStorePassword().or(() -> findProperty(properties, "keystorePassword")).orElse("");
        String keyPassword = securityInfo.keyPassword().or(() -> findProperty(properties, "keyPassword")).orElse("");
        if (keystore.isPresent()) {
            KeyStore store = KeyStore.getInstance(KeyStore.getDefaultType());
            store.load(Paths.get(keystore.get()).toUri().toURL().openStream(), keystorePassword.toCharArray());
            sslContextBuilder.loadKeyMaterial(store, keyPassword.toCharArray());
        }
    }

    static void configureTrustStore(Map<String, String> properties, SecurityInfo securityInfo, SSLContextBuilder sslContextBuilder) throws KeyStoreException, IOException, NoSuchAlgorithmException, CertificateException {
        Optional<String> truststore = securityInfo.trustStore().or(() -> findProperty(properties, "truststore"));
        String truststorePassword = securityInfo.trustStorePassword().or(() -> findProperty(properties, "truststorePassword")).orElse("");
        if (truststore.isPresent()) {
            KeyStore trustMaterial = KeyStore.getInstance(KeyStore.getDefaultType());
            trustMaterial.load(Paths.get(truststore.get()).toUri().toURL().openStream(), truststorePassword.toCharArray());
            sslContextBuilder.loadTrustMaterial(trustMaterial, new TrustSelfSignedStrategy());
        } else {
            sslContextBuilder.loadTrustMaterial(null, (chain, authType) -> true);
        }
    }

    private static Optional<String> findProperty(Map<String, String> properties, String key) {
        return properties.entrySet().stream()
            .filter(e -> e.getKey().equalsIgnoreCase(key))
            .findFirst().map(Map.Entry::getValue);
    }

    private static Boolean isSystemProxySet() {
        return Stream.of("http.proxyHost", "https.proxyHost")
            .map(System::getProperty)
            .anyMatch(Objects::nonNull);
    }

    private static class NoOpResponseErrorHandler extends DefaultResponseErrorHandler {
        @Override
        public void handleError(ClientHttpResponse response) {
        }
    }
}
