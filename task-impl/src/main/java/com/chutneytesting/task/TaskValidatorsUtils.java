package com.chutneytesting.task;

import static com.chutneytesting.task.spi.validation.Validator.of;
import static java.lang.Double.parseDouble;
import static java.lang.Integer.parseInt;

import com.chutneytesting.task.spi.injectable.Target;
import com.chutneytesting.task.spi.time.Duration;
import com.chutneytesting.task.spi.validation.Validator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.apache.commons.lang3.StringUtils;

public class TaskValidatorsUtils {

    public static Validator<Target> targetValidation(Target target) {
        return of(target)
            .validate(Objects::nonNull, "No target provided")
            .validate(Target::name, n -> !n.isBlank(), "No target provided")
            .validate(Target::url, u -> u != null && !u.isBlank(), "No url defined on the target")
            .validate(Target::getUrlAsURI, noException -> true, "Target url is not valid")
            .validate(Target::getUrlAsURI, uri -> uri.getHost() != null && !uri.getHost().isEmpty(), "Target url has an undefined host");
    }

    public static Validator<String> durationValidation(String timeout, String label) {
        return of(timeout)
            .validate(StringUtils::isNotBlank, "No " + label + " provided")
            .validate(Duration::parseToMs, noException -> true, label + " is not parsable");
    }

    public static Validator<String> durationOrNullValidation(String timeout, String label) {
        return of(timeout)
            .validate(s -> s == null || Duration.parseToMs(s) >= Integer.MIN_VALUE , noException -> true, label + " is not parsable");
    }

    public static Validator<List> listValidation(List toVerify, String label) {
        return of(toVerify)
            .validate(Objects::nonNull, "No " + label + " provided (List)")
            .validate(m -> !m.isEmpty(), label + " should not be empty");
    }

    public static Validator<Map> mapValidation(Map toVerify, String label) {
        return of(toVerify)
            .validate(Objects::nonNull, "No " + label + " provided (Map)")
            .validate(m -> !m.isEmpty(), label + " should not be empty");
    }

    public static Validator<String> stringValidation(String toVerify, String label) {
        return of(toVerify)
            .validate(Objects::nonNull, "No " + label + " provided (String)")
            .validate(StringUtils::isNotBlank, label + " should not be blank");
    }

    public static Validator<String> integerOrNullValidation(String toVerify, String label) {
        return of(toVerify)
            .validate(s -> s == null || parseInt(s) >= Integer.MIN_VALUE, noException -> true, label + " parsing");
    }

    public static Validator<String> doubleOrNullValidation(String toVerify, String label) {
        return of(toVerify)
            .validate(s -> s == null || parseDouble(s) >= Double.MIN_VALUE, noException -> true, label + " parsing");
    }
}
