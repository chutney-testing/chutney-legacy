package com.chutneytesting.task.spi.validation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Fluent validator
 */
public final class Validator<T> {

    private final T toValidate;
    private final List<String> errors = new ArrayList<>();

    private Validator(T toValidate) {
        this.toValidate = toValidate;
    }

    /**
     * Builder
     */
    public static <T> Validator<T> of(T toValidate) {
        return new Validator<>(toValidate);
    }

    /**
     * Example : .validate(Person::getAge, a -> a >= 18, "should be eighteen or over");
     */
    public <U> Validator<T> validate(Function<? super T, ? extends U> projection, Predicate<? super U> validation, String message) {
        return validate(projection.andThen(validation::test)::apply, message);
    }

    /**
     * Example : .validate(Objects::nonNull, "should not be null")
     */
    public Validator<T> validate(final Predicate<? super T> validation, final String message) {
        try {
            if (!validation.test(toValidate)) {
                addError(message);
            }
        } catch (Exception e) {
            addError("[" + message + "] not applied because of exception " + e.getClass().getCanonicalName() + "(" + e.getMessage() + ")");
        }
        return this;
    }

    public void addError(String message) {
        errors.add(message);
    }

    public static List<String> getErrorsFrom(Validator... validators) {
        return (List<String>) Arrays.stream(validators).flatMap(l -> l.getErrors().stream()).collect(Collectors.toList());
    }

    public boolean isValid() {
        return errors.size() == 0;
    }

    public List<String> getErrors() {
        return errors;
    }
}
