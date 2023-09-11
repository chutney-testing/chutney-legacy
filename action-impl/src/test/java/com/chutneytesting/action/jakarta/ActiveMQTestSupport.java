
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

