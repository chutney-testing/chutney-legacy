package com.chutneytesting.task.http;

import static com.chutneytesting.task.TaskValidatorsUtils.durationValidation;
import static com.chutneytesting.task.TaskValidatorsUtils.targetValidation;
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

public class HttpPostTask implements Task {

    private static final String DEFAULT_TIMEOUT = "2000 ms";

    private final Target target;
    private final Logger logger;
    private final String uri;
    private final Map<String, String> headers;
    private final Object body;
    private final String timeout;

    public HttpPostTask(Target target, Logger logger, @Input("uri") String uri, @Input("body") String body, @Input("headers") Map<String, String> headers, @Input("timeout") String timeout) {
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
            targetValidation(target),
            durationValidation(this.timeout, "timeout")
        );
    }

    @Override
    public TaskExecutionResult execute() {
        HttpClient httpClient = new HttpClientFactory().create(target, String.class, parseToMs(timeout));
        HttpHeaders httpHeaders = new HttpHeaders();
        headers.forEach((key, value) -> httpHeaders.add(key, value));
        Supplier<ResponseEntity<String>> caller = () -> httpClient.post(this.uri, ofNullable(body).orElse("{}"), httpHeaders);
        return HttpTask.httpCall(logger, caller);
    }
}
