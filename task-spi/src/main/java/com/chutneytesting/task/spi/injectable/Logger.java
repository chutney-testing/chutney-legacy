package com.chutneytesting.task.spi.injectable;

public interface Logger {

    void info(String message);

    void error(String message);

    void error(Throwable exception);

    Logger reportOnly();
}
