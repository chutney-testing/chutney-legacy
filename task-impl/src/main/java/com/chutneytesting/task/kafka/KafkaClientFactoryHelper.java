package com.chutneytesting.task.kafka;

import static java.util.Optional.of;
import static java.util.Optional.ofNullable;
import static org.apache.kafka.clients.admin.AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG;

import com.chutneytesting.task.spi.injectable.Target;
import java.net.URI;

final class KafkaClientFactoryHelper {

    static String resolveBootStrapServerConfig(Target target) {
        return ofNullable(target.properties().get(BOOTSTRAP_SERVERS_CONFIG))
            .or(() -> of(target.getUrlAsURI()).map(URI::getAuthority))
            .orElseGet(target::url);
    }
}
