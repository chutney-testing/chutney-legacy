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

import static java.util.Collections.emptyMap;
import static java.util.Optional.empty;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.chutneytesting.action.spi.injectable.Logger;
import com.chutneytesting.action.spi.injectable.Target;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import javax.jms.ConnectionFactory;
import javax.jms.Queue;
import javax.naming.Context;
import javax.naming.NamingException;
import javax.naming.spi.InitialContextFactory;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

public class JmsSenderActionTest {

    @Test
    public void should_send_jms_message_to_destination() {

        String body = "builder";
        String destination = "testD";
        Map<String, String> headers = new HashMap<>();

        Target targetMock = mock(Target.class, RETURNS_DEEP_STUBS);
        configureServer(targetMock);
        Logger logger = mock(Logger.class);
        JmsSenderAction action = new JmsSenderAction(targetMock, logger, destination, body, headers);

        action.execute();

        // Then
        verify(logger, times(0)).error(any(String.class));
        ArgumentCaptor<String> info = ArgumentCaptor.forClass(String.class);
        verify(logger, times(1)).info(info.capture());

        assertThat(info.getValue()).contains("sent message").contains("testD");
    }

    private void configureServer(Target targetMock) {
        Map<String, String> props = new HashMap<>();
        props.put(Context.INITIAL_CONTEXT_FACTORY, MockInitialContextFactory.class.getName());
        when(targetMock.prefixedProperties(any())).thenReturn(props);
        when(targetMock.property("connectionFactoryName")).thenReturn(empty());
        when(targetMock.prefixedProperties(any(), anyBoolean())).thenReturn(emptyMap());
        when(targetMock.user()).thenReturn(empty());
    }

    public static final class MockInitialContextFactory implements InitialContextFactory {
        private final Context context;

        public MockInitialContextFactory() throws NamingException {
            this.context = mock(Context.class);
            when(context.lookup(any(String.class))).thenAnswer(iom -> Mockito.mock(Queue.class, RETURNS_DEEP_STUBS));
            when(context.lookup(eq("ConnectionFactory"))).thenReturn(Mockito.mock(ConnectionFactory.class, RETURNS_DEEP_STUBS));
        }

        @Override
        public Context getInitialContext(Hashtable<?, ?> environment) {
            return context;
        }
    }
}
