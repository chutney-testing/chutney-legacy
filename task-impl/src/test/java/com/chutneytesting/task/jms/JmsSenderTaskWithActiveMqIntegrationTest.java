package com.chutneytesting.task.jms;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import com.chutneytesting.task.TestTarget;
import com.chutneytesting.task.spi.injectable.Logger;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;

public class JmsSenderTaskWithActiveMqIntegrationTest extends ActiveMQTestSupport {

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
        JmsSenderTask task = new JmsSenderTask(target, logger, destination, body, headers);

        task.execute();

        assertThat(needClientAuthConnector.getBrokerService().getTotalConnections()).isEqualTo(expectedTotalConnections.get());
    }
}
