package com.chutneytesting.task.selenium;

import static com.chutneytesting.task.selenium.parameter.SeleniumActionTaskParameter.WEBDRIVER;

import com.chutneytesting.task.spi.TaskExecutionResult;
import com.chutneytesting.task.spi.injectable.Input;
import com.chutneytesting.task.spi.injectable.Logger;
import org.openqa.selenium.WebDriver;

public class SeleniumQuitTask extends SeleniumTask {

    public SeleniumQuitTask(Logger logger,
                            @Input(WEBDRIVER) WebDriver webDriver) {
        super(logger, webDriver);
    }

    @Override
    public TaskExecutionResult executeSeleniumTask() {
        webDriver.quit();
        return TaskExecutionResult.ok();
    }
}
