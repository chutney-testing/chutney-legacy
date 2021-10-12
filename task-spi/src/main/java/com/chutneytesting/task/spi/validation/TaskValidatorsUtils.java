package com.chutneytesting.task.spi.validation;

import static com.chutneytesting.task.spi.validation.Validator.of;

import com.chutneytesting.task.spi.injectable.Target;
import com.chutneytesting.task.spi.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class TaskValidatorsUtils {

    public static Validator<Target> targetValidation(Target target) {
        return of(target)
            .validate(Objects::nonNull, "No target provided")
            .validate(Target::name, n -> !n.isBlank(), "Target name is blank")
            .validate(Target::url, u -> u != null && !u.isBlank(), "No url defined on the target")
            .validate(Target::getUrlAsURI, noException -> true, "Target url is not valid")
            .validate(Target::getUrlAsURI, uri -> uri.getHost() != null && !uri.getHost().isEmpty(), "Target url has an undefined host");
    }

    public static Validator<String> durationValidation(String duration, String inputLabel) {
        return of(duration)
            .validate(s -> s != null && !s.isBlank(), "No " + inputLabel + " provided")
            .validate(Duration::parseToMs, noException -> true, inputLabel + " is not parsable");
    }

    public static Validator<List> notEmptyListValidation(List toVerify, String inputLabel) {
        return of(toVerify)
            .validate(Objects::nonNull, "No " + inputLabel + " provided (List)")
            .validate(m -> !m.isEmpty(), inputLabel + " should not be empty");
    }

    public static Validator<Map> notEmptyMapValidation(Map toVerify, String inputLabel) {
        return of(toVerify)
            .validate(Objects::nonNull, "No " + inputLabel + " provided (Map)")
            .validate(m -> !m.isEmpty(), inputLabel + " should not be empty");
    }

    public static Validator<String> notBlankStringValidation(String toVerify, String inputLabel) {
        return of(toVerify)
            .validate(Objects::nonNull, "No " + inputLabel + " provided (String)")
            .validate(s -> s != null && !s.isBlank(), inputLabel + " should not be blank");
    }
}
