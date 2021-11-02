package com.chutneytesting.task.radius;

import static com.chutneytesting.task.radius.RadiusHelper.createRadiusClient;
import static com.chutneytesting.task.radius.RadiusHelper.getRadiusProtocol;
import static com.chutneytesting.task.radius.RadiusHelper.radiusTargetPortPropertiesValidation;
import static com.chutneytesting.task.radius.RadiusHelper.radiusTargetPropertiesValidation;
import static com.chutneytesting.task.radius.RadiusHelper.silentGetAttribute;
import static com.chutneytesting.task.spi.validation.TaskValidatorsUtils.notBlankStringValidation;
import static com.chutneytesting.task.spi.validation.TaskValidatorsUtils.targetValidation;
import static com.chutneytesting.task.spi.validation.Validator.getErrorsFrom;
import static java.util.Collections.emptyMap;
import static java.util.Optional.ofNullable;

import com.chutneytesting.task.spi.Task;
import com.chutneytesting.task.spi.TaskExecutionResult;
import com.chutneytesting.task.spi.injectable.Input;
import com.chutneytesting.task.spi.injectable.Logger;
import com.chutneytesting.task.spi.injectable.Target;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.tinyradius.packet.AccessRequest;
import org.tinyradius.packet.RadiusPacket;
import org.tinyradius.util.RadiusClient;
import org.tinyradius.util.RadiusException;

public class RadiusAuthenticateTask implements Task {

    private final Logger logger;
    private final Target target;
    private final String protocol;
    private final String userName;
    private final String userPassword;
    private final Map<String, String> attributes;

    public RadiusAuthenticateTask(Logger logger, Target target, @Input("userName") String userName, @Input("userPassword") String userPassword, @Input("protocol") String protocol, @Input("attributes") Map<String, String> attributes) {
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
    public TaskExecutionResult execute() {
        AccessRequest accessRequest = new AccessRequest(userName, userPassword);
        accessRequest.setAuthProtocol(protocol);
        attributes.forEach(accessRequest::addAttribute);

        RadiusClient client = createRadiusClient(target);

        try {
            RadiusPacket response = client.authenticate(accessRequest);
            if (response == null) {
                logger.error("Authenticate failed. Response is null");
                return TaskExecutionResult.ko();
            }

            Map<String, Object> outputs = new HashMap<>();
            outputs.put("radiusResponse", response);

            logger.info("Access request for [" + userName + "] response type : " + response.getPacketTypeName());
            String ip = silentGetAttribute(response, "Framed-IP-Address");
            if (!ip.isBlank()) {
                logger.info("Response ip address " + ip);
            }
            return TaskExecutionResult.ok(outputs);
        } catch (IOException | RadiusException e) {
            logger.error(e);
            return TaskExecutionResult.ko();
        }
    }
}
