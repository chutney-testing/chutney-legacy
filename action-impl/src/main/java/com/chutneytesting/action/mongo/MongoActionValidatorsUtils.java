package com.chutneytesting.action.mongo;

import static com.chutneytesting.action.spi.validation.ActionValidatorsUtils.targetValidation;

import com.chutneytesting.action.spi.injectable.Target;
import com.chutneytesting.action.spi.validation.Validator;
import org.apache.commons.lang3.StringUtils;

public class MongoActionValidatorsUtils {

    public static Validator<Target> mongoTargetValidation(Target target) {
        return targetValidation(target)
            .validate(t -> target.property("databaseName").orElse(null), StringUtils::isNotBlank, "Missing Target property 'databaseName'");
    }
}
