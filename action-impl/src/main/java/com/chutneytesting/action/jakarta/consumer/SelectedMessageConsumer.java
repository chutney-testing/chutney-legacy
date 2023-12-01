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

import com.chutneytesting.action.jakarta.consumer.bodySelector.BodySelector;
import com.chutneytesting.tools.Streams;
import jakarta.jms.JMSException;
import jakarta.jms.Message;
import jakarta.jms.QueueBrowser;
import java.util.Enumeration;
import java.util.Optional;

class SelectedMessageConsumer implements Consumer {

    private final QueueBrowser browser;
    private final BodySelector bodySelector;
    private final int browserMaxDepth;

    SelectedMessageConsumer(QueueBrowser browser, BodySelector bodySelector, int browserMaxDepth) {
        this.browser = browser;
        this.bodySelector = bodySelector;
        this.browserMaxDepth = browserMaxDepth;
    }

    @SuppressWarnings("unchecked")
    public Optional<Message> getMessage() throws JMSException {
        Enumeration<Message> messageEnumeration = browser.getEnumeration();
        return Streams.toStream(messageEnumeration)
            .limit(browserMaxDepth)
            .filter(bodySelector::match)
            .findFirst();
    }
}
