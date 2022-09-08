package com.chutneytesting.action.assertion;

import static com.chutneytesting.action.spi.validation.ActionValidatorsUtils.notEmptyListValidation;
import static com.chutneytesting.action.spi.validation.Validator.getErrorsFrom;
import static java.util.Collections.emptyList;
import static java.util.Optional.ofNullable;

import com.chutneytesting.action.spi.Action;
import com.chutneytesting.action.spi.ActionExecutionResult;
import com.chutneytesting.action.spi.injectable.Input;
import com.chutneytesting.action.spi.injectable.Logger;
import java.util.List;
import java.util.Map;

/**
 * Input are evaluated (SPeL) before entering the action
 */
public class AssertAction implements Action {

    private final Logger logger;
    private final List<Map<String, Boolean>> asserts;

    public AssertAction(Logger logger, @Input("asserts") List<Map<String, Boolean>> asserts) {
        this.logger = logger;
        this.asserts = ofNullable(asserts).orElse(emptyList());
    }

    @Override
    public List<String> validateInputs() {
        return getErrorsFrom(
            notEmptyListValidation(asserts, "asserts")
        );
    }

    @Override
    public ActionExecutionResult execute() {
        boolean result = asserts.stream().allMatch(l -> l.entrySet().stream()
            .map(e -> {
                if ("assert-true".equals(e.getKey())) {
                    if (e.getValue()) {
                        logger.info("assert ok");
                        return true;
                    } else {
                        return false;
                    }
                } else {
                    logger.error("Unknown assert type [" + e.getKey() + "]");
                    return Boolean.FALSE;
                }
            })
            .allMatch(r -> r)
        );
        return result ? ActionExecutionResult.ok() : ActionExecutionResult.ko();
    }
}
