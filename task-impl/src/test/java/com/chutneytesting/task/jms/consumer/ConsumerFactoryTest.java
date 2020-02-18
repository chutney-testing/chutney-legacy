package com.chutneytesting.task.jms.consumer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import javax.jms.JMSException;
import javax.jms.Session;
import org.junit.Test;

public class ConsumerFactoryTest {

    @Test
    public void building_consumer_without_body_selector_returns_a_simple_consumer() throws JMSException {
        JmsListenerParameters arguments = new JmsListenerParameters("","",null,0,"");
        ConsumerFactory consumerFactory = new ConsumerFactory(arguments);

        Session session = mock(Session.class);
        Consumer consumer = consumerFactory.build(session, null);

        assertThat(consumer).isInstanceOf(SimpleMessageConsumer.class);
    }

    @Test
    public void building_consumer_with_body_selector_returns_a_selected_consumer() throws JMSException {
        JmsListenerParameters arguments = new JmsListenerParameters("","","XPATH '/test'",0,"");
        ConsumerFactory consumerFactory = new ConsumerFactory(arguments);

        Session session = mock(Session.class);
        Consumer consumer = consumerFactory.build(session, null);

        assertThat(consumer).isInstanceOf(SelectedMessageConsumer.class);
    }
}
