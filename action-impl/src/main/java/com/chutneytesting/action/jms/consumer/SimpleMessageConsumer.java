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

package com.chutneytesting.action.jms.consumer;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import java.util.Optional;

class SimpleMessageConsumer implements Consumer {

    private final MessageConsumer messageConsumer;
    private final long timeout;

    SimpleMessageConsumer(MessageConsumer messageConsumer, long timeout) {
        this.messageConsumer = messageConsumer;
        this.timeout = timeout;
    }

    @Override
    public Optional<Message> getMessage() throws JMSException {
        return Optional.ofNullable(messageConsumer.receive(timeout));
    }
}
