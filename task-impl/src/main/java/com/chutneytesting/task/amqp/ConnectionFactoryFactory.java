package com.chutneytesting.task.amqp;

import static com.chutneytesting.task.common.SecurityUtils.buildSslContext;
import static java.util.function.Predicate.not;

import com.chutneytesting.task.spi.injectable.Target;
import com.rabbitmq.client.Address;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import java.io.IOException;
import java.net.URISyntaxException;
import java.security.GeneralSecurityException;
import java.util.Optional;
import java.util.concurrent.TimeoutException;
import javax.net.ssl.SSLContext;

public class ConnectionFactoryFactory {

    private final ConnectionFactory connectionFactory;

    ConnectionFactoryFactory() {
        this.connectionFactory = new ConnectionFactory();
    }

    ConnectionFactoryFactory(ConnectionFactory connectionFactory) {
        this.connectionFactory = connectionFactory;
    }

    public Connection newConnection(Target target) throws IOException, TimeoutException {
        try {
            if ("amqps".equalsIgnoreCase(target.uri().getScheme())) {
                SSLContext sslContext = buildSslContext(target).build();
                connectionFactory.useSslProtocol(sslContext);
            }
        } catch (GeneralSecurityException e) {
            throw new IllegalArgumentException(e.getMessage(), e);
        }

        target.user().ifPresent(connectionFactory::setUsername);
        target.userPassword().ifPresent(connectionFactory::setPassword);

        Optional<String> singleHost = Optional.ofNullable(target.host());
        Optional<String> amqpAddresses = target.property("addresses");
        if (singleHost.isPresent() && amqpAddresses.isEmpty()) {
            try {
                connectionFactory.setUri(target.uri());
            } catch (URISyntaxException | GeneralSecurityException e) {
                throw new IllegalArgumentException(e.getMessage(), e);
            }
            return connectionFactory.newConnection();
        } else {
            Address[] adresses = Address.parseAddresses(amqpAddresses.filter(not(String::isBlank)).orElseGet(() -> target.uri().getAuthority()));
            return connectionFactory.newConnection(adresses);
        }
    }
}
