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


package com.chutneytesting.action.jakarta;

import org.apache.activemq.artemis.core.config.impl.ConfigurationImpl;
import org.apache.activemq.artemis.core.server.ActiveMQServer;
import org.apache.activemq.artemis.core.server.ActiveMQServers;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;

public class ActiveMQTestSupport {

    private static ActiveMQServer server;

    static String keyStorePath = ActiveMQTestSupport.class.getResource("/security/server.jks").getPath().toString();
    static String keyStorePassword = "server";

    static String trustStorePath = ActiveMQTestSupport.class.getResource("/security/truststore.jks").getPath().toString();
    static String trustStorePassword = "truststore";

    @BeforeAll
    public static void setUp() throws Exception {


        server = ActiveMQServers.newActiveMQServer(new ConfigurationImpl()
            .setPersistenceEnabled(false)
            .setJournalDirectory("target/data/journal")
            .setSecurityEnabled(false)
            .addAcceptorConfiguration("ssl",
                "tcp://localhost:61617?" +
                    "sslEnabled=true" +
                    "&keyStorePath=" + keyStorePath +
                    "&keyStorePassword=" + keyStorePassword +
                    "&trustStorePath=" + trustStorePath +
                    "&trustStorePassword" + trustStorePassword +
                    "&needClientAuth=false"
                // "&wantClientAuth=true" certificate must either be marked as having both clientAuth and serverAuth extended key usage
            ));
        server.start();
    }

    @AfterAll
    public static void tearDown() throws Exception {
        server.stop();
    }
}

