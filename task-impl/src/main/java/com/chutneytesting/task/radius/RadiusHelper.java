package com.chutneytesting.task.radius;

import static com.chutneytesting.task.spi.validation.TaskValidatorsUtils.targetPropertiesNotBlankValidation;

import com.chutneytesting.task.spi.injectable.Target;
import com.chutneytesting.task.spi.validation.Validator;
import org.tinyradius.attribute.RadiusAttribute;
import org.tinyradius.packet.AccessRequest;
import org.tinyradius.packet.RadiusPacket;
import org.tinyradius.util.RadiusClient;

public final class RadiusHelper {

    private RadiusHelper() {
    }

    public static String getRadiusProtocol(String protocol) {
        return AccessRequest.AUTH_CHAP.equalsIgnoreCase(protocol) ? AccessRequest.AUTH_CHAP : AccessRequest.AUTH_PAP;
    }

    public static Validator<Target> radiusTargetPropertiesValidation(Target target) {
        return targetPropertiesNotBlankValidation(target, "sharedSecret", "authenticatePort", "accountingPort");
    }

    public static RadiusClient createRadiusClient(Target target) {
        String hostname = target.getUrlAsURI().getHost();
        String sharedSecret = target.properties().get("sharedSecret");
        int authenticatePort = Integer.parseInt(target.properties().get("authenticatePort"));
        int accountingPort = Integer.parseInt(target.properties().get("accountingPort"));

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
