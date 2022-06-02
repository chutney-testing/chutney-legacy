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
            if ("amqps".equalsIgnoreCase(target.uri().getScheme()) && (target.keyStore().isPresent() || target.trustStore().isPresent())) {
                SSLContext sslContext = buildSslContext(target).build();
                connectionFactory.useSslProtocol(sslContext);
            }

            connectionFactory.setUri(target.uri());

        } catch (URISyntaxException | GeneralSecurityException e) {
            throw new IllegalArgumentException(e.getMessage(), e);
        }

        target.user().ifPresent(connectionFactory::setUsername);
        target.userPassword().ifPresent(connectionFactory::setPassword);

        return connectionFactory;
    }
}
