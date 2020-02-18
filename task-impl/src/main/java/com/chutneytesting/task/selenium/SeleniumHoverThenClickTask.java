package com.chutneytesting.task.selenium;

import static com.chutneytesting.task.selenium.parameter.SeleniumActionTaskParameter.BY;
import static com.chutneytesting.task.selenium.parameter.SeleniumActionTaskParameter.SELECTOR;
import static com.chutneytesting.task.selenium.parameter.SeleniumActionTaskParameter.WAIT;
import static com.chutneytesting.task.selenium.parameter.SeleniumActionTaskParameter.WEBDRIVER;

import com.chutneytesting.task.spi.TaskExecutionResult;
import com.chutneytesting.task.spi.injectable.Input;
import com.chutneytesting.task.spi.injectable.Logger;
import java.util.Optional;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;

public class SeleniumHoverThenClickTask extends SeleniumTask implements SeleniumFindBehavior {

    private final String selector;
    private final String by;
    private final Integer wait;

    public SeleniumHoverThenClickTask(Logger logger,
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
    public TaskExecutionResult executeSeleniumTask() {
        Optional<WebElement> menuHoverWebElement = findElement(logger, webDriver, selector, by, wait);
        Actions actions = new Actions(webDriver);

        if (menuHoverWebElement.isPresent()) {
            actions.moveToElement(menuHoverWebElement.get()).build().perform();
            SeleniumClickTask seleniumClickTask = new SeleniumClickTask(logger, webDriver, selector, by, wait);
            return seleniumClickTask.execute();
        }
        takeScreenShot();
        return TaskExecutionResult.ko();
    }
}
