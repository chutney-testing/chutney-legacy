package com.chutneytesting.task.jms;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import com.chutneytesting.task.spi.injectable.Logger;
import com.chutneytesting.task.TestSecurityInfo;
import com.chutneytesting.task.TestTarget;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;

public class JmsSenderTaskWithActiveMqIntegrationTest extends ActiveMQTestSupport {

    @Test
    public void failedSSL2WayAskWithOneWayProvided() throws Exception {

        String body = "messageBody";
        String destination = "dynamicQueues/testD";
        Map<String, String> headers = new HashMap<>();


        TestSecurityInfo security = TestSecurityInfo.builder()
            .withTrustStore("security/truststore.jks")
            .withTrustStorePassword("truststore")
            .build();

        TestTarget target = TestTarget.TestTargetBuilder.builder()
            .withTargetId("id")
            .withUrl(needClientAuthConnector.getPublishableConnectString())
            .withSecurity(security)
            .withProperty("java.naming.factory.initial", "org.apache.activemq.jndi.ActiveMQSslInitialContextFactory")
            .build();

        Logger logger = mock(Logger.class);
        JmsSenderTask task = new JmsSenderTask(target, logger, destination, body, headers);

        task.execute();

        assertThat(needClientAuthConnector.getBrokerService().getTotalConnections()).isEqualTo(expectedTotalConnections.get());
    }


}
