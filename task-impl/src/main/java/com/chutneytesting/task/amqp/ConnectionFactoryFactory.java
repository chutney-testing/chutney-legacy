package com.chutneytesting.task.amqp;

import com.chutneytesting.task.spi.injectable.Target;
import com.rabbitmq.client.ConnectionFactory;
import java.net.URISyntaxException;
import java.security.GeneralSecurityException;

public class ConnectionFactoryFactory {

    public ConnectionFactory create(Target target) {
        ConnectionFactory connectionFactory = new ConnectionFactory();
        try {
            connectionFactory.setUri(target.uri());
        } catch (URISyntaxException | GeneralSecurityException e) {
            throw new IllegalArgumentException(e.getMessage(), e);
        }
        target.user().ifPresent(connectionFactory::setUsername);
        target.userPassword().ifPresent(connectionFactory::setPassword);
        return connectionFactory;
    }
}
