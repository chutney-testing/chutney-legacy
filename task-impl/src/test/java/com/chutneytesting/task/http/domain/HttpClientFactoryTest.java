package com.chutneytesting.task.http.domain;

import static java.util.Collections.emptyMap;
import static org.assertj.core.api.Assertions.assertThat;

import com.chutneytesting.task.TestSecurityInfo;
import com.chutneytesting.task.spi.injectable.SecurityInfo;
import java.lang.reflect.Field;
import java.security.PrivateKey;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import javax.net.ssl.KeyManager;
import javax.net.ssl.X509KeyManager;
import org.apache.http.ssl.SSLContextBuilder;
import org.junit.jupiter.api.Test;

class HttpClientFactoryTest {

    @Test
    void should_load_private_key_with_password() throws Exception {
        // Given
        SSLContextBuilder context = new SSLContextBuilder();
        SecurityInfo security = TestSecurityInfo.builder()
            .withKeyStore("src/test/resources/security/keystore-with-keypwd.jks")
            .withKeyStorePassword("server")
            .withKeyPassword("key_pwd")
            .build();

        // When
        HttpClientFactory.configureKeyStore(emptyMap(), security, context);

        // Then
        PrivateKey actual = retrieveLoadedPrivateKey(context, "server");
        assertThat(actual).isNotNull();
    }

    @Test
    void should_load_private_key_from_properties() throws Exception {
        // Given
        SSLContextBuilder context = new SSLContextBuilder();
        Map<String, String> properties = new HashMap<>(3);
        properties.put("keyStore", "src/test/resources/security/keystore-with-keypwd.jks");
        properties.put("keyStorePassWord", "server");
        properties.put("keyPassword", "key_pwd");

        // When
        HttpClientFactory.configureKeyStore(properties, TestSecurityInfo.builder().build(), context);

        // Then
        PrivateKey actual = retrieveLoadedPrivateKey(context, "server");
        assertThat(actual).isNotNull();
    }

    @Test
    void should_use_keystorePassword_for_key_when_keyPassword_not_provided() throws Exception {
        // Given
        SSLContextBuilder context = new SSLContextBuilder();
        TestSecurityInfo securityInfo = TestSecurityInfo.builder()
            .withKeyStore("src/test/resources/security/server.jks")
            .withKeyStorePassword("server")
            .build();

        // When
        HttpClientFactory.configureKeyStore(emptyMap(), securityInfo, context);

        // Then
        PrivateKey actual = retrieveLoadedPrivateKey(context, "server");
        assertThat(actual).isNotNull();
    }

    private PrivateKey retrieveLoadedPrivateKey(SSLContextBuilder context, String pkName) throws Exception {
        Field privateField = SSLContextBuilder.class.getDeclaredField("keyManagers");
        privateField.setAccessible(true);
        Set<KeyManager> set = (Set<KeyManager>) privateField.get(context);
        KeyManager km = set.iterator().next();
        return ((X509KeyManager) km).getPrivateKey(pkName);
    }
}
