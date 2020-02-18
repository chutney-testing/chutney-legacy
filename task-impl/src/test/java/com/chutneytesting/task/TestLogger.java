package com.chutneytesting.task;

import com.chutneytesting.task.spi.injectable.Logger;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.LoggerFactory;

public class TestLogger implements Logger {

    public final List<String> info = new ArrayList<>();
    public final List<String> errors = new ArrayList<>();

    private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(TestLogger.class);

    @Override
    public void info(String message) {
        info.add(message);
        LOGGER.info(message);
    }

    @Override
    public void error(String message) {
        errors.add(message);
        LOGGER.error(message);
    }

    @Override
    public void error(Throwable exception) {
        errors.add(exception.getMessage());
        LOGGER.debug(exception.getMessage(), exception);
    }
}
