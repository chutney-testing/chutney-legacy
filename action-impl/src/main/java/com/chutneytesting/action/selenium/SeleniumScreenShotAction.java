package com.chutneytesting.action.selenium;

import static com.chutneytesting.action.selenium.parameter.SeleniumActionActionParameter.WEBDRIVER;

import com.chutneytesting.action.spi.ActionExecutionResult;
import com.chutneytesting.action.spi.injectable.Input;
import com.chutneytesting.action.spi.injectable.Logger;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;

public class SeleniumScreenShotAction extends SeleniumAction {

    public SeleniumScreenShotAction(Logger logger,
                                  @Input(WEBDRIVER) WebDriver webDriver) {
        super(logger, webDriver);
    }

    @Override
    public ActionExecutionResult executeSeleniumAction() {
        String screenShot = ((TakesScreenshot) webDriver).getScreenshotAs(OutputType.BASE64);
        logger.reportOnly().info("data:image/png;base64," + screenShot);

        return ActionExecutionResult.ok();
    }
}
