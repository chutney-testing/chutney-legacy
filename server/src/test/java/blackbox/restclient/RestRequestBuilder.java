package blackbox.restclient;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

public class RestRequestBuilder {

    private final RestTemplate restTemplate;
    private final HttpHeaders headers = new HttpHeaders();
    private String url;
    private Object body;

    RestRequestBuilder(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public RestRequestBuilder withUrl(String url) {
        this.url = url;
        return this;
    }

    public RestRequestBuilder withBody(Object body) {
        this.body = body;
        return this;
    }

    public RestRequestBuilder addHeader(String key, String value) {
        headers.add(key, value);
        return this;
    }

    public ResponseEntity<String> post() {
        return post(String.class);
    }

    public <T> ResponseEntity<T> post(Class<T> responseClass) {
        return restTemplate.postForEntity(url, new HttpEntity<>(body, headers), responseClass);
    }

    public ResponseEntity<String> get(){
        return get(String.class);
    }

    public ResponseEntity<String> get(Object uriVariables){
        return get(String.class, uriVariables);
    }

    public <T> ResponseEntity<T> get(Class<T> responseType, Object uriVariables){
        return restTemplate.getForEntity(url, responseType, uriVariables);
    }

    public void delete() {
        restTemplate.delete(url);
    }

    public String patch() {
        return patch(String.class);
    }

    public <T> T patch(Class<T> responseClass) {
        return restTemplate.patchForObject(url, new HttpEntity<>(body, headers), responseClass);
    }
}
