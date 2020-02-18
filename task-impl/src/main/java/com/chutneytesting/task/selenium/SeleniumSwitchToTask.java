package com.chutneytesting.task.selenium;

import static com.chutneytesting.task.selenium.parameter.SeleniumActionTaskParameter.BY;
import static com.chutneytesting.task.selenium.parameter.SeleniumActionTaskParameter.SELECTOR;
import static com.chutneytesting.task.selenium.parameter.SeleniumActionTaskParameter.SWITCHTYPE;
import static com.chutneytesting.task.selenium.parameter.SeleniumActionTaskParameter.WAIT;
import static com.chutneytesting.task.selenium.parameter.SeleniumActionTaskParameter.WEBDRIVER;

import com.chutneytesting.task.spi.TaskExecutionResult;
import com.chutneytesting.task.spi.injectable.Input;
import com.chutneytesting.task.spi.injectable.Logger;
import java.util.Optional;
import java.util.Set;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

public class SeleniumSwitchToTask extends SeleniumTask implements SeleniumFindBehavior {

    private static final String SELENIUM_OUTPUTS_KEY = "outputSwitchTo";

    private final String selector;
    private final String by;
    private final Integer wait;
    private final String switchType;

    public SeleniumSwitchToTask(Logger logger,
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
    public TaskExecutionResult executeSeleniumTask() {
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
                if (!webElement.isPresent()) {
                    if (selector != null && by == null) {
                        webDriver.switchTo().window(selector);
                        logger.info("Switch to window");
                        return TaskExecutionResult.ok(SELENIUM_OUTPUTS_KEY, webDriver.getWindowHandle());
                    }
                    webDriver.switchTo().defaultContent();
                    logger.info("Switch to default content");
                } else {
                    return TaskExecutionResult.ko();
                }
            }
            break;
            case "Popup": {
                if (!webElement.isPresent()) {
                    Set<String> windowHandlers = webDriver.getWindowHandles();
                    String parentWindowHandler = webDriver.getWindowHandle();

                    String popupHandler = windowHandlers.stream()
                        .filter(h -> !parentWindowHandler.equals(h))
                        .findFirst()
                        .get();
                    webDriver.switchTo().window(popupHandler);
                    logger.info("Switch to popup");
                    return TaskExecutionResult.ok(SELENIUM_OUTPUTS_KEY, popupHandler);
                } else {
                    return TaskExecutionResult.ko();
                }
            }
            case "AlertOk": {
                try {
                    webDriver.switchTo().alert().accept();
                    logger.info("Switch to alert and click on the OK button");
                    return TaskExecutionResult.ok();
                } catch (Exception e) {
                    return TaskExecutionResult.ko();
                }
            }
            case "AlertCancel": {
                try {
                    webDriver.switchTo().alert().dismiss();
                    logger.info("Switch to alert and click on the Cancel button");
                    return TaskExecutionResult.ok();
                } catch (Exception e) {
                    return TaskExecutionResult.ko();
                }
            }
            default: {
                webDriver.switchTo().defaultContent();
                logger.info("Switch to default content");
            }
            break;
        }

        return TaskExecutionResult.ok();
    }
}
