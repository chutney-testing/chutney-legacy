/*
 * Copyright 2017-2023 Enedis
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.chutneytesting.action.radius;

import static com.chutneytesting.action.spi.validation.ActionValidatorsUtils.targetPropertiesNotBlankValidation;

import com.chutneytesting.action.spi.injectable.Target;
import com.chutneytesting.action.spi.validation.Validator;
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
                .validate(t -> target.numericProperty(property), port -> port.isPresent() && port.get().intValue() > 0, property + " is not a valid port number");
        }
        return validator;
    }

    public static RadiusClient createRadiusClient(Target target) {
        String hostname = target.host();
        String sharedSecret = target.property(SHARED_SECRET_TARGET_PROPERTY).orElse("");
        int authenticatePort = target.numericProperty(AUTH_PORT_TARGET_PROPERTY).orElse(0).intValue();
        int accountingPort = target.numericProperty(ACC_PORT_TARGET_PROPERTY).orElse(0).intValue();

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
