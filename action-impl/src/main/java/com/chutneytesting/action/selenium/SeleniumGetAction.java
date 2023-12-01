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

import static com.chutneytesting.action.selenium.parameter.SeleniumActionActionParameter.SELECTOR;
import static com.chutneytesting.action.selenium.parameter.SeleniumActionActionParameter.VALUE;
import static com.chutneytesting.action.selenium.parameter.SeleniumActionActionParameter.WEBDRIVER;

import com.chutneytesting.action.spi.ActionExecutionResult;
import com.chutneytesting.action.spi.injectable.Input;
import com.chutneytesting.action.spi.injectable.Logger;
import java.util.Set;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;

public class SeleniumGetAction extends SeleniumAction {

    private static final String SELENIUM_OUTPUTS_KEY = "outputGet";

    private final String selector;
    private final String value;

    public SeleniumGetAction(Logger logger,
                           @Input(WEBDRIVER) WebDriver webDriver,
                           @Input(SELECTOR) String selector,
                           @Input(VALUE) String value) {
        super(logger, webDriver);
        this.selector = selector;
        this.value = value;
    }

    @Override
    public ActionExecutionResult executeSeleniumAction() {
        if (selector != null) {
            Set<String> afterAllCurrentWindowHandles = getNewWindowHandle(webDriver);

            webDriver.switchTo().window((String) afterAllCurrentWindowHandles.toArray()[0]);
            logger.info("Switch to new window");
        }
        logger.info("Get url : " + value);
        webDriver.get(value);

        return ActionExecutionResult.ok(SELENIUM_OUTPUTS_KEY, webDriver.getWindowHandle());
    }

    private Set<String> getNewWindowHandle(WebDriver webDriver) {
        Set<String> allCurrentWindowHandles = webDriver.getWindowHandles();
        ((JavascriptExecutor) webDriver).executeScript("window.open();");
        Set<String> afterAllCurrentWindowHandles = webDriver.getWindowHandles();
        afterAllCurrentWindowHandles.removeAll(allCurrentWindowHandles);
        return afterAllCurrentWindowHandles;
    }
}
