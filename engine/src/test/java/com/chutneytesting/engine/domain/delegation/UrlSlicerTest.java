package com.chutneytesting.engine.domain.delegation;

import static org.assertj.core.api.Assertions.assertThat;

import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(JUnitParamsRunner.class)
public class UrlSlicerTest {

    @Test
    @Parameters(method = "acceptedURLsWithPorts")
    public void should_build_with_valid_url(String url) {
        new UrlSlicer(url);
    }

    @Test(expected = IllegalArgumentException.class)
    public void should_build_with_invalid_url() {
        new UrlSlicer("invalid url:12");
    }

    @Test
    @Parameters(method = "acceptedURLsWithPorts")
    public void should_parse_host(String url) {
        String host = new UrlSlicer(url).host;
        assertThat(host).as("host").isEqualTo("somehost");
    }

    @Test
    @Parameters(method = "acceptedURLsWithPorts")
    public void should_parse_explicit_port(String url) {
        int port = new UrlSlicer(url).port;
        assertThat(port).as("port").isEqualTo(12);
    }

    @Test
    @Parameters(method = "acceptedURLsWithoutPorts")
    public void should_use_default_port_protocol(String url, Integer defaultPort) {
        int port = new UrlSlicer(url).port;
        assertThat(port).as("port").isEqualTo(defaultPort);
    }

    private static String[] acceptedURLsWithPorts() {
        return new String[]{
            "proto://somehost:12",
            "proto://somehost:12/path",
            "jdbc:oracle:thin:@(DESCRIPTION=(ADDRESS=(PROTOCOL=TCP)(host=somehost)(PORT=12))"
        };
    }

    private static Object[] acceptedURLsWithoutPorts() {
        return new Object[]{
            new Object[]{"http://somehost", 80},
            new Object[]{"https://somehost", 443},
            new Object[]{"ssh://somehost", 22},
            new Object[]{"amqp://somehost", 5672},
            new Object[]{"amqps://somehost", 5671}
        };
    }

}
