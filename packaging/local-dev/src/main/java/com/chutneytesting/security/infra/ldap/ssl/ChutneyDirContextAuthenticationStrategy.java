/*
 * Copyright 2017-2023 Enedis
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
