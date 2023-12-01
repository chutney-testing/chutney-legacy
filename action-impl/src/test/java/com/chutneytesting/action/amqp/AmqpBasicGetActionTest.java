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

package com.chutneytesting.action.amqp;

import static com.chutneytesting.action.amqp.AmqpActionsTest.mockConnectionFactory;
import static org.assertj.core.api.Assertions.assertThat;

import com.chutneytesting.action.TestLogger;
import com.chutneytesting.action.TestTarget;
import com.chutneytesting.action.spi.Action;
import com.chutneytesting.action.spi.ActionExecutionResult;
import com.chutneytesting.action.spi.ActionExecutionResult.Status;
import com.chutneytesting.action.spi.injectable.Target;
import com.github.fridujo.rabbitmq.mock.MockConnectionFactory;
import org.junit.jupiter.api.Test;

public class AmqpBasicGetActionTest {

    @Test
    public void basicGet_fails_when_no_message_is_available() {
        MockConnectionFactory mockConnectionFactory = new MockConnectionFactory();
        TestLogger logger = new TestLogger();
        Target target = TestTarget.TestTargetBuilder.builder()
            .withTargetId("rabbit")
            .withUrl("amqp://non_host:1234")
            .withProperty("user", "guest")
            .withProperty("password", "guest")
            .build();

        String queueName = mockConnectionFactory.newConnection().createChannel().queueDeclare().getQueue();

        Action amqpBasicGetAction = mockConnectionFactory(new AmqpBasicGetAction(target, queueName, logger), mockConnectionFactory);

        ActionExecutionResult actionExecutionResult = amqpBasicGetAction.execute();

        assertThat(actionExecutionResult.status).isEqualTo(Status.Failure);
        assertThat(logger.errors).containsOnly("No message available");
    }
}
