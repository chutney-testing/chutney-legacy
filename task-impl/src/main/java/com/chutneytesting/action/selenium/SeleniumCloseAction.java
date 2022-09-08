package com.chutneytesting.action.selenium;

import static com.chutneytesting.action.selenium.parameter.SeleniumActionActionParameter.WEBDRIVER;

import com.chutneytesting.action.spi.ActionExecutionResult;
import com.chutneytesting.action.spi.injectable.Input;
import com.chutneytesting.action.spi.injectable.Logger;
import org.openqa.selenium.WebDriver;

public class SeleniumCloseAction extends SeleniumAction {

    public SeleniumCloseAction(Logger logger,
                             @Input(WEBDRIVER) WebDriver webDriver) {
        super(logger, webDriver);
    }

    @Override
    public ActionExecutionResult executeSeleniumAction() {
        webDriver.close();
        logger.info("Selenium instance " + webDriver + "closed");
        return ActionExecutionResult.ok();
    }
}
