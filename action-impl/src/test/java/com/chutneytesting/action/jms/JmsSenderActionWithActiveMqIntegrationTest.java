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

package com.chutneytesting.action.jms;

import static com.chutneytesting.action.spi.ActionExecutionResult.Status.Success;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import com.chutneytesting.action.TestTarget;
import com.chutneytesting.action.jms.JmsListenerAction;
import com.chutneytesting.action.jms.JmsSenderAction;
import com.chutneytesting.action.spi.ActionExecutionResult;
import com.chutneytesting.action.spi.injectable.Logger;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;

public class JmsSenderActionWithActiveMqIntegrationTest extends ActiveMQTestSupport {
    @Test
    public void failedSSL2WayAskWithOneWayProvided() throws Exception {

        String body = "messageBody";
        String destination = "dynamicQueues/testD";
        Map<String, String> headers = new HashMap<>();

        TestTarget target = TestTarget.TestTargetBuilder.builder()
            .withTargetId("id")
            .withUrl(needClientAuthConnector.getPublishableConnectString())
            .withProperty("java.naming.factory.initial", "org.apache.activemq.jndi.ActiveMQSslInitialContextFactory")
            .withProperty("trustStore", "security/truststore.jks")
            .withProperty("trustStorePassword", "truststore")
            .build();

        Logger logger = mock(Logger.class);
        JmsSenderAction action = new JmsSenderAction(target, logger, destination, body, headers);

        action.execute();

        assertThat(needClientAuthConnector.getBrokerService().getTotalConnections()).isEqualTo(expectedTotalConnections.get());
    }

}
