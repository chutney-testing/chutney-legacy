package com.chutneytesting.task.spi.injectable;

public interface TasksConfiguration {

    String getString(String key);

    String getString(String key, String defaultValue);

    Integer getInteger(String key);

    Integer getInteger(String key, Integer defaultValue);
}
