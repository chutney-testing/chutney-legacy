/*
 * Copyright 2017-2023 Enedis
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
