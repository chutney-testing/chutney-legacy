package com.chutneytesting.task.selenium;

import static com.chutneytesting.task.selenium.parameter.SeleniumActionTaskParameter.WEBDRIVER;

import com.chutneytesting.task.spi.TaskExecutionResult;
import com.chutneytesting.task.spi.injectable.Input;
import com.chutneytesting.task.spi.injectable.Logger;
import org.openqa.selenium.WebDriver;

public class SeleniumCloseTask extends SeleniumTask {

    public SeleniumCloseTask(Logger logger,
                             @Input(WEBDRIVER) WebDriver webDriver) {
        super(logger, webDriver);
    }

    @Override
    public TaskExecutionResult executeSeleniumTask() {
        webDriver.close();
        logger.info("Selenium instance " + webDriver + "closed");
        return TaskExecutionResult.ok();
    }
}
