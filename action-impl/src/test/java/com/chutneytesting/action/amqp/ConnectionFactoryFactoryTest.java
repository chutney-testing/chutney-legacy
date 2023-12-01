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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import com.chutneytesting.action.TestTarget;
import com.chutneytesting.action.spi.injectable.Target;
import com.rabbitmq.client.Address;
import com.rabbitmq.client.ConnectionFactory;
import java.net.URI;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

class ConnectionFactoryFactoryTest {

    @Test
    void should_create_connection_from_target_uri() throws Exception {
        // Given
        ConnectionFactory connectionFactory = mock(ConnectionFactory.class);
        ConnectionFactoryFactory sut = new ConnectionFactoryFactory(connectionFactory);
        Target target = TestTarget.TestTargetBuilder.builder()
            .withTargetId("target with adresses property")
            .withUrl("scheme://host:888")
            .build();

        // When
        sut.newConnection(target);

        // Then
        ArgumentCaptor<URI> createConnectionAdresses = ArgumentCaptor.forClass(URI.class);
        verify(connectionFactory).setUri(createConnectionAdresses.capture());
        assertThat(createConnectionAdresses.getValue()).isEqualTo(target.uri());
        verify(connectionFactory).newConnection();
    }

    @Test
    void should_create_connection_from_target_addresses_property() throws Exception {
        // Given
        ConnectionFactory connectionFactory = mock(ConnectionFactory.class);
        ConnectionFactoryFactory sut = new ConnectionFactoryFactory(connectionFactory);
        Target target = TestTarget.TestTargetBuilder.builder()
            .withTargetId("target with adresses property")
            .withUrl("scheme://host:888")
            .withProperty("addresses", "localhost:666,localhost:999")
            .build();

        // When
        sut.newConnection(target);

        // Then
        verify(connectionFactory).newConnection(any(Address[].class));
    }

    @Test
    void should_create_connection_from_target_uri_when_target_addresses_property_is_empty() throws Exception {
        // Given
        ConnectionFactory connectionFactory = mock(ConnectionFactory.class);
        ConnectionFactoryFactory sut = new ConnectionFactoryFactory(connectionFactory);
        Target target = TestTarget.TestTargetBuilder.builder()
            .withTargetId("target with adresses property")
            .withUrl("scheme://host:888")
            .withProperty("addresses", "")
            .build();

        // When
        sut.newConnection(target);

        // Then
        ArgumentCaptor<Address[]> createConnectionAdresses = ArgumentCaptor.forClass(Address[].class);
        verify(connectionFactory).newConnection(createConnectionAdresses.capture());
        assertThat(createConnectionAdresses.getValue()).containsExactly(new Address("host", 888));
    }
}
