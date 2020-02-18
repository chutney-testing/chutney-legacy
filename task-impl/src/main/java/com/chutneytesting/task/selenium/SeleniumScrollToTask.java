package com.chutneytesting.task.selenium;

import static com.chutneytesting.task.selenium.parameter.SeleniumActionTaskParameter.BY;
import static com.chutneytesting.task.selenium.parameter.SeleniumActionTaskParameter.SELECTOR;
import static com.chutneytesting.task.selenium.parameter.SeleniumActionTaskParameter.WAIT;
import static com.chutneytesting.task.selenium.parameter.SeleniumActionTaskParameter.WEBDRIVER;

import com.chutneytesting.task.spi.TaskExecutionResult;
import com.chutneytesting.task.spi.injectable.Input;
import com.chutneytesting.task.spi.injectable.Logger;
import java.util.Optional;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

public class SeleniumScrollToTask extends SeleniumTask implements SeleniumFindBehavior {

    private final String selector;
    private final String by;
    private final Integer wait;

    public SeleniumScrollToTask(Logger logger,
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
    TaskExecutionResult executeSeleniumTask() {
        Optional<WebElement> webElement = findElement(logger, webDriver, selector, by, wait);

        if (webElement.isPresent()) {
            ((JavascriptExecutor) webDriver).executeScript("arguments[0].scrollIntoView(true);", webElement.get());
            logger.info("Scroll to element : " + webElement.get());
            return TaskExecutionResult.ok();
        }
        logger.error("Cannot retrieve element to scroll.");
        return TaskExecutionResult.ko();
    }
}
