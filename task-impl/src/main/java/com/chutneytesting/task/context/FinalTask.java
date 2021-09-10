package com.chutneytesting.task.context;

import static com.chutneytesting.task.spi.TaskExecutionResult.ok;
import static java.util.Objects.requireNonNull;
import static java.util.Optional.ofNullable;

import com.chutneytesting.task.spi.FinallyAction;
import com.chutneytesting.task.spi.Task;
import com.chutneytesting.task.spi.TaskExecutionResult;
import com.chutneytesting.task.spi.injectable.FinallyActionRegistry;
import com.chutneytesting.task.spi.injectable.Input;
import com.chutneytesting.task.spi.injectable.Logger;
import com.chutneytesting.task.spi.injectable.Target;
import java.util.Map;

public class FinalTask implements Task {

    private final Logger logger;
    private final FinallyActionRegistry finallyActionRegistry;
    private final String taskIdentifier;
    private final String whatFor;
    private final Target target;
    private final Map<String, Object> inputs;
    private final String strategyType;
    private final Map<String, Object> strategyProperties;

    public FinalTask(Logger logger,
                     FinallyActionRegistry finallyActionRegistry,
                     @Input("identifier") String taskIdentifier,
                     @Input("what-for") String whatFor,
                     Target target,
                     @Input("inputs") Map<String, Object> inputs,
                     @Input("strategy-type") String strategyType,
                     @Input("strategy-properties") Map<String, Object> strategyProperties
    ) {
        this.logger = logger;
        this.finallyActionRegistry = finallyActionRegistry;
        this.taskIdentifier = requireNonNull(taskIdentifier, "identifier is mandatory");
        this.whatFor = ofNullable(whatFor).orElse(FinalTask.class.getSimpleName());
        this.target = target;
        this.inputs = inputs;
        this.strategyType = strategyType;
        this.strategyProperties = strategyProperties;
    }

    @Override
    public TaskExecutionResult execute() {
        FinallyAction.Builder finallyActionBuilder = FinallyAction.Builder.forAction(taskIdentifier, whatFor);

        ofNullable(target).ifPresent(finallyActionBuilder::withTarget);
        ofNullable(inputs).map(Map::entrySet).ifPresent(entries -> entries.forEach(e -> finallyActionBuilder.withInput(e.getKey(), e.getValue())));
        ofNullable(strategyType).ifPresent(st -> {
            finallyActionBuilder.withStrategyType(st);
            ofNullable(strategyProperties).ifPresent(finallyActionBuilder::withStrategyProperties);
        });

        finallyActionRegistry.registerFinallyAction(finallyActionBuilder.build());
        logger.info(taskIdentifier + " as finally action registered");
        return ok();
    }
}
