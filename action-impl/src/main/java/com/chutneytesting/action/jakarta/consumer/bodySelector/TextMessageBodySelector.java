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

package com.chutneytesting.action.jakarta.consumer.bodySelector;

import jakarta.jms.JMSException;
import jakarta.jms.Message;
import jakarta.jms.TextMessage;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

abstract class TextMessageBodySelector implements BodySelector {
    private static final Logger LOGGER = LoggerFactory.getLogger(TextMessageBodySelector.class);

    @Override
    public boolean match(Message message) {
        final boolean matches;
        if (message instanceof TextMessage textMessage) {
            Optional<String> messageBody = textContent(textMessage);
            matches = messageBody.map(this::match).orElse(Boolean.FALSE);
        } else {
            matches = false;
        }
        return matches;
    }

    public abstract boolean match(String messageBody);

    private Optional<String> textContent(TextMessage message) {
        try {
            String messageBody = message.getText();
            return Optional.ofNullable(messageBody);
        } catch (JMSException e) {
            LOGGER.warn("Unable to read text from JMS TextMessage", e);
        }
        return Optional.empty();
    }
}
