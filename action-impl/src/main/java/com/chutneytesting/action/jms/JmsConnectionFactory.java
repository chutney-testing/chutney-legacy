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

import static com.chutneytesting.tools.ThrowingFunction.toUnchecked;
import static com.chutneytesting.tools.ThrowingSupplier.toUnchecked;

import com.chutneytesting.action.jms.consumer.Consumer;
import com.chutneytesting.action.jms.consumer.ConsumerFactory;
import com.chutneytesting.action.spi.injectable.Target;
import com.chutneytesting.tools.CloseableResource;
import com.chutneytesting.tools.UncheckedException;
import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.InvalidSelectorException;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageProducer;
import javax.jms.Session;
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

public class JmsConnectionFactory {

    private static final Logger LOGGER = LoggerFactory.getLogger(JmsConnectionFactory.class);

    public CloseableResource<Consumer> createConsumer(Target target, String destination, String timeout) {
        return createConsumer(target, destination, timeout, null, null, 0);
    }

    public CloseableResource<Consumer> createConsumer(Target target, String destination, String timeout, String bodySelector, String selector, int browserMaxDepth) {
        ConsumerFactory consumerFactory = new ConsumerFactory(bodySelector, selector, timeout, browserMaxDepth);
        return obtainCloseableResource(target, destination, consumerFactory::build);
    }

    public CloseableResource<MessageSender> getMessageProducer(Target target, String destinationName) throws UncheckedJmsException {
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

    private <T> CloseableResource<T> obtainCloseableResource(Target target, String destinationName, JmsThrowingBiFunction<Session, Destination, T> resourceBuilder) throws UncheckedJmsException {
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
            throw new UncheckedJmsException("Cannot parse selector " + e.getMessage(), e);
        } catch (NameNotFoundException e) {
            throw new UncheckedJmsException("Cannot find destination " + e.getMessage() + " on jms server " + target.name() + " (" + target.uri().toString() + ")", e);
        } catch (NamingException | JMSException e) {
            throw new UncheckedJmsException("Cannot connect to jms server " + target.name() + " (" + target.uri().toString() + "): " + e.getMessage(), e);
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
