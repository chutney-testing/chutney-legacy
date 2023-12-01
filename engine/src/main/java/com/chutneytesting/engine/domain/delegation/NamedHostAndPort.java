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

package com.chutneytesting.engine.domain.delegation;

import java.util.Objects;
import java.util.Optional;

public class NamedHostAndPort {

    private final String name;
    private final String host;
    private final int port;

    public NamedHostAndPort(String name, String host, int port) {
        this.name = Optional.ofNullable(name).orElseThrow(() -> new IllegalArgumentException("Name should not be null"));
        this.host = Optional.ofNullable(host).orElseThrow(() -> new IllegalArgumentException("Host should not be null"));
        this.port = Optional.ofNullable(port).orElseThrow(() -> new IllegalArgumentException("Port should not be null"));
    }

    public String name() {
        return name;
    }

    public String host() {
        return host;
    }

    public int port() {
        return port;
    }

    @Override
    public String toString() {
        return "name='" + name + '\'' +
            ", host='" + host + '\'' +
            ", port=" + port;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NamedHostAndPort that = (NamedHostAndPort) o;
        return port == that.port &&
            Objects.equals(name, that.name) &&
            Objects.equals(host, that.host);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, host, port);
    }
}
