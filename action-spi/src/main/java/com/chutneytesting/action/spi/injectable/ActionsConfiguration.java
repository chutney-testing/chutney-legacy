package com.chutneytesting.action.spi.injectable;

public interface ActionsConfiguration {

    String getString(String key);

    String getString(String key, String defaultValue);

    Integer getInteger(String key);

    Integer getInteger(String key, Integer defaultValue);
}
