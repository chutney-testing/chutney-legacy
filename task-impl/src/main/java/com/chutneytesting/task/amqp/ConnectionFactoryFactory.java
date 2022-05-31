package com.chutneytesting.task.amqp;

import static com.chutneytesting.task.common.SecurityUtils.buildSslContext;

import com.chutneytesting.task.spi.injectable.Target;
import com.rabbitmq.client.ConnectionFactory;
import java.net.URISyntaxException;
import java.security.GeneralSecurityException;
import javax.net.ssl.SSLContext;

public class ConnectionFactoryFactory {

    public ConnectionFactory create(Target target) {
        ConnectionFactory connectionFactory = new ConnectionFactory();
        try {
            connectionFactory.setUri(target.uri());

            if ("amqps".equalsIgnoreCase(target.uri().getScheme())) {
                SSLContext sslContext = buildSslContext(target).build();
                connectionFactory.useSslProtocol(sslContext);
            }

            target.property("sslProtocol")
                .ifPresent(protocol -> {
                    try {
                        connectionFactory.useSslProtocol(protocol);
                    } catch (GeneralSecurityException e) {
                        throw new IllegalArgumentException(e.getMessage(), e);
                    }
                });

        } catch (URISyntaxException | GeneralSecurityException e) {
            throw new IllegalArgumentException(e.getMessage(), e);
        }
        target.user().ifPresent(connectionFactory::setUsername);
        target.userPassword().ifPresent(connectionFactory::setPassword);
        return connectionFactory;
    }
}
