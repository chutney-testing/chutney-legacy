package com.chutneytesting.task.radius;

import static com.chutneytesting.task.radius.RadiusHelper.createRadiusClient;
import static com.chutneytesting.task.radius.RadiusHelper.silentGetAttribute;
import static com.chutneytesting.task.radius.RadiusHelper.validateTargetInput;
import static java.util.Collections.emptyMap;
import static java.util.Optional.ofNullable;
import static org.apache.commons.lang3.Validate.notEmpty;
import static org.apache.commons.lang3.Validate.notNull;

import com.chutneytesting.task.spi.Task;
import com.chutneytesting.task.spi.TaskExecutionResult;
import com.chutneytesting.task.spi.injectable.Input;
import com.chutneytesting.task.spi.injectable.Logger;
import com.chutneytesting.task.spi.injectable.Target;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.lang3.Validate;
import org.tinyradius.packet.AccountingRequest;
import org.tinyradius.packet.RadiusPacket;
import org.tinyradius.util.RadiusClient;
import org.tinyradius.util.RadiusException;

public class RadiusAccountingTask implements Task {

    private final Logger logger;
    private final Target target;
    private final String userName;
    private final Map<String, String> attributes;
    private final Integer accountingType;

    public RadiusAccountingTask(Logger logger, Target target, @Input("userName") String userName, @Input("attributes") Map<String, String> attributes, @Input("accountingType") Integer accountingType) {
        validateTargetInput(target);
        notEmpty(userName, "Please set userName");
        notNull(accountingType, "Please set accountingType (by default start = 1, stop = 2, interim = 3, on = 7, off = 8)");

        this.logger = logger;
        this.target = target;
        this.userName = userName;
        this.accountingType = accountingType;

        this.attributes = ofNullable(attributes).orElse(emptyMap());
    }

    @Override
    public TaskExecutionResult execute() {
        AccountingRequest accessRequest = new AccountingRequest(userName, accountingType);
        attributes.entrySet().forEach(a -> accessRequest.addAttribute(a.getKey(), a.getValue()));

        RadiusClient client = createRadiusClient(target);

        try {
            RadiusPacket response = client.account(accessRequest);
            if (response == null) {
                logger.error("Accounting failed. Response is null");
                return TaskExecutionResult.ko();
            }

            Map<String, Object> outputs = new HashMap<>();
            outputs.put("radiusResponse", response);

            if (response.getPacketType() == RadiusPacket.ACCESS_REJECT) {
                logger.error("Accounting rejected. " + silentGetAttribute(response, "Reply-Message"));
                return TaskExecutionResult.ko(outputs);
            }
            logger.info("Accounting succeeded for [" + userName + "]");
            return TaskExecutionResult.ok(outputs);
        } catch (IOException | RadiusException e) {
            logger.error(e);
            return TaskExecutionResult.ko();
        }
    }
}
