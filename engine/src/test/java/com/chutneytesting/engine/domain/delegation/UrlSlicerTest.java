package com.chutneytesting.engine.domain.delegation;

import static org.assertj.core.api.Assertions.assertThat;

import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(JUnitParamsRunner.class)
public class UrlSlicerTest {

    @Test
    @Parameters(method = "acceptedURLs")
    public void urlWrapper_build_with_valid_url(String url) {
        new UrlSlicer(url);
    }

    @Test(expected = IllegalArgumentException.class)
    public void urlWrapper_build_with_invalid_url() throws Exception {
        new UrlSlicer("invalid url:12");
    }

    @Test
    @Parameters(method = "acceptedURLs")
    public void urlWrapper_parse_host(String url) {
        String host = new UrlSlicer(url).host;
        assertThat(host).as("host").isEqualTo("somehost");
    }

    @Test
    @Parameters(method = "acceptedURLs")
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
