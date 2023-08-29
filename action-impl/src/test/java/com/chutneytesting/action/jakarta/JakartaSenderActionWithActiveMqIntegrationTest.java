package com.chutneytesting.action.jakarta;

import static com.chutneytesting.action.spi.ActionExecutionResult.Status.Success;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import com.chutneytesting.action.TestTarget;
import com.chutneytesting.action.spi.ActionExecutionResult;
import com.chutneytesting.action.spi.injectable.Logger;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;

public class JakartaSenderActionWithActiveMqIntegrationTest extends ActiveMQTestSupport {
    @Test
    public void failedSSL2WayAskWithOneWayProvidedWithArtemisClient() throws Exception {

        String body = "messageBody";
        String destination = "dynamicQueues/testD";
        Map<String, String> headers = new HashMap<>();

        TestTarget target = TestTarget.TestTargetBuilder.builder()
            .withTargetId("id")
            .withUrl("tcp://localhost:61617?" +
                "sslEnabled=true" +
                "&keyStorePath=" + keyStorePath +
                "&keyStorePassword=" + keyStorePassword +
                "&trustStorePath=" + trustStorePath +
                "&trustStorePassword" + trustStorePassword +
                "&verifyHost=false"
            )
            .withProperty("java.naming.factory.initial", "org.apache.activemq.artemis.jndi.ActiveMQInitialContextFactory")
            .build();

        Logger logger = mock(Logger.class);
        JakartaSenderAction action = new JakartaSenderAction(target, logger, destination, body, headers);

        ActionExecutionResult result = action.execute();

        assertThat(result.status).isEqualTo(Success);

        JakartaListenerAction jmsListenerAction = new JakartaListenerAction(target, logger, destination, "2 sec", null, null, null);
        result = jmsListenerAction.execute();
        assertThat(result.status).isEqualTo(Success);
        assertThat(result.outputs.get("textMessage")).isEqualTo("messageBody");
    }

}
