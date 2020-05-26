package com.chutneytesting.engine.domain.delegation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

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
        int port = new UrlSlicer(url).port;
        assertThat(port).as("port").isEqualTo(12);
    }

    private static String[] acceptedURLs() {
        return new String[]{
            "proto://somehost:12",
            "proto://somehost:12/path",
            "jdbc:oracle:thin:@(DESCRIPTION=(ADDRESS=(PROTOCOL=TCP)(host=somehost)(PORT=12))"
        };
    }

}
