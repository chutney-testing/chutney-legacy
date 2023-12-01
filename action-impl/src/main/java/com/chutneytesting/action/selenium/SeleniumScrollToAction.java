/*
 * Copyright 2017-2023 Enedis
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.chutneytesting.action.selenium;

import static com.chutneytesting.action.selenium.parameter.SeleniumActionActionParameter.BY;
import static com.chutneytesting.action.selenium.parameter.SeleniumActionActionParameter.SELECTOR;
import static com.chutneytesting.action.selenium.parameter.SeleniumActionActionParameter.WAIT;
import static com.chutneytesting.action.selenium.parameter.SeleniumActionActionParameter.WEBDRIVER;

import com.chutneytesting.action.spi.ActionExecutionResult;
import com.chutneytesting.action.spi.injectable.Input;
import com.chutneytesting.action.spi.injectable.Logger;
import java.util.Optional;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

public class SeleniumScrollToAction extends SeleniumAction implements SeleniumFindBehavior {

    private final String selector;
    private final String by;
    private final Integer wait;

    public SeleniumScrollToAction(Logger logger,
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
            ((JavascriptExecutor) webDriver).executeScript("arguments[0].scrollIntoView(true);", webElement.get());
            logger.info("Scroll to element : " + webElement.get());
            return ActionExecutionResult.ok();
        }
        logger.error("Cannot retrieve element to scroll.");
        return ActionExecutionResult.ko();
    }
}
