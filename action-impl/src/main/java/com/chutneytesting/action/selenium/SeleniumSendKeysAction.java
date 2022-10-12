package com.chutneytesting.action.selenium;

import static com.chutneytesting.action.selenium.parameter.SeleniumActionActionParameter.BY;
import static com.chutneytesting.action.selenium.parameter.SeleniumActionActionParameter.SELECTOR;
import static com.chutneytesting.action.selenium.parameter.SeleniumActionActionParameter.VALUE;
import static com.chutneytesting.action.selenium.parameter.SeleniumActionActionParameter.WAIT;
import static com.chutneytesting.action.selenium.parameter.SeleniumActionActionParameter.WEBDRIVER;

import com.chutneytesting.action.spi.ActionExecutionResult;
import com.chutneytesting.action.spi.injectable.Input;
import com.chutneytesting.action.spi.injectable.Logger;
import java.util.Optional;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.Select;

public class SeleniumSendKeysAction extends SeleniumAction implements SeleniumFindBehavior {

    private final String value;
    private final String selector;
    private final String by;
    private final Integer wait;

    public SeleniumSendKeysAction(Logger logger,
                                @Input(WEBDRIVER) WebDriver webDriver,
                                @Input(SELECTOR) String selector,
                                @Input(BY) String by,
                                @Input(WAIT) Integer wait,
                                @Input(VALUE) String value) {
        super(logger, webDriver);
        this.selector = selector;
        this.by = by;
        this.wait = wait;
        this.value = value;
    }

    @Override
    public ActionExecutionResult executeSeleniumAction() {
        if (value == null) {
            logger.error("No value given for sendKeys.");
            return ActionExecutionResult.ko();
        }

        Optional<WebElement> webElementOpt = findElement(logger, webDriver, selector, by, wait);

        if (webElementOpt.isPresent()) {
            WebElement webElement = webElementOpt.get();
            if ("select".equals(webElement.getTagName())) {
                new Select(webElement).selectByValue(value);
            } else {
                String[] values = value.split("\\*");
                Optional<Keys> key = mapKeysFromValue(values[0]);
                if (key.isPresent()) {
                    webElement.sendKeys(key.get() + (values.length > 1 ? values[1] : ""));
                } else {
                    if ("input".equals(webElement.getTagName()) || "textaera".equals(webElement.getTagName())) {
                        try {
                            ((JavascriptExecutor) webDriver).executeScript("arguments[0].value = ''", webElement);
                        } catch (Exception e) {
                            logger.info("JS clearing failed, try simple clearing");
                            try {
                                webElement.clear();
                            } catch (ClassCastException cc) {
                                logger.error("WebDriver cannot execute simple clearing");
                                return ActionExecutionResult.ko();
                            }
                        }
                    }
                    webElement.sendKeys(value);
                }
            }

            logger.info("Send keys to element : " + webElement);
            return ActionExecutionResult.ok();
        } else {
            takeScreenShot();
            logger.error("Cannot retrieve element to sendKeys to.");
            return ActionExecutionResult.ko();
        }
    }


    private Optional<Keys> mapKeysFromValue(String value) {
        try {
            return Optional.of(Keys.valueOf(value));
        } catch (IllegalArgumentException e) { /* do nothing */ }
        return Optional.empty();
    }


}
