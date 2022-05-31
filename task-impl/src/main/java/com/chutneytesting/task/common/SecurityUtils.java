package com.chutneytesting.task.common;

import static com.chutneytesting.tools.Entry.toEntryList;

import com.chutneytesting.task.spi.injectable.SecurityInfo;
import java.io.IOException;
import java.nio.file.Paths;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.Map;
import java.util.Optional;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.ssl.SSLContextBuilder;

public class SecurityUtils {

    private SecurityUtils() {
    }

    public static SSLContextBuilder buildSslContext(Map<String, String> properties, SecurityInfo securityInfo) {
        try {
            SSLContextBuilder sslContextBuilder = new SSLContextBuilder();
            configureTrustStore(properties, securityInfo, sslContextBuilder);
            configureKeyStore(properties, securityInfo, sslContextBuilder);
            return sslContextBuilder;
        } catch (GeneralSecurityException | IOException e) {
            throw new IllegalArgumentException(e);
        }
    }

    public static void configureKeyStore(Map<String, String> properties, SecurityInfo securityInfo, SSLContextBuilder sslContextBuilder) throws KeyStoreException, IOException, NoSuchAlgorithmException, CertificateException, UnrecoverableKeyException {
        Optional<String> keystore = findProperty(securityInfo.keyStore(), properties, "keystore");
        String keystorePassword = findProperty(securityInfo.keyStorePassword(), properties, "keystorePassword").orElse("");
        String keyPassword = findProperty(securityInfo.keyPassword(), properties, "keyPassword").orElse("");
        if (keystore.isPresent()) {
            KeyStore store = KeyStore.getInstance(KeyStore.getDefaultType());
            store.load(Paths.get(keystore.get()).toUri().toURL().openStream(), keystorePassword.toCharArray());
            sslContextBuilder.loadKeyMaterial(store, keyPassword.toCharArray());
        }
    }

    public static void configureTrustStore(Map<String, String> properties, SecurityInfo securityInfo, SSLContextBuilder sslContextBuilder) throws KeyStoreException, IOException, NoSuchAlgorithmException, CertificateException {
        Optional<String> truststore = findProperty(securityInfo.trustStore(), properties, "truststore");
        String truststorePassword = findProperty(securityInfo.trustStorePassword(), properties, "truststorePassword").orElse("");
        if (truststore.isPresent()) {
            KeyStore trustMaterial = KeyStore.getInstance(KeyStore.getDefaultType());
            trustMaterial.load(Paths.get(truststore.get()).toUri().toURL().openStream(), truststorePassword.toCharArray());
            sslContextBuilder.loadTrustMaterial(trustMaterial, new TrustSelfSignedStrategy());
        } else {
            sslContextBuilder.loadTrustMaterial(null, (chain, authType) -> true);
        }
    }

    private static Optional<String> findProperty(Optional<String> securityValue, Map<String, String> properties, String key) {
        return securityValue.or(() ->
            toEntryList(properties).stream()
                .filter(e -> e.key.equalsIgnoreCase(key))
                .findFirst().map(e -> e.value)
        );
    }
}
