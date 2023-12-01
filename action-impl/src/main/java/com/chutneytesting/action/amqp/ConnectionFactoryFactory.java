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

package com.chutneytesting.action.amqp;

import static com.chutneytesting.action.common.SecurityUtils.buildSslContext;
import static java.util.function.Predicate.not;

import com.chutneytesting.action.spi.injectable.Target;
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
