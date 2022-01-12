package com.chutneytesting.task.radius;

import static com.chutneytesting.task.spi.validation.TaskValidatorsUtils.targetPropertiesNotBlankValidation;
import static java.lang.Integer.parseInt;

import com.chutneytesting.task.spi.injectable.Target;
import com.chutneytesting.task.spi.validation.Validator;
import java.util.List;
import org.tinyradius.attribute.RadiusAttribute;
import org.tinyradius.packet.AccessRequest;
import org.tinyradius.packet.RadiusPacket;
import org.tinyradius.util.RadiusClient;

public final class RadiusHelper {

    static final String SHARED_SECRET_TARGET_PROPERTY = "sharedSecret";
    static final String AUTH_PORT_TARGET_PROPERTY = "authenticatePort";
    static final String ACC_PORT_TARGET_PROPERTY = "accountingPort";

    private RadiusHelper() {
    }

    public static String getRadiusProtocol(String protocol) {
        return AccessRequest.AUTH_CHAP.equalsIgnoreCase(protocol) ? AccessRequest.AUTH_CHAP : AccessRequest.AUTH_PAP;
    }

    public static Validator<Target> radiusTargetPropertiesValidation(Target target) {
        return targetPropertiesNotBlankValidation(target, SHARED_SECRET_TARGET_PROPERTY, AUTH_PORT_TARGET_PROPERTY, ACC_PORT_TARGET_PROPERTY);
    }

    public static Validator<Target> radiusTargetPortPropertiesValidation(Target target) {
        Validator<Target> validator = Validator.of(target);
        for (String property : List.of(AUTH_PORT_TARGET_PROPERTY, ACC_PORT_TARGET_PROPERTY)) {
            validator
                .validate(t -> parseInt(t.properties().get(property)), port -> port > 0, property + " is not a valid port number");
        }
        return validator;
    }

    public static RadiusClient createRadiusClient(Target target) {
        String hostname = target.host();
        String sharedSecret = target.properties().get(SHARED_SECRET_TARGET_PROPERTY);
        int authenticatePort = parseInt(target.properties().get(AUTH_PORT_TARGET_PROPERTY));
        int accountingPort = parseInt(target.properties().get(ACC_PORT_TARGET_PROPERTY));

        RadiusClient client = new RadiusClient(hostname, sharedSecret);
        client.setAuthPort(authenticatePort);
        client.setAcctPort(accountingPort);
        return client;
    }

    public static String silentGetAttribute(RadiusPacket response, String attribute) {
        try {
            RadiusAttribute replyMessage = response.getAttribute(attribute);
            return replyMessage != null ? replyMessage.getAttributeValue() : "";
        } catch (IllegalArgumentException e) {
            // noop
        }
        return "";
    }
}
