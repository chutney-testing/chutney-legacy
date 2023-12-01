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

import static com.chutneytesting.action.selenium.parameter.SeleniumActionActionParameter.WEBDRIVER;

import com.chutneytesting.action.spi.ActionExecutionResult;
import com.chutneytesting.action.spi.injectable.Input;
import com.chutneytesting.action.spi.injectable.Logger;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.Point;
import org.openqa.selenium.WebDriver;

public class SeleniumSetBrowserSizeAction extends SeleniumAction {

    private final Integer width;
    private final Integer height;

    public SeleniumSetBrowserSizeAction(Logger logger,
                                      @Input(WEBDRIVER) WebDriver webDriver,
                                      @Input("width") Integer width,
                                      @Input("height") Integer height) {
        super(logger, webDriver);
        this.width = width;
        this.height = height;
    }

    @Override
    public ActionExecutionResult executeSeleniumAction() {
        try {
            webDriver.manage().window().setPosition(new Point(0, 0));
            webDriver.manage().window().setSize(new Dimension(width, height));

            logger.info("Set browser window size to " + width + "x" + height);
        } catch (NumberFormatException e) {
            logger.error("Failed to parse width: " + width + " or height: " + height);
            return ActionExecutionResult.ko();
        }
        return ActionExecutionResult.ok();
    }
}
