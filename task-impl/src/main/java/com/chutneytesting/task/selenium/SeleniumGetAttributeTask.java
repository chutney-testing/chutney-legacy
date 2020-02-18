package com.chutneytesting.task.selenium;

import static com.chutneytesting.task.selenium.parameter.SeleniumActionTaskParameter.BY;
import static com.chutneytesting.task.selenium.parameter.SeleniumActionTaskParameter.SELECTOR;
import static com.chutneytesting.task.selenium.parameter.SeleniumActionTaskParameter.WAIT;
import static com.chutneytesting.task.selenium.parameter.SeleniumActionTaskParameter.WEBDRIVER;

import com.chutneytesting.task.spi.TaskExecutionResult;
import com.chutneytesting.task.spi.injectable.Input;
import com.chutneytesting.task.spi.injectable.Logger;
import java.util.Optional;
import org.apache.commons.lang3.StringUtils;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

public class SeleniumGetAttributeTask extends SeleniumTask implements SeleniumFindBehavior {

    private static final String SELENIUM_OUTPUTS_KEY = "outputAttributeValue";

    private final String selector;
    private final String by;
    private final Integer wait;
    private final String attribute;

    public SeleniumGetAttributeTask(Logger logger,
                                    @Input(WEBDRIVER) WebDriver webDriver,
                                    @Input(SELECTOR) String selector,
                                    @Input(BY) String by,
                                    @Input(WAIT) Integer wait,
                                    @Input("attribute") String attribute) {
        super(logger, webDriver);
        this.selector = selector;
        this.by = by;
        this.wait = wait;
        this.attribute = attribute;
    }

    @Override
    public TaskExecutionResult executeSeleniumTask() {
        Optional<WebElement> webElement = findElement(logger, webDriver, selector, by, wait);

        if (webElement.isPresent()) {
            logger.info("Get attribute from element : " + webElement.get());
            ((JavascriptExecutor) webDriver).executeScript("arguments[0].style.background='yellow'", webElement.get());

            if (StringUtils.isNotBlank(attribute)) {
                return TaskExecutionResult.ok(SELENIUM_OUTPUTS_KEY, webElement.get().getAttribute(attribute));
            } else {
                return TaskExecutionResult.ok(SELENIUM_OUTPUTS_KEY, webElement.get().getAttribute("value"));
            }
        } else {
            takeScreenShot();
            logger.error("Cannot retrieve value of attribute " + attribute + " from element " + selector);
            return TaskExecutionResult.ko();
        }
    }
}
