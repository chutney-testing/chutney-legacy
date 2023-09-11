package com.chutneytesting.action.jakarta;

import static com.chutneytesting.tools.ThrowingFunction.toUnchecked;
import static com.chutneytesting.tools.ThrowingSupplier.toUnchecked;

import com.chutneytesting.action.jakarta.consumer.Consumer;
import com.chutneytesting.action.jakarta.consumer.ConsumerFactory;
import com.chutneytesting.action.spi.injectable.Target;
import com.chutneytesting.tools.CloseableResource;
import com.chutneytesting.tools.UncheckedException;
import jakarta.jms.Connection;
import jakarta.jms.ConnectionFactory;
import jakarta.jms.Destination;
import jakarta.jms.InvalidSelectorException;
import jakarta.jms.JMSException;
import jakarta.jms.Message;
import jakarta.jms.MessageProducer;
import jakarta.jms.Session;
import java.util.Hashtable;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NameNotFoundException;
import javax.naming.NamingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JakartaConnectionFactory {

    private static final Logger LOGGER = LoggerFactory.getLogger(JakartaConnectionFactory.class);

    public CloseableResource<Consumer> createConsumer(Target target, String destination, String timeout) {
        return createConsumer(target, destination, timeout, null, null, 0);
    }

    public CloseableResource<Consumer> createConsumer(Target target, String destination, String timeout, String bodySelector, String selector, int browserMaxDepth) {
        ConsumerFactory consumerFactory = new ConsumerFactory(bodySelector, selector, timeout, browserMaxDepth);
        return obtainCloseableResource(target, destination, consumerFactory::build);
    }

    public CloseableResource<MessageSender> getMessageProducer(Target target, String destinationName) throws UncheckedJakartaException {
        return obtainCloseableResource(target, destinationName, (session, destination) -> {
            MessageProducer messageProducer = session.createProducer(destination);

            return (messageContent, headers) -> {
                Message message = session.createTextMessage(messageContent);
                for (Entry<String, String> headerEntry : headers.entrySet()) {
                    message.setStringProperty(headerEntry.getKey(), headerEntry.getValue());
                }
                messageProducer.send(message);
            };
        });
    }

    private <T> CloseableResource<T> obtainCloseableResource(Target target, String destinationName, JmsThrowingBiFunction<Session, Destination, T> resourceBuilder) throws UncheckedJakartaException {
        Hashtable<String, String> environmentProperties = new Hashtable<>();
        environmentProperties.put(Context.PROVIDER_URL, target.uri().toString());
        environmentProperties.putAll(target.prefixedProperties("java.naming."));
        environmentProperties.putAll(target.prefixedProperties("jndi.", true));

        configureSsl(target, environmentProperties);

        String connectionFactoryName = target.property("connectionFactoryName").orElse("ConnectionFactory");

        try {
            debugClassLoader();
            Context context = new InitialContext(environmentProperties);
            ConnectionFactory connectionFactory = (ConnectionFactory) context.lookup(connectionFactoryName);

            Connection connection = createConnection(connectionFactory, target);
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
            throw new UncheckedJakartaException("Cannot parse selector " + e.getMessage(), e);
        } catch (NameNotFoundException e) {
            throw new UncheckedJakartaException("Cannot find destination " + e.getMessage() + " on jms server " + target.name() + " (" + target.uri().toString() + ")", e);
        } catch (NamingException | JMSException e) {
            throw new UncheckedJakartaException("Cannot connect to jms server " + target.name() + " (" + target.uri().toString() + "): " + e.getMessage(), e);
        }
    }

    private void debugClassLoader() {
        ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
        LOGGER.debug("Thread.currentThread().getContextClassLoader(): " + contextClassLoader);
        ClassLoader systemClassLoader = ClassLoader.getSystemClassLoader();
        LOGGER.debug("ClassLoader.getSystemClassLoader(): " + systemClassLoader);
    }

    private Connection createConnection(ConnectionFactory connectionFactory, Target target) throws JMSException {
        try {
            return target.user()
                .map(toUnchecked(user -> connectionFactory.createConnection(user, target.userPassword().orElse(""))))
                .orElseGet(toUnchecked(connectionFactory::createConnection));
        } catch (UncheckedException e) {
            // Only JMSException can be unchecked in previous calls
            throw (JMSException) e.getCause();
        }
    }

    private void configureSsl(Target target, Map<String, String> environmentProperties) {
        putInMapIfPresent("connection.ConnectionFactory.keyStore", target.keyStore(), environmentProperties);
        putInMapIfPresent("connection.ConnectionFactory.keyStorePassword", target.keyStorePassword(), environmentProperties);
        putInMapIfPresent("connection.ConnectionFactory.keyStoreKeyPassword", target.keyPassword(), environmentProperties);
        putInMapIfPresent("connection.ConnectionFactory.trustStore", target.trustStore(), environmentProperties);
        putInMapIfPresent("connection.ConnectionFactory.trustStorePassword", target.trustStorePassword(), environmentProperties);
    }

    private void putInMapIfPresent(String key, Optional<String> optionalValue, Map<String, String> map) {
        optionalValue.ifPresent(s -> map.put(key, s));
    }

    private interface JmsThrowingBiFunction<T1, T2, R> {
        R apply(T1 t1, T2 t2) throws JMSException;
    }

    public interface MessageSender {
        void send(String message, Map<String, String> headers) throws JMSException;
    }
}
