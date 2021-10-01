package com.chutneytesting.task.http;

import static com.chutneytesting.task.spi.time.Duration.parseToMs;
import static com.chutneytesting.task.spi.validation.Validator.getErrorsFrom;
import static com.chutneytesting.task.spi.validation.Validator.of;

import com.chutneytesting.task.http.domain.HttpClient;
import com.chutneytesting.task.http.domain.HttpClientFactory;
import com.chutneytesting.task.http.domain.HttpTask;
import com.chutneytesting.task.spi.Task;
import com.chutneytesting.task.spi.TaskExecutionResult;
import com.chutneytesting.task.spi.injectable.Input;
import com.chutneytesting.task.spi.injectable.Logger;
import com.chutneytesting.task.spi.injectable.Target;
import com.chutneytesting.task.spi.time.Duration;
import com.chutneytesting.task.spi.validation.Validator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;

public class HttpGetTask implements Task {

    private static final String DEFAULT_TIMEOUT = "2000 ms";

    private final Target target;
    private final Logger logger;
    private final String uri;
    private final Map<String, String> headers;
    private final String timeout;

    public HttpGetTask(Target target, Logger logger, @Input("uri") String uri, @Input("headers") Map<String, String> headers, @Input("timeout") String timeout) {
        this.target = target;
        this.logger = logger;
        this.uri = uri;
        this.headers = headers != null ? headers : new HashMap<>();
        this.timeout = Optional.ofNullable(timeout).orElse(DEFAULT_TIMEOUT);
    }

    @Override
    public List<String> validateInputs() {
        Validator<Target> targetValidator = of(this.target)
            .validate(Objects::nonNull, "No target provided")
            .validate(Target::name, n -> !n.isBlank(), "No target provided")
            .validate(Target::url, u -> u != null && !u.isBlank(), "No url defined on the target")
            .validate(Target::getUrlAsURI, noException -> true, "Target url is not valid")
            .validate(Target::getUrlAsURI, uri -> uri.getHost() != null && !uri.getHost().isEmpty(), "Target url has an undefined host");
        Validator<String> timeoutValidator = of(this.timeout)
            .validate(StringUtils::isNotBlank, "Timeout should not be blank")
            .validate(Duration::parseToMs, noException -> true, "Duration is not parsable");
        return getErrorsFrom(targetValidator, timeoutValidator);
    }

    @Override
    public TaskExecutionResult execute() {
        HttpClient httpClient = new HttpClientFactory().create(target, String.class, parseToMs(timeout));
        HttpHeaders httpHeaders = new HttpHeaders();
        headers.forEach((key, value) -> httpHeaders.add(key, value));
        Supplier<ResponseEntity<String>> caller = () -> httpClient.get(this.uri, httpHeaders);
        return HttpTask.httpCall(logger, caller);
    }
}
