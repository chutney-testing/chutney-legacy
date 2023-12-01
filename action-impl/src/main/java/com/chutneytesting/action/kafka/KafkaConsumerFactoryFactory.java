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
import static java.util.Collections.unmodifiableMap;
import static org.apache.kafka.clients.admin.AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG;
import static org.apache.kafka.clients.consumer.ConsumerConfig.GROUP_ID_CONFIG;

import com.chutneytesting.action.spi.injectable.Target;
import java.util.HashMap;
import java.util.Map;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;

public class KafkaConsumerFactoryFactory {

    public ConsumerFactory<String, String> create(Target target, String group, Map<String, String> config) {

        Map<String, Object> consumerConfig = new HashMap<>();
        consumerConfig.put(BOOTSTRAP_SERVERS_CONFIG, resolveBootStrapServerConfig(target));
        consumerConfig.put(GROUP_ID_CONFIG, group);
        target.trustStore().ifPresent(trustStore -> {
          consumerConfig.put("ssl.truststore.location", trustStore);
          consumerConfig.put("ssl.truststore.password", target.trustStorePassword().orElseThrow(IllegalArgumentException::new));
        });

        consumerConfig.putAll(config);

        return new DefaultKafkaConsumerFactory<>(
            unmodifiableMap(consumerConfig),
            new StringDeserializer(),
            new StringDeserializer());
    }
}
