package blackbox.restclient;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.support.BasicAuthenticationInterceptor;
import org.springframework.web.client.RestTemplate;

public class RestClient {
    private final RestTemplate restTemplate;

    public RestClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public RestRequestBuilder defaultRequest() {
        return request().addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_UTF8_VALUE);
    }

    public RestRequestBuilder request() {
        restTemplate.getInterceptors().add(new BasicAuthenticationInterceptor("user", "user"));
        return new RestRequestBuilder(restTemplate);
    }


}
