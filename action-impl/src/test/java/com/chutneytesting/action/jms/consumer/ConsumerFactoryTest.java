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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import javax.jms.JMSException;
import javax.jms.Session;
import org.junit.jupiter.api.Test;

public class ConsumerFactoryTest {

    @Test
    public void building_consumer_without_body_selector_returns_a_simple_consumer() throws JMSException {
        ConsumerFactory consumerFactory = new ConsumerFactory(null, null, "1 sec", 0);

        Session session = mock(Session.class);
        Consumer consumer = consumerFactory.build(session, null);

        assertThat(consumer).isInstanceOf(SimpleMessageConsumer.class);
    }

    @Test
    public void building_consumer_with_body_selector_returns_a_selected_consumer() throws JMSException {
        ConsumerFactory consumerFactory = new ConsumerFactory("XPATH '/test'", null, "1 sec", 0);


        Session session = mock(Session.class);
        Consumer consumer = consumerFactory.build(session, null);

        assertThat(consumer).isInstanceOf(SelectedMessageConsumer.class);
    }
}
