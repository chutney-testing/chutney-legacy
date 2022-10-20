package com.chutneytesting.task.http;

import static com.chutneytesting.task.function.SoapFunction.soapInsertWSUsernameToken;
import static com.chutneytesting.task.http.HttpTaskHelper.httpCommonValidation;
import static com.chutneytesting.task.spi.time.Duration.parseToMs;
import static com.chutneytesting.task.spi.validation.Validator.getErrorsFrom;
import static java.util.Optional.ofNullable;

import com.chutneytesting.task.http.domain.HttpClient;
import com.chutneytesting.task.http.domain.HttpClientFactory;
import com.chutneytesting.task.http.domain.HttpTask;
import com.chutneytesting.task.spi.Task;
import com.chutneytesting.task.spi.TaskExecutionResult;
import com.chutneytesting.task.spi.injectable.Input;
import com.chutneytesting.task.spi.injectable.Logger;
import com.chutneytesting.task.spi.injectable.Target;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;

public class HttpSoapTask implements Task {

    private static final String DEFAULT_TIMEOUT = "2000 ms";

    private final Target target;
    private final Logger logger;
    private final String uri;
    private final String username;
    private final String password;
    private final Object body;
    private final String timeout;
    private final Map<String, String> headers;

    public HttpSoapTask(Target target, Logger logger,
                        @Input("uri") String uri,
                        @Input("body") String body,
                        @Input("username") String username,
                        @Input("password") String password,
                        @Input("timeout") String timeout,
                        @Input("headers") Map<String, String> headers) {
        this.target = target;
        this.logger = logger;
        this.uri = uri;
        this.body = body;
        this.username = username;
        this.password = password;
        this.timeout = ofNullable(timeout).orElse(DEFAULT_TIMEOUT);
        this.headers = headers != null ? headers : new HashMap<>();
    }

    @Override
    public List<String> validateInputs() {
        return getErrorsFrom(
            httpCommonValidation(target, timeout)
        );
    }

    @Override
    public TaskExecutionResult execute() {
        HttpClient httpClient = new HttpClientFactory().create(logger, target, String.class, (int) parseToMs(timeout));
        HttpHeaders httpHeaders = new HttpHeaders();
        headers.forEach(httpHeaders::add);
        Object envelope = soapInsertWSUsernameToken(this.username, this.password, body.toString());
        Supplier<ResponseEntity<String>> caller = () -> httpClient.post(this.uri, ofNullable(envelope).orElse("{}"), httpHeaders);
        return HttpTask.httpCall(logger, caller);
    }
}
