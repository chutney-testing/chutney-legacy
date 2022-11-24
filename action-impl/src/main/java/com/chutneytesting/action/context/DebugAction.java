package com.chutneytesting.action.context;

import com.chutneytesting.action.spi.Action;
import com.chutneytesting.action.spi.ActionExecutionResult;
import com.chutneytesting.action.spi.injectable.Input;
import com.chutneytesting.action.spi.injectable.Logger;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class DebugAction implements Action {

    private final Logger logger;
    private final Map<String, Object> inputs;
    private final List<String> filter;

    public DebugAction(Logger logger, Map<String, Object> inputs, @Input("filters") List<String> filter) {
        this.logger = logger;
        this.inputs = inputs;
        this.filter = Optional.ofNullable(filter).orElseGet(Collections::emptyList);
    }

    @Override
    public ActionExecutionResult execute() {
        inputs.entrySet().stream()
            .filter(entry -> filter.isEmpty() || filter.contains(entry.getKey()))
            .forEach(entry -> logger.info(entry.getKey() + " : [" + entry.getValue() + "]"));
        return ActionExecutionResult.ok();
    }
}
