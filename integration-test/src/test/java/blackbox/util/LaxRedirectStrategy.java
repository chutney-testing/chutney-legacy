/*
 * ====================================================================
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 *
 */

package blackbox.util;

import java.net.URI;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.ProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.protocol.HttpContext;

/**
 * {@link org.apache.http.impl.client.DefaultRedirectStrategy#getRedirect(HttpRequest, HttpResponse, HttpContext)}
 * Does not redirect on 302 HTTP status (SC_MOVED_TEMPORARILY)
 *
 * So this override allows full redirect with copy on 302 HTTP status.
 *
 */
public class LaxRedirectStrategy extends org.apache.http.impl.client.LaxRedirectStrategy {

    public static final LaxRedirectStrategy INSTANCE = new LaxRedirectStrategy();

    @Override
    public HttpUriRequest getRedirect(final HttpRequest request,
                                      final HttpResponse response,
                                      final HttpContext context) throws ProtocolException {

        final URI uri = getLocationURI(request, response, context);
        final String method = request.getRequestLine().getMethod();
        if (method.equalsIgnoreCase(HttpHead.METHOD_NAME)) {
            return new HttpHead(uri);
        } else if (method.equalsIgnoreCase(HttpGet.METHOD_NAME)) {
            return new HttpGet(uri);
        } else {
            final int status = response.getStatusLine().getStatusCode();
            if (status == HttpStatus.SC_TEMPORARY_REDIRECT || status == HttpStatus.SC_MOVED_TEMPORARILY) {
                return RequestBuilder.copy(request).setUri(uri).build();
            } else {
                return new HttpGet(uri);
            }
        }
    }
}
