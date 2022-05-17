package com.chutneytesting.task.http.domain;

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
        RestTemplate restTemplate = buildRestTemplate(target, timeout);

        return (httpMethod, resource, input) -> restTemplate.exchange(target.url() + resource, httpMethod, input, responseType);
    }

    public HttpClient create(Target target, Class<String> responseType, int timeout) {
        RestTemplate restTemplate = buildRestTemplate(target, timeout);

        return (httpMethod, resource, input) -> restTemplate.exchange(target.url() + resource, httpMethod, input, responseType);
    }

    private static RestTemplate buildRestTemplate(Target target, int timeout) {
        SSLContext sslContext = buildSslContext(target);

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
        configureBasicAuth(target, restTemplate);
        removeErrorHandler(restTemplate);
        return restTemplate;
    }

    private static SSLContext buildSslContext(Target target) {
        try {
            SSLContextBuilder sslContextBuilder = new SSLContextBuilder();
            configureTrustStore(target, sslContextBuilder);
            configureKeyStore(target, sslContextBuilder);
            return sslContextBuilder.build();
        } catch (GeneralSecurityException | IOException e) {
            throw new IllegalArgumentException(e);
        }
    }

    private static void removeErrorHandler(RestTemplate restTemplate) {
        restTemplate.setErrorHandler(new NoOpResponseErrorHandler());
    }

    private static void configureBasicAuth(Target target, RestTemplate restTemplate) {
        if (target.user().isPresent()) {
            String user = target.user().get();
            String password = target.userPassword().orElse("");
            restTemplate.getInterceptors().add(
                new BasicAuthenticationInterceptor(user, password, StandardCharsets.UTF_8)
            );
        }
    }

    static void configureKeyStore(Target target, SSLContextBuilder sslContextBuilder) throws KeyStoreException, IOException, NoSuchAlgorithmException, CertificateException, UnrecoverableKeyException {
        Optional<String> keystore = target.keyStore();
        String keystorePassword = target.keyStorePassword().orElse("");
        String keyPassword = target.keyPassword().orElse(keystorePassword);
        if (keystore.isPresent()) {
            KeyStore store = KeyStore.getInstance(KeyStore.getDefaultType());
            store.load(Paths.get(keystore.get()).toUri().toURL().openStream(), keystorePassword.toCharArray());
            sslContextBuilder.loadKeyMaterial(store, keyPassword.toCharArray());
        }
    }

    static void configureTrustStore(Target target, SSLContextBuilder sslContextBuilder) throws KeyStoreException, IOException, NoSuchAlgorithmException, CertificateException {
        Optional<String> truststore = target.trustStore();
        String truststorePassword = target.trustStorePassword().orElse("");
        if (truststore.isPresent()) {
            KeyStore trustMaterial = KeyStore.getInstance(KeyStore.getDefaultType());
            trustMaterial.load(Paths.get(truststore.get()).toUri().toURL().openStream(), truststorePassword.toCharArray());
            sslContextBuilder.loadTrustMaterial(trustMaterial, new TrustSelfSignedStrategy());
        } else {
            sslContextBuilder.loadTrustMaterial(null, (chain, authType) -> true);
        }
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
