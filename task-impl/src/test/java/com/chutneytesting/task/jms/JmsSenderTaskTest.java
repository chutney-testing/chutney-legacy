package com.chutneytesting.task.jms;

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

import com.chutneytesting.task.spi.injectable.Logger;
import com.chutneytesting.task.spi.injectable.Target;
import com.mockrunner.jms.ConfigurationManager;
import com.mockrunner.jms.DestinationManager;
import com.mockrunner.mock.jms.MockConnectionFactory;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import javax.naming.Context;
import javax.naming.NamingException;
import javax.naming.spi.InitialContextFactory;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

public class JmsSenderTaskTest {

    @Test
    public void should_send_jms_message_to_destination() {

        String body = "builder";
        String destination = "testD";
        Map<String, String> headers = new HashMap<>();

        Target targetMock = mock(Target.class, RETURNS_DEEP_STUBS);
        configureServer(targetMock);
        Logger logger = mock(Logger.class);
        JmsSenderTask task = new JmsSenderTask(targetMock, logger, destination, body, headers);

        task.execute();

        // Then
        verify(logger, times(0)).error(any(String.class));
        ArgumentCaptor<String> info = ArgumentCaptor.forClass(String.class);
        verify(logger, times(1)).info(info.capture());

        assertThat(info.getValue()).contains("sent message").contains("testD");
    }

    private void configureServer(Target targetMock) {
        Map<String, String> props = new HashMap<>();
        props.put(Context.INITIAL_CONTEXT_FACTORY, MockRunnerInitialContextFactory.class.getName());
        when(targetMock.prefixedProperties(any())).thenReturn(props);
        when(targetMock.property("connectionFactoryName")).thenReturn(empty());
        when(targetMock.prefixedProperties(any(), anyBoolean())).thenReturn(emptyMap());
        when(targetMock.url()).thenReturn("unused URL");
        when(targetMock.user()).thenReturn(empty());
    }

    public static final class MockRunnerInitialContextFactory implements InitialContextFactory {
        private final Context context;

        public MockRunnerInitialContextFactory() throws NamingException {
            DestinationManager dm = new DestinationManager();
            this.context = mock(Context.class);
            when(context.lookup(any(String.class))).thenAnswer(iom -> dm.createQueue(iom.getArgument(0)));
            when(context.lookup(eq("ConnectionFactory"))).thenReturn(new MockConnectionFactory(dm, new ConfigurationManager()));
        }

        @Override
        public Context getInitialContext(Hashtable<?, ?> environment) {
            return context;
        }
    }
}
