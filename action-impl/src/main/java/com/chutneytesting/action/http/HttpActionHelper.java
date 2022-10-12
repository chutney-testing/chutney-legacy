package com.chutneytesting.action.http;

import static com.chutneytesting.action.spi.validation.ActionValidatorsUtils.durationValidation;
import static com.chutneytesting.action.spi.validation.ActionValidatorsUtils.targetValidation;

import com.chutneytesting.action.spi.injectable.Target;
import com.chutneytesting.action.spi.validation.Validator;

public class HttpActionHelper {

    private HttpActionHelper() {
    }

    static Validator<?>[] httpCommonValidation(Target target, String timeout) {
        return new Validator[]{targetValidation(target),
            durationValidation(timeout, "timeout")};
    }
}
