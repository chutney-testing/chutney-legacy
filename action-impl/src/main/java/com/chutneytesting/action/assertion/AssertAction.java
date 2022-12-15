package com.chutneytesting.action.assertion;

import static com.chutneytesting.action.spi.validation.ActionValidatorsUtils.notEmptyListValidation;
import static com.chutneytesting.action.spi.validation.Validator.getErrorsFrom;
import static java.util.Collections.emptyList;
import static java.util.Optional.ofNullable;

import com.chutneytesting.action.spi.Action;
import com.chutneytesting.action.spi.ActionExecutionResult;
import com.chutneytesting.action.spi.injectable.Input;
import com.chutneytesting.action.spi.injectable.Logger;
import com.chutneytesting.action.spi.injectable.StepDefinitionSpi;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.IntStream;

/**
 * Input are evaluated (SPeL) before entering the action
 */
public class AssertAction implements Action {

    private final String ASSERTS_INPUT_LABEL = "asserts";
    private final Logger logger;
    private final List<Map<String, Boolean>> asserts;
    private final StepDefinitionSpi stepDefinition;

    public AssertAction(Logger logger, @Input(ASSERTS_INPUT_LABEL) List<Map<String, Boolean>> asserts, StepDefinitionSpi stepDefinition) {
        this.logger = logger;
        this.asserts = ofNullable(asserts).orElse(emptyList());
        this.stepDefinition = stepDefinition;
    }

    @Override
    public List<String> validateInputs() {
        return getErrorsFrom(
            notEmptyListValidation(asserts, ASSERTS_INPUT_LABEL)
        );
    }

    @Override
    public ActionExecutionResult execute() {
        List<Map<String, Object>> stepDefinitionInputs = (List<Map<String, Object>>) stepDefinition.inputs().get(ASSERTS_INPUT_LABEL);
        boolean allAssertionAreValid = IntStream
            .range(0, asserts.size())
            .mapToObj(index ->
                asserts.get(index)
                    .entrySet()
                    .stream()
                    .map(assertion -> checkAssertion(assertion, stepDefinitionInputs.get(index))))
            .flatMap(Function.identity())
            .allMatch(valid -> valid);
        return allAssertionAreValid ? ActionExecutionResult.ok() : ActionExecutionResult.ko();
    }

    private Boolean checkAssertion(Map.Entry<String, Boolean> assertion, Map<String, Object> stepDefinitionAssertions) {
        if ("assert-true".equals(assertion.getKey())) {
            String stepDefinitionAssertion = (String) stepDefinitionAssertions.get(assertion.getKey());
            if (assertion.getValue()) {
                logger.info(stepDefinitionAssertion + " is True");
                return true;
            } else {
                logger.error(stepDefinitionAssertion + " is False");
                return false;
            }
        } else {
            logger.error("Unknown assert type [" + assertion.getKey() + "]");
            return Boolean.FALSE;
        }
    }
}
