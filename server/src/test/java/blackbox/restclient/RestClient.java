package blackbox.restclient;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.support.BasicAuthenticationInterceptor;
import org.springframework.web.client.RestTemplate;

public class RestClient {
    private final RestTemplate restTemplate;
    private BasicAuthenticationInterceptor currentBasicAuth;

    public RestClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public RestRequestBuilder defaultRequest() {
        return defaultRequestAs("user", "user");
    }

    public RestRequestBuilder request() {
        return requestAs("user", "user");
    }

    public RestRequestBuilder defaultRequestAs(String user, String password) {
        return requestAs(user, password).addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_UTF8_VALUE);
    }

    public RestRequestBuilder requestAs(String user, String password) {
        if (currentBasicAuth != null) {
            restTemplate.getInterceptors().remove(currentBasicAuth);
        }
        currentBasicAuth = new BasicAuthenticationInterceptor(user, password);
        restTemplate.getInterceptors().add(currentBasicAuth);
        return new RestRequestBuilder(restTemplate);
    }
}
