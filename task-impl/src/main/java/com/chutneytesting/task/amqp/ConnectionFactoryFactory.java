package com.chutneytesting.task.amqp;

import com.rabbitmq.client.ConnectionFactory;
import com.chutneytesting.task.spi.injectable.Target;
import java.net.URISyntaxException;
import java.security.GeneralSecurityException;

public class ConnectionFactoryFactory {

    public ConnectionFactory create(Target target) {
        ConnectionFactory connectionFactory = new ConnectionFactory();
        try {
            connectionFactory.setUri(target.getUrlAsURI());
        } catch (URISyntaxException | GeneralSecurityException e) {
            throw new IllegalArgumentException(e.getMessage(), e);
        }
        target.security().credential().ifPresent(cred -> {
                connectionFactory.setUsername(cred.username());
                connectionFactory.setPassword(cred.password());
            }
        );
        return connectionFactory;
    }
}
