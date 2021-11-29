package com.chutneytesting.task.jms;

import static com.chutneytesting.tools.Entry.toEntryList;
import static com.chutneytesting.tools.ThrowingSupplier.toUnchecked;

import com.chutneytesting.task.jms.consumer.Consumer;
import com.chutneytesting.task.jms.consumer.ConsumerFactory;
import com.chutneytesting.task.jms.consumer.JmsListenerParameters;
import com.chutneytesting.task.spi.injectable.SecurityInfo;
import com.chutneytesting.task.spi.injectable.Target;
import com.chutneytesting.tools.CloseableResource;
import com.chutneytesting.tools.ThrowingFunction;
import com.chutneytesting.tools.UncheckedException;
import java.util.Hashtable;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.InvalidSelectorException;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NameNotFoundException;
import javax.naming.NamingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JmsConnectionFactory {

    private static final Logger LOGGER = LoggerFactory.getLogger(JmsConnectionFactory.class);

    public CloseableResource<Consumer> createConsumer(Target target, JmsListenerParameters arguments) {
        ConsumerFactory consumerFactory = new ConsumerFactory(arguments);
        return obtainCloseableResource(target, arguments.destination, consumerFactory::build);
    }

    public CloseableResource<MessageSender> getMessageProducer(Target target, String destinationName) throws UncheckedJmsException {
        return obtainCloseableResource(target, destinationName, (session, destination) -> {
            MessageProducer messageProducer = session.createProducer(destination);

            return (MessageSender) (messageContent, headers) -> {
                Message message = session.createTextMessage(messageContent);
                for (Entry<String, String> headerEntry : headers.entrySet()) {
                    message.setStringProperty(headerEntry.getKey(), headerEntry.getValue());
                }
                messageProducer.send(message);
            };
        });
    }

    private <T> CloseableResource<T> obtainCloseableResource(Target target, String destinationName, JmsThrowingBiFunction<Session, Destination, T> resourceBuilder) throws UncheckedJmsException {
        Hashtable<String, String> environmentProperties = new Hashtable<>();
        environmentProperties.put(Context.PROVIDER_URL, target.url());
        environmentProperties.putAll(target.properties());

        configureSsl(target, environmentProperties);

        String connectionFactoryName = target.properties().getOrDefault("connectionFactoryName", "ConnectionFactory");

        try {
            debugClassLoader();
            Context context = new InitialContext(environmentProperties);
            ConnectionFactory connectionFactory = (ConnectionFactory) context.lookup(connectionFactoryName);

            Connection connection = createConnection(connectionFactory, target.security().credential());
            connection.start();
            Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
            Destination destination = (Destination) context.lookup(destinationName);
            return CloseableResource.build(resourceBuilder.apply(session, destination), () -> {
                try {
                    session.close();
                } catch (JMSException e) {
                    LOGGER.warn("Unable to close JMS Session: " + e.getMessage());
                }
                try {
                    connection.close();
                } catch (JMSException e) {
                    LOGGER.warn("Unable to close JMS Connection: " + e.getMessage());
                }
            });
        } catch (InvalidSelectorException e) {
            throw new UncheckedJmsException("Cannot parse selector " + e.getMessage(), e);
        } catch (NameNotFoundException e) {
            throw new UncheckedJmsException("Cannot find destination " + e.getMessage() + " on jms server " + target.name() + " (" + target.url() + ")", e);
        } catch (NamingException | JMSException e) {
            throw new UncheckedJmsException("Cannot connect to jms server " + target.name() + " (" + target.url() + "): " + e.getMessage(), e);
        }
    }

    private void debugClassLoader() {
        ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
        LOGGER.debug("Thread.currentThread().getContextClassLoader(): " + contextClassLoader);
        ClassLoader systemClassLoader = ClassLoader.getSystemClassLoader();
        LOGGER.debug("ClassLoader.getSystemClassLoader(): " + systemClassLoader);
    }

    private Connection createConnection(ConnectionFactory connectionFactory, Optional<SecurityInfo.Credential> optionalCredential) throws JMSException {
        try {
            return optionalCredential
                .map(ThrowingFunction.toUnchecked(credential -> connectionFactory.createConnection(credential.username(), credential.password())))
                .orElseGet(toUnchecked(() -> connectionFactory.createConnection()));
        } catch (UncheckedException e) {
            // Only JMSException can be unchecked in previous calls
            throw (JMSException) e.getCause();
        }
    }

    private void configureSsl(Target target, Hashtable<String, String> environmentProperties) {
        target.security().keyStore().ifPresent(keyStore -> environmentProperties.put("connection.ConnectionFactory.keyStore", keyStore));
        target.security().keyStorePassword().ifPresent(keyStorePassword -> environmentProperties.put("connection.ConnectionFactory.keyStorePassword", keyStorePassword));

        target.security().keyPassword()
            .or(() -> toEntryList(target.properties()).stream()
                .filter(e -> e.key.equalsIgnoreCase("keyPassword"))
                .findFirst()
                .map(e -> e.value)
            ).ifPresent(keyStoreKeyPassword -> environmentProperties.put("connection.ConnectionFactory.keyStoreKeyPassword", keyStoreKeyPassword));

        target.security().trustStore().ifPresent(trustStore -> environmentProperties.put("connection.ConnectionFactory.trustStore", trustStore));
        target.security().trustStorePassword().ifPresent(trustStorePassword -> environmentProperties.put("connection.ConnectionFactory.trustStorePassword", trustStorePassword));
    }

    private interface JmsThrowingBiFunction<T1, T2, R> {
        R apply(T1 t1, T2 t2) throws JMSException;
    }

    public interface MessageSender {
        void send(String message, Map<String, String> headers) throws JMSException;
    }
}

