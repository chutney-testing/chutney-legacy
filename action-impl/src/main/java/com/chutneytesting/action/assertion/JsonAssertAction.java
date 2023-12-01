/*
 * Copyright 2017-2023 Enedis
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.chutneytesting.action.assertion;

import static com.chutneytesting.action.spi.validation.ActionValidatorsUtils.notBlankStringValidation;
import static com.chutneytesting.action.spi.validation.ActionValidatorsUtils.notEmptyMapValidation;
import static com.chutneytesting.action.spi.validation.Validator.getErrorsFrom;

import com.chutneytesting.action.assertion.placeholder.PlaceholderAsserter;
import com.chutneytesting.action.assertion.placeholder.PlaceholderAsserterUtils;
import com.chutneytesting.action.spi.Action;
import com.chutneytesting.action.spi.ActionExecutionResult;
import com.chutneytesting.action.spi.injectable.Input;
import com.chutneytesting.action.spi.injectable.Logger;
import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.InvalidJsonException;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.Option;
import com.jayway.jsonpath.ReadContext;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

public class JsonAssertAction implements Action {

    private final Logger logger;
    private final String document;
    private final Map<String, Object> mapExpectedResults;

    public JsonAssertAction(Logger logger,
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
    public ActionExecutionResult execute() {
        try {
            ReadContext json = JsonPath.parse(document, Configuration.defaultConfiguration().addOptions(Option.SUPPRESS_EXCEPTIONS));

            AtomicBoolean matchesOk = new AtomicBoolean(true);
            mapExpectedResults.entrySet().stream().forEach(entry -> {
                    String path = entry.getKey();
                    Object expected = entry.getValue();
                    Object actualValue = json.read(path);

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
                        if (!result) {
                            result = expected.toString().equals(actualValue.toString());
                            if (result) {
                                logger.info("Comparing object is false, but comparing toString() of this object is true");
                            }
                        }
                    }
                    if (result) {
                        logger.info("On path [" + path + "], found [" + actualValue + "]");
                    } else {
                        logger.error("On path [" + path + "], found [" + actualValue + "], expected was [" + expected + "]");
                        matchesOk.set(false);
                    }
                }
            );

            if (!matchesOk.get()) {
                return ActionExecutionResult.ko();
            }
            return ActionExecutionResult.ok();
        } catch (InvalidJsonException e) {
            logger.error("JSON parsing failed::: " + e.getMessage() + "\n" + e.getJson());
            return ActionExecutionResult.ko();
        }
    }
}
