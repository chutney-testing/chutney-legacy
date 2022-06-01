package com.chutneytesting.task.kafka;

import static java.util.Optional.of;
import static org.apache.kafka.clients.admin.AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG;

import com.chutneytesting.task.spi.injectable.Target;
import java.net.URI;

final class KafkaClientFactoryHelper {

    static String resolveBootStrapServerConfig(Target target) {
        return target.property(BOOTSTRAP_SERVERS_CONFIG)
            .or(() -> of(target.uri()).map(URI::getAuthority))
            .orElseGet(() -> target.uri().toString());
    }
}
