package com.chutneytesting.task.assertion;

import static com.chutneytesting.task.spi.validation.TaskValidatorsUtils.notBlankStringValidation;
import static com.chutneytesting.task.spi.validation.TaskValidatorsUtils.notEmptyMapValidation;
import static com.chutneytesting.task.spi.validation.Validator.getErrorsFrom;

import com.chutneytesting.task.assertion.placeholder.PlaceholderAsserter;
import com.chutneytesting.task.assertion.placeholder.PlaceholderAsserterUtils;
import com.chutneytesting.task.assertion.utils.JsonUtils;
import com.chutneytesting.task.spi.Task;
import com.chutneytesting.task.spi.TaskExecutionResult;
import com.chutneytesting.task.spi.injectable.Input;
import com.chutneytesting.task.spi.injectable.Logger;
import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.InvalidJsonException;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.Option;
import com.jayway.jsonpath.ReadContext;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class JsonAssertTask implements Task {

    private final Logger logger;
    private final String document;
    private final Map<String, Object> mapExpectedResults;

    public JsonAssertTask(Logger logger,
                          @Input("document") String document,
                          @Input("expected") Map<String, Object> mapExpectedResults) {
        this.logger = logger;
        this.document = document;
        this.mapExpectedResults = mapExpectedResults;
    }

    @Override
    public List<String> validateInputs() {
        return getErrorsFrom(
            notBlankStringValidation(document, "document"),
            notEmptyMapValidation(mapExpectedResults, "expected")
        );
    }

    @Override
    public TaskExecutionResult execute() {
        try {
            String serializedDocument = JsonUtils.jsonStringify(document);
            ReadContext document = JsonPath.parse(serializedDocument, Configuration.defaultConfiguration().addOptions(Option.SUPPRESS_EXCEPTIONS));

            boolean matchesOk = mapExpectedResults.entrySet().stream().allMatch(entry -> {
                    String path = entry.getKey();
                    Object expected = entry.getValue();
                    Object actualValue = document.read(path);

                    boolean result;

                    Optional<PlaceholderAsserter> asserts = PlaceholderAsserterUtils.getAsserterMatching(expected);
                    if (asserts.isPresent()) {
                        result = asserts.get().assertValue(logger, actualValue, expected);
                    } else if (actualValue == null) {
                        logger.error("Path [" + path + "] not found");
                        result = false;
                    } else if (expected instanceof Number || actualValue instanceof Number) {
                        // hack hjson : org.hjson.JsonNumber.toString   => if (res.endsWith(".0")) return res.substring(0, res.length()-2);
                        // hack hjson : actualValue or expectedValue is a String and the other is a Number
                        result = new BigDecimal(expected.toString()).compareTo(new BigDecimal(actualValue.toString())) == 0;
                    } else {
                        result = expected.equals(actualValue);
                    }
                    if (!result) {
                        logger.error("On path [" + path + "], found [" + actualValue + "], expected was [" + expected + "]");
                    }
                    return result;
                }
            );

            if (!matchesOk) {
                return TaskExecutionResult.ko();
            }
            return TaskExecutionResult.ok();
        } catch (InvalidJsonException e) {
            logger.error("JSON parsing failed::: " + e.getMessage() + "\n" + e.getJson());
            return TaskExecutionResult.ko();
        }
    }
}
