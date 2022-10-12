package com.chutneytesting.action.selenium;

import static com.chutneytesting.action.selenium.parameter.SeleniumActionActionParameter.BY;
import static com.chutneytesting.action.selenium.parameter.SeleniumActionActionParameter.SELECTOR;
import static com.chutneytesting.action.selenium.parameter.SeleniumActionActionParameter.SWITCHTYPE;
import static com.chutneytesting.action.selenium.parameter.SeleniumActionActionParameter.WAIT;
import static com.chutneytesting.action.selenium.parameter.SeleniumActionActionParameter.WEBDRIVER;

import com.chutneytesting.action.spi.ActionExecutionResult;
import com.chutneytesting.action.spi.injectable.Input;
import com.chutneytesting.action.spi.injectable.Logger;
import java.util.Optional;
import java.util.Set;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

public class SeleniumSwitchToAction extends SeleniumAction implements SeleniumFindBehavior {

    private static final String SELENIUM_OUTPUTS_KEY = "outputSwitchTo";

    private final String selector;
    private final String by;
    private final Integer wait;
    private final String switchType;

    public SeleniumSwitchToAction(Logger logger,
                                @Input(WEBDRIVER) WebDriver webDriver,
                                @Input(SELECTOR) String selector,
                                @Input(BY) String by,
                                @Input(WAIT) Integer wait,
                                @Input(SWITCHTYPE) String switchType) {
        super(logger, webDriver);
        this.selector = selector;
        this.by = by;
        this.wait = wait;
        this.switchType = switchType;
    }

    @Override
    public ActionExecutionResult executeSeleniumAction() {
        Optional<WebElement> webElement = findElement(logger, webDriver, selector, by, wait);

        switch (Optional.ofNullable(switchType).orElse("")) {
            case "Frame": {
                if (webElement.isPresent()) {
                    webDriver.switchTo().frame(webElement.get());
                    logger.info("Switch to frame");
                }
            }
            break;
            case "Window": {
                if (webElement.isEmpty()) {
                    if (selector != null && by == null) {
                        webDriver.switchTo().window(selector);
                        logger.info("Switch to window");
                        return ActionExecutionResult.ok(SELENIUM_OUTPUTS_KEY, webDriver.getWindowHandle());
                    }
                    webDriver.switchTo().defaultContent();
                    logger.info("Switch to default content");
                } else {
                    return ActionExecutionResult.ko();
                }
            }
            break;
            case "Popup": {
                if (webElement.isEmpty()) {
                    Set<String> windowHandlers = webDriver.getWindowHandles();
                    String parentWindowHandler = webDriver.getWindowHandle();

                    String popupHandler = windowHandlers.stream()
                        .filter(h -> !parentWindowHandler.equals(h))
                        .findFirst()
                        .get();
                    webDriver.switchTo().window(popupHandler);
                    logger.info("Switch to popup");
                    return ActionExecutionResult.ok(SELENIUM_OUTPUTS_KEY, popupHandler);
                } else {
                    return ActionExecutionResult.ko();
                }
            }
            case "AlertOk": {
                try {
                    webDriver.switchTo().alert().accept();
                    logger.info("Switch to alert and click on the OK button");
                    return ActionExecutionResult.ok();
                } catch (Exception e) {
                    return ActionExecutionResult.ko();
                }
            }
            case "AlertCancel": {
                try {
                    webDriver.switchTo().alert().dismiss();
                    logger.info("Switch to alert and click on the Cancel button");
                    return ActionExecutionResult.ok();
                } catch (Exception e) {
                    return ActionExecutionResult.ko();
                }
            }
            default: {
                webDriver.switchTo().defaultContent();
                logger.info("Switch to default content");
            }
            break;
        }

        return ActionExecutionResult.ok();
    }
}
