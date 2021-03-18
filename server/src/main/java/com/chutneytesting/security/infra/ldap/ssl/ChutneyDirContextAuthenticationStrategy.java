package com.chutneytesting.security.infra.ldap.ssl;

import java.util.Hashtable;
import org.springframework.ldap.core.support.SimpleDirContextAuthenticationStrategy;

public class ChutneyDirContextAuthenticationStrategy extends SimpleDirContextAuthenticationStrategy {

    @Override
    public void setupEnvironment(Hashtable<String, Object> env, String userDn, String password) {
        env.put("java.naming.ldap.factory.socket", "com.chutneytesting.security.infra.ldap.ssl.CustomSSLSocketFactory");
        super.setupEnvironment(env, userDn, password);
    }
}
