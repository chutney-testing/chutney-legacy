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
