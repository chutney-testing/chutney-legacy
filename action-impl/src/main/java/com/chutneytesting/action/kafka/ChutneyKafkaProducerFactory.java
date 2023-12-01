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

package com.chutneytesting.action.kafka;

import static com.chutneytesting.action.kafka.KafkaClientFactoryHelper.resolveBootStrapServerConfig;
import static org.apache.kafka.clients.producer.ProducerConfig.BOOTSTRAP_SERVERS_CONFIG;

import com.chutneytesting.action.spi.injectable.Target;
import java.util.HashMap;
import java.util.Map;
import org.apache.kafka.common.config.SslConfigs;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;

final class ChutneyKafkaProducerFactory {

    private DefaultKafkaProducerFactory<String, String> factory;

    KafkaTemplate<String, String> create(Target target, Map<String, String> config) {

        Map<String, Object> producerConfig = new HashMap<>();
        producerConfig.put(BOOTSTRAP_SERVERS_CONFIG, resolveBootStrapServerConfig(target));
        producerConfig.putAll(config);
        target.trustStore().ifPresent(trustStore -> {
          producerConfig.put(SslConfigs.SSL_TRUSTSTORE_LOCATION_CONFIG, trustStore);
          producerConfig.put(SslConfigs.SSL_TRUSTSTORE_PASSWORD_CONFIG, target.trustStorePassword().orElseThrow(IllegalArgumentException::new));
        });

        this.factory = new DefaultKafkaProducerFactory<>(
            producerConfig,
            new StringSerializer(),
            new StringSerializer());

        return new KafkaTemplate<>(this.factory, true);
    }

    void destroy() {
        factory.destroy();
    }
}
