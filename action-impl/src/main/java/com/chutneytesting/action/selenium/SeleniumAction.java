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

import com.chutneytesting.action.spi.Action;
import com.chutneytesting.action.spi.ActionExecutionResult;
import com.chutneytesting.action.spi.injectable.Logger;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;

public abstract class SeleniumAction implements Action {

    protected final Logger logger;
    protected final WebDriver webDriver;

    protected SeleniumAction(Logger logger, WebDriver webDriver) {
        this.logger = logger;
        this.webDriver = webDriver;
    }

    protected abstract ActionExecutionResult executeSeleniumAction();

    @Override
    public final ActionExecutionResult execute() {
        try {
            return this.executeSeleniumAction();
        } catch (Exception e) {
            logger.error(e.toString());
            takeScreenShot();
            return ActionExecutionResult.ko();
        }
    }

    protected void takeScreenShot() {
        try {
            String screenShot = ((TakesScreenshot) webDriver).getScreenshotAs(OutputType.BASE64);
            logger.error("data:image/png;base64," + screenShot);
        } catch (ClassCastException e) {
            logger.error("WebDriver could not take screenshots.");
        } catch (Exception e) {
            logger.error("Error taking screenshot : " + e.getMessage());
        }
    }
}
