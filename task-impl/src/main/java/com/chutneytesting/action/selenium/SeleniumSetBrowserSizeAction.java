package com.chutneytesting.action.selenium;

import static com.chutneytesting.action.selenium.parameter.SeleniumActionActionParameter.WEBDRIVER;

import com.chutneytesting.action.spi.ActionExecutionResult;
import com.chutneytesting.action.spi.injectable.Input;
import com.chutneytesting.action.spi.injectable.Logger;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.Point;
import org.openqa.selenium.WebDriver;

public class SeleniumSetBrowserSizeAction extends SeleniumAction {

    private final Integer width;
    private final Integer height;

    public SeleniumSetBrowserSizeAction(Logger logger,
                                      @Input(WEBDRIVER) WebDriver webDriver,
                                      @Input("width") Integer width,
                                      @Input("height") Integer height) {
        super(logger, webDriver);
        this.width = width;
        this.height = height;
    }

    @Override
    public ActionExecutionResult executeSeleniumAction() {
        try {
            webDriver.manage().window().setPosition(new Point(0, 0));
            webDriver.manage().window().setSize(new Dimension(width, height));

            logger.info("Set browser window size to " + width + "x" + height);
        } catch (NumberFormatException e) {
            logger.error("Failed to parse width: " + width + " or height: " + height);
            return ActionExecutionResult.ko();
        }
        return ActionExecutionResult.ok();
    }
}
