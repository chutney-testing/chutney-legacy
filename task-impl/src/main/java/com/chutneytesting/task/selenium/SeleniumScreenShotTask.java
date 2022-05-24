package com.chutneytesting.task.selenium;

import static com.chutneytesting.task.selenium.parameter.SeleniumActionTaskParameter.WEBDRIVER;

import com.chutneytesting.task.spi.TaskExecutionResult;
import com.chutneytesting.task.spi.injectable.Input;
import com.chutneytesting.task.spi.injectable.Logger;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;

public class SeleniumScreenShotTask extends SeleniumTask {

    public SeleniumScreenShotTask(Logger logger,
                                  @Input(WEBDRIVER) WebDriver webDriver) {
        super(logger, webDriver);
    }

    @Override
    public TaskExecutionResult executeSeleniumTask() {
        String screenShot = ((TakesScreenshot) webDriver).getScreenshotAs(OutputType.BASE64);
        logger.reportOnly().info("data:image/png;base64," + screenShot);

        return TaskExecutionResult.ok();
    }
}
