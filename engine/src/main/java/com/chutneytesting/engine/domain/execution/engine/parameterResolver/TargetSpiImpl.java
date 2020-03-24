package com.chutneytesting.engine.domain.execution.engine.parameterResolver;

import com.chutneytesting.task.spi.injectable.SecurityInfo;
import com.chutneytesting.task.spi.injectable.SecurityInfo.Credential;
import com.chutneytesting.task.spi.injectable.Target;
import java.util.Map;
import java.util.Optional;

public class TargetSpiImpl implements Target {
    private final com.chutneytesting.engine.domain.environment.Target delegate;
    private final SecurityInfo security;

    public TargetSpiImpl(com.chutneytesting.engine.domain.environment.Target delegate) {
        this.delegate = delegate;
        this.security = new SecurityInfoSpiImpl(delegate.security);
    }

    @Override
    public String name() {
        return delegate.name;
    }

    @Override
    public String url() {
        return delegate.url;
    }

    @Override
    public Map<String, String> properties() {
        return delegate.properties;
    }

    @Override
    public SecurityInfo security() {
        return security;
    }

    static class SecurityInfoSpiImpl implements SecurityInfo {

        private final com.chutneytesting.engine.domain.environment.SecurityInfo delegate;

        SecurityInfoSpiImpl(com.chutneytesting.engine.domain.environment.SecurityInfo delegate) {
            this.delegate = delegate;
        }

        @Override
        public Optional<Credential> credential() {
            return Optional.ofNullable(delegate.credential).map(CredentialSpiImpl::new);
        }

        @Override
        public Optional<String> trustStore() {
            return Optional.ofNullable(delegate.trustStore);
        }

        @Override
        public Optional<String> trustStorePassword() {
            return Optional.ofNullable(delegate.trustStorePassword);
        }

        @Override
        public Optional<String> keyStore() {
            return Optional.ofNullable(delegate.keyStore);
        }

        @Override
        public Optional<String> keyStorePassword() {
            return Optional.ofNullable(delegate.keyStorePassword);
        }

        @Override
        public Optional<String> privateKey() {
            return Optional.ofNullable(delegate.privateKey);
        }
    }

    static class CredentialSpiImpl implements Credential {

        private final com.chutneytesting.engine.domain.environment.SecurityInfo.Credential delegate;

        CredentialSpiImpl(com.chutneytesting.engine.domain.environment.SecurityInfo.Credential delegate) {
            this.delegate = delegate;
        }

        @Override
        public String username() {
            return delegate.username;
        }

        @Override
        public String password() {
            return delegate.password;
        }
    }
}
