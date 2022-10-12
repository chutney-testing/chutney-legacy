package com.chutneytesting.action.context;

import static com.chutneytesting.action.spi.ActionExecutionResult.ok;
import static java.util.Objects.requireNonNull;
import static java.util.Optional.ofNullable;

import com.chutneytesting.action.spi.FinallyAction;
import com.chutneytesting.action.spi.Action;
import com.chutneytesting.action.spi.ActionExecutionResult;
import com.chutneytesting.action.spi.injectable.FinallyActionRegistry;
import com.chutneytesting.action.spi.injectable.Input;
import com.chutneytesting.action.spi.injectable.Logger;
import com.chutneytesting.action.spi.injectable.Target;
import java.util.Map;

public class FinalAction implements Action {

    private final Logger logger;
    private final FinallyActionRegistry finallyActionRegistry;
    private final String type;
    private final String name;
    private final Target target;
    private final Map<String, Object> inputs;
    private Map<String, Object> validations;
    private final String strategyType;
    private final Map<String, Object> strategyProperties;

    public FinalAction(Logger logger,
                     FinallyActionRegistry finallyActionRegistry,
                     @Input("type") String type,
                     @Input("name") String name,
                     Target target,
                     @Input("inputs") Map<String, Object> inputs,
                     @Input("validations") Map<String, Object> validations,
                     @Input("strategy-type") String strategyType,
                     @Input("strategy-properties") Map<String, Object> strategyProperties
    ) {
        this.logger = logger;
        this.finallyActionRegistry = finallyActionRegistry;
        this.type = requireNonNull(type, "type is mandatory");
        this.name = requireNonNull(name, "name is mandatory");
        this.target = target;
        this.inputs = inputs;
        this.validations = validations;
        this.strategyType = strategyType;
        this.strategyProperties = strategyProperties;
    }

    @Override
    public ActionExecutionResult execute() {
        FinallyAction.Builder finallyActionBuilder = FinallyAction.Builder.forAction(type, name);

        ofNullable(target).ifPresent(finallyActionBuilder::withTarget);
        ofNullable(inputs).map(Map::entrySet).ifPresent(entries -> entries.forEach(e -> finallyActionBuilder.withInput(e.getKey(), e.getValue())));
        ofNullable(validations).map(Map::entrySet).ifPresent(entries -> entries.forEach(e -> finallyActionBuilder.withValidation(e.getKey(), e.getValue())));
        ofNullable(strategyType).ifPresent(st -> {
            finallyActionBuilder.withStrategyType(st);
            ofNullable(strategyProperties).ifPresent(finallyActionBuilder::withStrategyProperties);
        });

        finallyActionRegistry.registerFinallyAction(finallyActionBuilder.build());
        logger.info(name + " (" + type + ") as finally action registered");
        return ok();
    }
}
