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

package com.chutneytesting.agent.domain.explore;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.catchThrowable;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

public class UrlSlicerTest {

    @ParameterizedTest
    @MethodSource("acceptedURLs")
    public void urlWrapper_build_with_valid_url(String url) {
        new UrlSlicer(url);
    }

    @Test()
    public void urlWrapper_build_with_invalid_url() {
        assertThatThrownBy(() -> new UrlSlicer("invalid url:12"))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @ParameterizedTest
    @MethodSource("acceptedURLs")
    public void urlWrapper_parse_host(String url) {
        String host = new UrlSlicer(url).host;
        assertThat(host).as("host").isEqualTo("somehost");
    }

    @ParameterizedTest
    @MethodSource("acceptedURLs")
    public void urlWrapper_parse_port(String url) {
        UrlSlicer slicedUrl = new UrlSlicer(url);
        assertThat(slicedUrl.host).as("host").isEqualTo("somehost");
        assertThat(slicedUrl.port).as("port").isEqualTo(12);
    }

    @ParameterizedTest
    @MethodSource("acceptedURLsWithoutPorts")
    public void should_use_default_port_protocol(String url, Integer defaultPort) {
        UrlSlicer slicedUrl = new UrlSlicer(url);
        assertThat(slicedUrl.host).as("host").isEqualTo("somehost");
        assertThat(slicedUrl.port).as("port").isEqualTo(defaultPort);
    }

    @Test
    public void should_throw_when_target_port_is_null() {
        Throwable thrown = catchThrowable(() -> {
            new UrlSlicer("fake://host-without-port/");
        });

        assertThat(thrown)
            .isInstanceOf(UndefinedPortException.class)
            .hasMessageContaining("Port is not defined on [fake://host-without-port/]. Cannot default port for [fake] protocol.");
    }

    private static String[] acceptedURLs() {
        return new String[]{
            "proto://somehost:12/",
            "proto://somehost:12/path",
            "jdbc:oracle:thin:@(DESCRIPTION=(ADDRESS=(PROTOCOL=TCP)(host=somehost)(PORT=12))"
        };
    }

    private static Object[] acceptedURLsWithoutPorts() {
        return new Object[]{
            new Object[]{"http://somehost/", 80},
            new Object[]{"https://somehost", 443},
            new Object[]{"ssh://somehost", 22},
            new Object[]{"amqp://somehost", 5672},
            new Object[]{"amqps://somehost", 5671},
            new Object[]{"ftp://somehost", 20},
        };
    }
}
