package javax.ws.rs.core;

import java.net.URI;

/**
 * From https://ecosystem.atlassian.net/browse/JRJC-262
 */
public class UriBuilder {
    private jakarta.ws.rs.core.UriBuilder internalUriBuilder;

    protected UriBuilder() {
    }

    public static UriBuilder fromUri(URI uri) {
        UriBuilder instance = new UriBuilder();
        instance.internalUriBuilder = jakarta.ws.rs.core.UriBuilder.fromUri(uri);
        return instance;
    }

    public UriBuilder path(String path) {
        internalUriBuilder.path(path);
        return this;
    }

    public UriBuilder queryParam(String name, Object... values) {
        internalUriBuilder.queryParam(name, values);
        return this;
    }

    public URI build(Object... values) {
        return internalUriBuilder.build(values);
    }
}
