package com.chutneytesting.action.jakarta.consumer;

import static com.chutneytesting.action.spi.time.Duration.parseToMs;

import com.chutneytesting.action.jakarta.consumer.bodySelector.BodySelector;
import com.chutneytesting.action.jakarta.consumer.bodySelector.BodySelectorFactory;
import jakarta.jms.Destination;
import jakarta.jms.JMSException;
import jakarta.jms.MessageConsumer;
import jakarta.jms.Queue;
import jakarta.jms.QueueBrowser;
import jakarta.jms.Session;

public class ConsumerFactory {

    private final BodySelectorFactory bodySelectorFactory = new BodySelectorFactory();
    private final String bodySelector;
    private final String selector;
    private final String timeout;
    private final Integer browserMaxDepth;

    public ConsumerFactory(String bodySelector, String selector, String timeout, int browserMaxDepth) {
        this.bodySelector = bodySelector;
        this.selector = selector;
        this.timeout = timeout;
        this.browserMaxDepth = browserMaxDepth;
    }

    public Consumer build(Session session, Destination destination) throws JMSException {
        final Consumer consumer;
        if (bodySelector == null || bodySelector.isEmpty()) {
            MessageConsumer messageConsumer = session.createConsumer(destination, selector);
            consumer = new SimpleMessageConsumer(messageConsumer, (int) parseToMs(timeout));
        } else {
            QueueBrowser browser = session.createBrowser((Queue) destination, selector);
            BodySelector bodySelectorBuild = bodySelectorFactory.build(bodySelector);
            consumer = new SelectedMessageConsumer(browser, bodySelectorBuild, browserMaxDepth);
        }
        return consumer;
    }
}
