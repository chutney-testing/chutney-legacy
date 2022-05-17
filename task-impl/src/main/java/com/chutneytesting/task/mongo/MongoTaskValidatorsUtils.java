package com.chutneytesting.task.mongo;

import static com.chutneytesting.task.spi.validation.TaskValidatorsUtils.targetValidation;

import com.chutneytesting.task.spi.injectable.Target;
import com.chutneytesting.task.spi.validation.Validator;
import org.apache.commons.lang3.StringUtils;

public class MongoTaskValidatorsUtils {

    public static Validator<Target> mongoTargetValidation(Target target) {
        return targetValidation(target)
            .validate(t -> target.property("databaseName").orElse(null), StringUtils::isNotBlank, "Missing Target property 'databaseName'");
    }
}
