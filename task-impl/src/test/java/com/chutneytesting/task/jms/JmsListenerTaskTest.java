package com.chutneytesting.task.jms;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.chutneytesting.task.spi.TaskExecutionResult;
import com.chutneytesting.task.spi.injectable.Logger;
import com.chutneytesting.task.spi.injectable.Target;
import com.chutneytesting.task.jms.consumer.JmsListenerParameters;
import java.util.Optional;
import javax.jms.TextMessage;
import org.junit.Test;
import org.springframework.test.util.ReflectionTestUtils;

public class JmsListenerTaskTest {

    @Test
    public void should_retrieve_jms_message_in_queue_and_put_it_in_the_context() throws Exception {

        Target target = mock(Target.class, RETURNS_DEEP_STUBS);
        Logger logger = mock(Logger.class);

        // Given 1 - jms task creation
        JmsListenerParameters listenerJmsParameters = new JmsListenerParameters("","","",0,"");
        JmsListenerTask task = new JmsListenerTask(target, logger, listenerJmsParameters);

        TextMessage textMessageMock = mock(TextMessage.class, RETURNS_DEEP_STUBS);
        String messageBody = "FAKE_JSON_PRODUCED_BY_SGE";
        when(textMessageMock.getText()).thenReturn(messageBody);

        JmsConnectionFactory jmsConnectionFactoryMock = mock(JmsConnectionFactory.class, RETURNS_DEEP_STUBS);
        when(jmsConnectionFactoryMock.createConsumer(any(), any()).getResource().getMessage()).thenReturn(Optional.of(textMessageMock));

        ReflectionTestUtils.setField(task, "jmsConnectionFactory", jmsConnectionFactoryMock);

        Target targetMock = mock(Target.class);
        when(targetMock.name()).thenReturn("SAR2");

        // When
        TaskExecutionResult executionResult = task.execute();

        // Then
        verify(logger, times(1)).info(anyString());

        assertThat(executionResult.outputs.get("textMessage")).as("Sent JMS message").isEqualTo(messageBody);
    }
}
