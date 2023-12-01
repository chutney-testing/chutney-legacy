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

package com.chutneytesting.action.http.domain;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;

public interface HttpClient {

    ResponseEntity<String> call(HttpMethod httpMethod, String resource, HttpEntity<?> input) throws HttpClientErrorException;

    default ResponseEntity<String> post(String resource, Object body, MultiValueMap<String, String> headers) throws HttpClientErrorException {
        return call(HttpMethod.POST, resource, new HttpEntity<>(body, headers));
    }

    default ResponseEntity<String> put(String resource, Object body, MultiValueMap<String, String> headers) throws HttpClientErrorException {
        return call(HttpMethod.PUT, resource, new HttpEntity<>(body, headers));
    }

    default ResponseEntity<String> get(String resource, MultiValueMap<String, String> headers) throws HttpClientErrorException {
        return call(HttpMethod.GET, resource, new HttpEntity<>(null, headers));
    }

    default ResponseEntity<String> delete(String resource, MultiValueMap<String, String> headers) throws HttpClientErrorException {
        return call(HttpMethod.DELETE, resource, new HttpEntity<>(null, headers));
    }

    default ResponseEntity<String> patch(String resource, Object body, MultiValueMap<String, String> headers) throws HttpClientErrorException {
        return call(HttpMethod.PATCH, resource, new HttpEntity<>(body, headers));
    }
}
