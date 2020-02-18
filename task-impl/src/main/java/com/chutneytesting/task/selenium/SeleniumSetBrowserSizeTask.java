package com.chutneytesting.task.selenium;

import static com.chutneytesting.task.selenium.parameter.SeleniumActionTaskParameter.WEBDRIVER;

import com.chutneytesting.task.spi.TaskExecutionResult;
import com.chutneytesting.task.spi.injectable.Input;
import com.chutneytesting.task.spi.injectable.Logger;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.Point;
import org.openqa.selenium.WebDriver;

public class SeleniumSetBrowserSizeTask extends SeleniumTask {

    private final Integer width;
    private final Integer height;

    public SeleniumSetBrowserSizeTask(Logger logger,
                                      @Input(WEBDRIVER) WebDriver webDriver,
                                      @Input("width") Integer width,
                                      @Input("height") Integer height) {
        super(logger, webDriver);
        this.width = width;
        this.height = height;
    }

    @Override
    public TaskExecutionResult executeSeleniumTask() {
        try {
            webDriver.manage().window().setPosition(new Point(0, 0));
            webDriver.manage().window().setSize(new Dimension(width, height));

            logger.info("Set browser window size to " + width + "x" + height);
        } catch (NumberFormatException e) {
            logger.error("Failed to parse width: " + width + " or height: " + height);
            return TaskExecutionResult.ko();
        }
        return TaskExecutionResult.ok();
    }
}
