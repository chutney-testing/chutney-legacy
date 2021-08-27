package com.chutneytesting.task.radius;

import static org.apache.commons.lang3.Validate.notEmpty;
import static org.apache.commons.lang3.Validate.notNull;

import com.chutneytesting.task.spi.injectable.Input;
import com.chutneytesting.task.spi.injectable.Target;
import org.tinyradius.attribute.RadiusAttribute;
import org.tinyradius.packet.AccessRequest;
import org.tinyradius.packet.RadiusPacket;
import org.tinyradius.util.RadiusClient;

public final class RadiusHelper {

    private RadiusHelper() {
    }

    public static String getRadiusProtocol(@Input("protocol") String protocol) {
        return AccessRequest.AUTH_CHAP.equalsIgnoreCase(protocol) ? AccessRequest.AUTH_CHAP : AccessRequest.AUTH_PAP;
    }

    public static void validateTargetInput(Target target) {
        notNull(target, "Please provide a target");
        notEmpty(target.url(), "Please set url on target");
        notNull(target.properties().get("sharedSecret"), "Please set sharedSecret properties on target");
        notNull(target.properties().get("authenticatePort"), "Please set authenticatePort properties on target");
        notNull(target.properties().get("accountingPort"), "Please set accountingPort properties on target");
    }

    public static RadiusClient createRadiusClient(Target target) {
        String hostname = target.getUrlAsURI().getHost();
        String sharedSecret = target.properties().get("sharedSecret");
        Integer authenticatePort = Integer.valueOf(target.properties().get("authenticatePort"));
        Integer accountingPort = Integer.valueOf(target.properties().get("accountingPort"));

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
        }
        return "";
    }
}
