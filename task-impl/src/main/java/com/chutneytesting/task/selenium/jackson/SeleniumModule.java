package com.chutneytesting.task.selenium.jackson;

import com.fasterxml.jackson.databind.module.SimpleModule;
import org.openqa.selenium.WebDriver;

public class SeleniumModule extends SimpleModule {

    private static final String NAME = "CustomIntervalModule";

    public SeleniumModule() {
        super(NAME);
        addSerializer(WebDriver.class, new WebDriverSerializer());
    }
}
