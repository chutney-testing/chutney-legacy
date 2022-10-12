package com.chutneytesting.action.radius;

import static com.chutneytesting.action.radius.RadiusHelper.createRadiusClient;
import static com.chutneytesting.action.radius.RadiusHelper.radiusTargetPortPropertiesValidation;
import static com.chutneytesting.action.radius.RadiusHelper.radiusTargetPropertiesValidation;
import static com.chutneytesting.action.spi.validation.ActionValidatorsUtils.notBlankStringValidation;
import static com.chutneytesting.action.spi.validation.ActionValidatorsUtils.targetValidation;
import static com.chutneytesting.action.spi.validation.Validator.getErrorsFrom;
import static com.chutneytesting.action.spi.validation.Validator.of;
import static java.util.Collections.emptyMap;
import static java.util.Optional.ofNullable;

import com.chutneytesting.action.spi.Action;
import com.chutneytesting.action.spi.ActionExecutionResult;
import com.chutneytesting.action.spi.injectable.Input;
import com.chutneytesting.action.spi.injectable.Logger;
import com.chutneytesting.action.spi.injectable.Target;
import com.chutneytesting.action.spi.validation.Validator;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.tinyradius.packet.AccountingRequest;
import org.tinyradius.packet.RadiusPacket;
import org.tinyradius.util.RadiusClient;
import org.tinyradius.util.RadiusException;

public class RadiusAccountingAction implements Action {

    private final Logger logger;
    private final Target target;
    private final String userName;
    private final Map<String, String> attributes;
    private final Integer accountingType;

    public RadiusAccountingAction(Logger logger, Target target, @Input("userName") String userName, @Input("attributes") Map<String, String> attributes, @Input("accountingType") Integer accountingType) {
        this.logger = logger;
        this.target = target;
        this.userName = userName;
        this.accountingType = accountingType;
        this.attributes = ofNullable(attributes).orElse(emptyMap());
    }

    @Override
    public List<String> validateInputs() {
        return getErrorsFrom(
            targetValidation(target),
            radiusTargetPropertiesValidation(target),
            radiusTargetPortPropertiesValidation(target),
            notBlankStringValidation(userName, "userName"),
            accountingTypeValidation()
        );
    }

    @Override
    public ActionExecutionResult execute() {
        AccountingRequest accessRequest = new AccountingRequest(userName, accountingType);
        attributes.forEach(accessRequest::addAttribute);

        RadiusClient client = createRadiusClient(target);

        try {
            RadiusPacket response = client.account(accessRequest);
            if (response == null) {
                logger.error("Accounting failed. Response is null");
                return ActionExecutionResult.ko();
            }

            Map<String, Object> outputs = new HashMap<>();
            outputs.put("radiusResponse", response);

            logger.info("Accounting request as [" + userName + "] response type : " + response.getPacketTypeName());
            return ActionExecutionResult.ok(outputs);
        } catch (IOException | RadiusException e) {
            logger.error(e);
            return ActionExecutionResult.ko();
        }
    }

    private Validator<Integer> accountingTypeValidation() {
        return of(accountingType).validate(at -> (at >= 1 && at <= 15), "Invalid accountingType (by default start = 1, stop = 2, interim = 3, on = 7, off = 8)");
    }
}
