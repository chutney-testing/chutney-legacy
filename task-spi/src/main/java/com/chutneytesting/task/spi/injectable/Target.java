package com.chutneytesting.task.spi.injectable;

import static java.util.Optional.ofNullable;

import java.net.URI;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public interface Target {

    String name();

    String url();

    URI uri();

    @Deprecated
    Map<String, String> properties();

    default Optional<String> property(String key) {
        return ofNullable(properties().get(key));
    }

    default Map<String, String> prefixedProperties(String prefix) {
        return prefixedProperties(prefix, false);
    }

    default Map<String, String> prefixedProperties(String prefix, boolean cutPrefix) {
        return properties().entrySet().stream()
            .filter(e -> e.getKey() != null)
            .filter(e -> e.getKey().startsWith(prefix))
            .collect(Collectors.toMap(e -> e.getKey().substring(cutPrefix ? prefix.length() : 0), Map.Entry::getValue));
    }

    default Optional<Number> numericProperty(String key) {
        return property(key).map(k -> {
            try {
                return NumberFormat.getInstance().parse(k);
            } catch (ParseException e) {
                throw new RuntimeException(e);
            }
        });
    }

    default Optional<Boolean> booleanProperty(String key) {
        return property(key).map(Boolean::parseBoolean);
    }

    @Deprecated
    SecurityInfo security();

    default Optional<String> user() {
        return property("username")
            .or(() -> property("user"));
    }

    default Optional<String> userPassword() {
        return property("userPassword")
            .or(() -> property("password"));
    }

    default Optional<String> trustStore() {
        return property("trustStore");
    }

    default Optional<String> trustStorePassword() {
        return property("trustStorePassword");
    }

    default Optional<String> keyStore() {
        return property("keyStore");
    }

    default Optional<String> keyStorePassword() {
        return property("keyStorePassword");
    }

    default Optional<String> keyPassword() {
        return property("keyPassword");
    }

    default Optional<String> privateKey() {
        return property("privateKey");
    }

    default Optional<String> privateKeyPassword() {
        return property("privateKeyPassword")
            .or(() -> property("privateKeyPassphrase"));
    }

    default String host() {
        return uri().getHost();
    }

    default int port() {
        return uri().getPort();
    }

}
