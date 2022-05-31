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
                SSLContext sslContext = buildSslContext(target.properties(), target.security()).build();
                connectionFactory.useSslProtocol(sslContext);
            }

            if (target.properties().containsKey("sslProtocol")) {
                connectionFactory.useSslProtocol(target.properties().get("sslProtocol"));
            }

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
