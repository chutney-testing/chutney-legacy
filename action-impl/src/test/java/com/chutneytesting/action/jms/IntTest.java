/*package com.chutneytesting.action.jms;

import static com.chutneytesting.action.spi.ActionExecutionResult.Status.Success;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import com.chutneytesting.action.TestTarget;
import com.chutneytesting.action.jms.jmsListenerAction;
import com.chutneytesting.action.jms.jmsSenderAction;
import com.chutneytesting.action.spi.ActionExecutionResult;
import com.chutneytesting.action.spi.injectable.Logger;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

@Testcontainers
public class IntTest {

    static String activemqAddress;
    static Integer hostJmsPort;

    @Container
    static GenericContainer<?> activemqContainer = new GenericContainer<>(DockerImageName.parse("webcenter/activemq"))
        .withExposedPorts(61616);

    @BeforeAll
    public static void setUp() throws Exception {
        activemqAddress = activemqContainer.getHost();
        hostJmsPort = activemqContainer.getMappedPort(61616);

    }

    @Test
    public void withRealActivMq() {
        String body = "messageBody";
        String destination = "dynamicQueues/testD";
        Map<String, String> headers = new HashMap<>();

        TestTarget target = TestTarget.TestTargetBuilder.builder()
            .withTargetId("id")
            .withUrl("tcp://" + activemqAddress + ":" + hostJmsPort + "?" +
                "sslEnabled=false"
            )
            .withProperty("java.naming.factory.initial", "org.apache.activemq.artemis.jndi.ActiveMQInitialContextFactory")
            .build();

        Logger logger = mock(Logger.class);
        jmsSenderAction action = new jmsSenderAction(target, logger, destination, body, headers);

        ActionExecutionResult result = action.execute();

        assertThat(result.status).isEqualTo(Success);

        jmsListenerAction jmsListenerAction = new jmsListenerAction(target, logger, destination, "2 sec", null, null, null);
        result = jmsListenerAction.execute();
        assertThat(result.status).isEqualTo(Success);
        assertThat(result.outputs.get("textMessage")).isEqualTo("messageBody");
    }
}
*/
