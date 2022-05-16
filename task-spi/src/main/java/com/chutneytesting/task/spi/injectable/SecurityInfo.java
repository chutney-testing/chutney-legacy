package com.chutneytesting.task.spi.injectable;

import java.util.Optional;

@Deprecated
public interface SecurityInfo {

    Optional<Credential> credential();
    Optional<String> trustStore();
    Optional<String> trustStorePassword();
    Optional<String> keyStore();
    Optional<String> keyStorePassword();
    Optional<String> keyPassword();
    Optional<String> privateKey();

    @Deprecated
    interface Credential {
        String username();
        String password();
    }
}
