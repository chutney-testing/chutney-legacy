package com.chutneytesting.action.radius;

import static com.chutneytesting.action.radius.RadiusHelper.createRadiusClient;
import static com.chutneytesting.action.radius.RadiusHelper.getRadiusProtocol;
import static com.chutneytesting.action.radius.RadiusHelper.radiusTargetPortPropertiesValidation;
import static com.chutneytesting.action.radius.RadiusHelper.radiusTargetPropertiesValidation;
import static com.chutneytesting.action.radius.RadiusHelper.silentGetAttribute;
import static com.chutneytesting.action.spi.validation.ActionValidatorsUtils.notBlankStringValidation;
import static com.chutneytesting.action.spi.validation.ActionValidatorsUtils.targetValidation;
import static com.chutneytesting.action.spi.validation.Validator.getErrorsFrom;
import static java.util.Collections.emptyMap;
import static java.util.Optional.ofNullable;

import com.chutneytesting.action.spi.Action;
import com.chutneytesting.action.spi.ActionExecutionResult;
import com.chutneytesting.action.spi.injectable.Input;
import com.chutneytesting.action.spi.injectable.Logger;
import com.chutneytesting.action.spi.injectable.Target;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.tinyradius.packet.AccessRequest;
import org.tinyradius.packet.RadiusPacket;
import org.tinyradius.util.RadiusClient;
import org.tinyradius.util.RadiusException;

/**
 * mschapv2 and eap not supported by the client we use.
 *
 * @See org.tinyradius.packet.AccessRequest#encodeRequestAttributes(java.lang.String)
 */
public class RadiusAuthenticateAction implements Action {

    private final Logger logger;
    private final Target target;
    private final String protocol;
    private final String userName;
    private final String userPassword;
    private final Map<String, String> attributes;

    public RadiusAuthenticateAction(Logger logger, Target target, @Input("userName") String userName, @Input("userPassword") String userPassword, @Input("protocol") String protocol, @Input("attributes") Map<String, String> attributes) {
        this.logger = logger;
        this.target = target;
        this.userName = userName;
        this.userPassword = userPassword;
        this.protocol = getRadiusProtocol(protocol);
        this.attributes = ofNullable(attributes).orElse(emptyMap());
    }

    @Override
    public List<String> validateInputs() {
        return getErrorsFrom(
            targetValidation(target),
            radiusTargetPropertiesValidation(target),
            radiusTargetPortPropertiesValidation(target),
            notBlankStringValidation(userName, "userName"),
            notBlankStringValidation(userPassword, "userPassword")
        );
    }

    @Override
    public ActionExecutionResult execute() {
        AccessRequest accessRequest = new AccessRequest(userName, userPassword);
        accessRequest.setAuthProtocol(protocol);
        attributes.forEach(accessRequest::addAttribute);

        RadiusClient client = createRadiusClient(target);

        try {
            RadiusPacket response = client.authenticate(accessRequest);
            if (response == null) {
                logger.error("Authenticate failed. Response is null");
                return ActionExecutionResult.ko();
            }

            Map<String, Object> outputs = new HashMap<>();
            outputs.put("radiusResponse", response);

            logger.info("Access request for [" + userName + "] response type : " + response.getPacketTypeName());
            String ip = silentGetAttribute(response, "Framed-IP-Address");
            if (!ip.isBlank()) {
                logger.info("Response ip address " + ip);
            }
            return ActionExecutionResult.ok(outputs);
        } catch (IOException | RadiusException e) {
            logger.error(e);
            return ActionExecutionResult.ko();
        }
    }
}
