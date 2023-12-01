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

package com.chutneytesting.action.jakarta;

import static com.chutneytesting.action.spi.ActionExecutionResult.Status.Success;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import com.chutneytesting.action.TestTarget;
import com.chutneytesting.action.spi.ActionExecutionResult;
import com.chutneytesting.action.spi.injectable.Logger;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;

public class JakartaSenderActionWithActiveMqIntegrationTest extends ActiveMQTestSupport {
    @Test
    public void failedSSL2WayAskWithOneWayProvided() {

        String body = "messageBody";
        String destination = "dynamicQueues/testD";
        Map<String, String> headers = new HashMap<>();

        TestTarget target = TestTarget.TestTargetBuilder.builder()
            .withTargetId("id")
            .withUrl("tcp://localhost:61617?" +
                "sslEnabled=true" +
                "&keyStorePath=" + keyStorePath +
                "&keyStorePassword=" + keyStorePassword +
                "&trustStorePath=" + trustStorePath +
                "&trustStorePassword" + trustStorePassword +
                "&verifyHost=false"
            )
            .withProperty("java.naming.factory.initial", "org.apache.activemq.artemis.jndi.ActiveMQInitialContextFactory")
            .build();

        Logger logger = mock(Logger.class);
        JakartaSenderAction action = new JakartaSenderAction(target, logger, destination, body, headers);

        ActionExecutionResult result = action.execute();

        assertThat(result.status).isEqualTo(Success);

        JakartaListenerAction jmsListenerAction = new JakartaListenerAction(target, logger, destination, "2 sec", null, null, null);
        result = jmsListenerAction.execute();
        assertThat(result.status).isEqualTo(Success);
        assertThat(result.outputs.get("textMessage")).isEqualTo("messageBody");
    }

}
