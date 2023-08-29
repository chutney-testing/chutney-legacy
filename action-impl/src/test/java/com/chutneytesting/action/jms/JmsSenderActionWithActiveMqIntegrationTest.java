package com.chutneytesting.action.jms;

import static com.chutneytesting.action.spi.ActionExecutionResult.Status.Success;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import com.chutneytesting.action.TestTarget;
import com.chutneytesting.action.jms.JmsListenerAction;
import com.chutneytesting.action.jms.JmsSenderAction;
import com.chutneytesting.action.spi.ActionExecutionResult;
import com.chutneytesting.action.spi.injectable.Logger;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;

public class JmsSenderActionWithActiveMqIntegrationTest extends ActiveMQTestSupport {
    @Test
    public void failedSSL2WayAskWithOneWayProvidedWithArtemisClient() throws Exception {

        String body = "messageBody";
        String destination = "dynamicQueues/testD";
        Map<String, String> headers = new HashMap<>();

        TestTarget target = TestTarget.TestTargetBuilder.builder()
            .withTargetId("id")
            .withUrl("tcp://localhost:61617")
            .withProperty("java.naming.factory.initial", "org.apache.activemq.jndi.ActiveMQSslInitialContextFactory")
            .withProperty("trustStore", "security/truststore.jks")
            .withProperty("trustStorePassword", "truststore")
            .build();

        Logger logger = mock(Logger.class);
        JmsSenderAction action = new JmsSenderAction(target, logger, destination, body, headers);

        ActionExecutionResult result = action.execute();

        assertThat(result.status).isEqualTo(Success);

        JmsListenerAction jmsListenerAction = new JmsListenerAction(target, logger, destination, "2 sec", null, null, null);
        result = jmsListenerAction.execute();
        assertThat(result.status).isEqualTo(Success);
        assertThat(result.outputs.get("textMessage")).isEqualTo("messageBody");
    }

}
