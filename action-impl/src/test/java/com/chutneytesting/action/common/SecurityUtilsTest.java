package com.chutneytesting.action.common;

import static com.chutneytesting.action.common.SecurityUtils.configureKeyStore;
import static org.assertj.core.api.Assertions.assertThat;

import com.chutneytesting.action.TestTarget;
import java.lang.reflect.Field;
import java.security.PrivateKey;
import java.util.Set;
import javax.net.ssl.KeyManager;
import javax.net.ssl.X509KeyManager;
import org.apache.http.ssl.SSLContextBuilder;
import org.junit.jupiter.api.Test;

class SecurityUtilsTest {

    @Test
    void should_load_private_key_with_password() throws Exception {
        // Given
        SSLContextBuilder context = new SSLContextBuilder();
        TestTarget target = TestTarget.TestTargetBuilder.builder()
            .withProperty("keyStore", "src/test/resources/security/keystore-with-keypwd.jks")
            .withProperty("keyStorePassword", "server")
            .withProperty("keyPassword", "key_pwd")
            .build();

        // When
        configureKeyStore(target, context);

        // Then
        PrivateKey actual = retrieveLoadedPrivateKey(context, "server");
        assertThat(actual).isNotNull();
    }

    @Test
    void should_use_keystorePassword_for_key_when_keyPassword_not_provided() throws Exception {
        // Given
        SSLContextBuilder context = new SSLContextBuilder();
        TestTarget target = TestTarget.TestTargetBuilder.builder()
            .withProperty("keyStore", "src/test/resources/security/server.jks")
            .withProperty("keyStorePassword", "server")
            .build();

        // When
        configureKeyStore(target, context);

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
