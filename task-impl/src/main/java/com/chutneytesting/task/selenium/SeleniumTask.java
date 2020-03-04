package com.chutneytesting.task.selenium;

import com.chutneytesting.task.spi.Task;
import com.chutneytesting.task.spi.TaskExecutionResult;
import com.chutneytesting.task.spi.injectable.Logger;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;

public abstract class SeleniumTask implements Task {

    protected final Logger logger;
    protected final WebDriver webDriver;

    protected SeleniumTask(Logger logger, WebDriver webDriver) {
        this.logger = logger;
        this.webDriver = webDriver;
    }

    protected abstract TaskExecutionResult executeSeleniumTask();

    @Override
    public final TaskExecutionResult execute() {
        try {
            return this.executeSeleniumTask();
        } catch (Exception e) {
            logger.error(e.toString());
            takeScreenShot();
            return TaskExecutionResult.ko();
        }
    }

    protected void takeScreenShot() {
        try {
            String screenShot = ((TakesScreenshot) webDriver).getScreenshotAs(OutputType.BASE64);
            logger.error("data:image/png;base64," + screenShot);
        } catch (ClassCastException e) {
            logger.error("WebDriver could not take screenshots.");
        } catch (Exception e) {
            logger.error("Error taking screenshot : " + e.getMessage());
        }
    }
}
