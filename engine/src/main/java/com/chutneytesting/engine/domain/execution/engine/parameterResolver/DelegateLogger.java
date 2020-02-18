package com.chutneytesting.engine.domain.execution.engine.parameterResolver;

import com.chutneytesting.task.spi.injectable.Logger;
import java.util.function.Consumer;
import org.slf4j.LoggerFactory;

public class DelegateLogger implements Logger {

    private final Consumer<String> infoConsumer;
    private final Consumer<String> errorConsumer;

    private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(DelegateLogger.class);

    public DelegateLogger(Consumer<String> infoConsumer, Consumer<String> errorConsumer) {
        this.infoConsumer = infoConsumer;
        this.errorConsumer = errorConsumer;
    }

    @Override
    public void info(String message) {
        infoConsumer.accept(message);
        LOGGER.debug(message);
    }

    @Override
    public void error(String message) {
        errorConsumer.accept(message);
        LOGGER.debug(message);
    }

    @Override
    public void error(Throwable exception) {
        errorConsumer.accept(exception.getMessage());
        LOGGER.debug(exception.getMessage(), exception);
    }
}
