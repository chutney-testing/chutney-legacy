package com.chutneytesting.task.http.domain;

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
}
