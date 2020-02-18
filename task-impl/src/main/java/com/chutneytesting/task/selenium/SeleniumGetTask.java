package com.chutneytesting.task.selenium;

import static com.chutneytesting.task.selenium.parameter.SeleniumActionTaskParameter.SELECTOR;
import static com.chutneytesting.task.selenium.parameter.SeleniumActionTaskParameter.VALUE;
import static com.chutneytesting.task.selenium.parameter.SeleniumActionTaskParameter.WEBDRIVER;

import com.chutneytesting.task.spi.TaskExecutionResult;
import com.chutneytesting.task.spi.injectable.Input;
import com.chutneytesting.task.spi.injectable.Logger;
import java.util.Set;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;

public class SeleniumGetTask extends SeleniumTask {

    private static final String SELENIUM_OUTPUTS_KEY = "outputGet";

    private final String selector;
    private final String value;

    public SeleniumGetTask(Logger logger,
                           @Input(WEBDRIVER) WebDriver webDriver,
                           @Input(SELECTOR) String selector,
                           @Input(VALUE) String value) {
        super(logger, webDriver);
        this.selector = selector;
        this.value = value;
    }

    @Override
    public TaskExecutionResult executeSeleniumTask() {
        if (selector != null) {
            Set<String> afterAllCurrentWindowHandles = getNewWindowHandle(webDriver);

            webDriver.switchTo().window((String) afterAllCurrentWindowHandles.toArray()[0]);
            logger.info("Switch to new window");
        }
        logger.info("Get url : " + value);
        webDriver.get(value);

        return TaskExecutionResult.ok(SELENIUM_OUTPUTS_KEY, webDriver.getWindowHandle());
    }

    private Set<String> getNewWindowHandle(WebDriver webDriver) {
        Set<String> allCurrentWindowHandles = webDriver.getWindowHandles();
        ((JavascriptExecutor) webDriver).executeScript("window.open();");
        Set<String> afterAllCurrentWindowHandles = webDriver.getWindowHandles();
        afterAllCurrentWindowHandles.removeAll(allCurrentWindowHandles);
        return afterAllCurrentWindowHandles;
    }
}
