package com.chutneytesting.action.jms;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import com.chutneytesting.action.TestTarget;
import com.chutneytesting.action.spi.injectable.Logger;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;

public class JmsSenderActionWithActiveMqIntegrationTest extends ActiveMQTestSupport {

    @Test
    public void failedSSL2WayAskWithOneWayProvided() throws Exception {

        String body = "messageBody";
        String destination = "dynamicQueues/testD";
        Map<String, String> headers = new HashMap<>();

        TestTarget target = TestTarget.TestTargetBuilder.builder()
            .withTargetId("id")
            .withUrl(needClientAuthConnector.getPublishableConnectString())
            .withProperty("java.naming.factory.initial", "org.apache.activemq.jndi.ActiveMQSslInitialContextFactory")
            .withProperty("trustStore", "security/truststore.jks")
            .withProperty("trustStorePassword", "truststore")
            .build();

        Logger logger = mock(Logger.class);
        JmsSenderAction action = new JmsSenderAction(target, logger, destination, body, headers);

        action.execute();

        assertThat(needClientAuthConnector.getBrokerService().getTotalConnections()).isEqualTo(expectedTotalConnections.get());
    }
}
