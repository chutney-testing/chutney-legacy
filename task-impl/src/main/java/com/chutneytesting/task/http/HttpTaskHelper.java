package com.chutneytesting.task.http;

import static com.chutneytesting.task.spi.validation.TaskValidatorsUtils.durationValidation;
import static com.chutneytesting.task.spi.validation.TaskValidatorsUtils.targetValidation;

import com.chutneytesting.task.spi.injectable.Target;
import com.chutneytesting.task.spi.validation.Validator;

public class HttpTaskHelper {

    private HttpTaskHelper() {

    }
    static Validator[] httpCommonValidation(Target target, String timeout) {
        return new Validator[]{targetValidation(target),
            durationValidation(timeout, "timeout")};
    }
}
