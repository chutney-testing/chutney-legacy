package com.chutneytesting.action.selenium;

import static com.chutneytesting.action.selenium.parameter.SeleniumActionActionParameter.BY;
import static com.chutneytesting.action.selenium.parameter.SeleniumActionActionParameter.SELECTOR;
import static com.chutneytesting.action.selenium.parameter.SeleniumActionActionParameter.WAIT;
import static com.chutneytesting.action.selenium.parameter.SeleniumActionActionParameter.WEBDRIVER;

import com.chutneytesting.action.spi.ActionExecutionResult;
import com.chutneytesting.action.spi.injectable.Input;
import com.chutneytesting.action.spi.injectable.Logger;
import java.util.Optional;
import java.util.function.Function;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;

public class SeleniumClickAction extends SeleniumAction implements SeleniumFindBehavior {

    private final String selector;
    private final String by;
    private final Integer wait;

    public SeleniumClickAction(Logger logger,
                             @Input(WEBDRIVER) WebDriver webDriver,
                             @Input(SELECTOR) String selector,
                             @Input(BY) String by,
                             @Input(WAIT) Integer wait) {
        super(logger, webDriver);
        this.selector = selector;
        this.by = by;
        this.wait = wait;
    }

    @Override
    public ActionExecutionResult executeSeleniumAction() {
        Optional<WebElement> webElement = findElement(logger, webDriver, selector, by, wait);

        if (webElement.isPresent()) {
            try {
                webElement.get().click();
            } catch (Exception e) {
                logger.error("Simple click failed, try JS click");
                try {
                    ((JavascriptExecutor) webDriver).executeScript("arguments[0].click();", webElement.get());
                } catch (ClassCastException cc) {
                    logger.error("WebDriver cannot execute JS scripts");
                    return ActionExecutionResult.ko();
                }
            }
            logger.info("Click on element : " + webElement.get());
            return ActionExecutionResult.ok();
        } else {
            takeScreenShot();
            logger.error("Cannot retrieve element to click.");
            return ActionExecutionResult.ko();
        }
    }

    @Override
    public Function<WebDriver, WebElement> findExpectation(By by) {
        return ExpectedConditions.elementToBeClickable(by);
    }
}
