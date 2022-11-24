package com.chutneytesting.action.http;

import static com.chutneytesting.action.http.HttpActionHelper.httpCommonValidation;
import static com.chutneytesting.action.spi.time.Duration.parseToMs;
import static com.chutneytesting.action.spi.validation.Validator.getErrorsFrom;
import static java.util.Optional.ofNullable;

import com.chutneytesting.action.http.domain.HttpAction;
import com.chutneytesting.action.http.domain.HttpClient;
import com.chutneytesting.action.http.domain.HttpClientFactory;
import com.chutneytesting.action.spi.Action;
import com.chutneytesting.action.spi.ActionExecutionResult;
import com.chutneytesting.action.spi.injectable.Input;
import com.chutneytesting.action.spi.injectable.Logger;
import com.chutneytesting.action.spi.injectable.Target;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;

public class HttpPostAction implements Action {

    private static final String DEFAULT_TIMEOUT = "2000 ms";

    private final Target target;
    private final Logger logger;
    private final String uri;
    private final Map<String, String> headers;
    private final Object body;
    private final String timeout;

    public HttpPostAction(Target target, Logger logger, @Input("uri") String uri, @Input("body") String body, @Input("headers") Map<String, String> headers, @Input("timeout") String timeout) {
        this.target = target;
        this.logger = logger;
        this.uri = uri;
        this.body = body;
        this.headers = headers != null ? headers : new HashMap<>();
        this.timeout = ofNullable(timeout).orElse(DEFAULT_TIMEOUT);
    }

    @Override
    public List<String> validateInputs() {
        return getErrorsFrom(
            httpCommonValidation(target, timeout)
        );
    }

    @Override
    public ActionExecutionResult execute() {
        HttpClient httpClient = new HttpClientFactory().create(logger, target, String.class, (int) parseToMs(timeout));
        HttpHeaders httpHeaders = new HttpHeaders();
        headers.forEach((key, value) -> httpHeaders.add(key, value));
        Supplier<ResponseEntity<String>> caller = () -> httpClient.post(this.uri, ofNullable(body).orElse("{}"), httpHeaders);
        return HttpAction.httpCall(logger, caller);
    }
}
