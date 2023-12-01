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

package com.chutneytesting.action.selenium.driver;

import static com.chutneytesting.action.spi.validation.Validator.getErrorsFrom;
import static com.chutneytesting.action.spi.validation.Validator.of;
import static java.time.temporal.ChronoUnit.SECONDS;
import static org.apache.commons.lang3.StringUtils.isNotEmpty;

import com.chutneytesting.action.spi.Action;
import com.chutneytesting.action.spi.ActionExecutionResult;
import com.chutneytesting.action.spi.FinallyAction;
import com.chutneytesting.action.spi.injectable.FinallyActionRegistry;
import com.chutneytesting.action.spi.injectable.Logger;
import com.chutneytesting.action.spi.validation.Validator;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.openqa.selenium.Capabilities;
import org.openqa.selenium.MutableCapabilities;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.remote.RemoteWebDriver;

public abstract class AbstractSeleniumDriverInitAction implements Action {

    private final FinallyActionRegistry finallyActionRegistry;
    private final String hubUrl;

    protected final Logger logger;
    protected final Boolean headless;

    protected final String driverPath;
    protected final String browserPath;

    protected AbstractSeleniumDriverInitAction(FinallyActionRegistry finallyActionRegistry,
                                               Logger logger,
                                               String hubUrl,
                                               Boolean headless,
                                               String driverPath,
                                               String browserPath) {
        this.finallyActionRegistry = finallyActionRegistry;
        this.logger = logger;
        this.hubUrl = hubUrl;
        this.headless = headless;
        this.driverPath = driverPath;
        this.browserPath = browserPath;
    }

    protected abstract MutableCapabilities buildOptions();

    protected abstract WebDriver localWebDriver(Capabilities capabilities);

    protected abstract Class<?> getChildClass();

    @Override
    public List<String> validateInputs() {
        Validator<String> validate = of(hubUrl)
            .validate(hub -> isNotEmpty(hub) || (isNotEmpty(browserPath) && isNotEmpty(driverPath)), "Provide [hub] for selenium remote or [browserPath and driverPath] for selenium local");
        return getErrorsFrom(validate);
    }

    @Override
    public ActionExecutionResult execute() {
        MutableCapabilities capabilities = buildOptions();

        WebDriver webDriver = createWebDriver(capabilities);
        configureWebDriver(webDriver);
        logger.info("RemoteWebDriver created : " + webDriver);
        createQuitFinallyAction(webDriver);
        return ActionExecutionResult.ok(toOutputs(webDriver));
    }

    WebDriver createWebDriver(Capabilities capabilities) {
        try {
            if (isNotEmpty(hubUrl)) {
                return new RemoteWebDriver(new URL(hubUrl), capabilities);
            } else {
                return localWebDriver(capabilities);
            }
        } catch (MalformedURLException e) {
            throw new IllegalStateException(e.getMessage());
        }
    }


    private void createQuitFinallyAction(WebDriver webDriver) {
        finallyActionRegistry.registerFinallyAction(
            FinallyAction.Builder
                .forAction("selenium-quit", getChildClass())
                .withInput("web-driver", webDriver)
                .build()
        );
        logger.info("Quit finally action registered");
    }

    private void configureWebDriver(WebDriver webDriver) {
        try {
            webDriver.manage().timeouts().implicitlyWait(Duration.of(0, SECONDS));
        } catch (Exception e) {
            logger.error("Default configuration of the remote webDriver failed");
        }
    }

    private Map<String, Object> toOutputs(WebDriver webDriver) {
        Map<String, Object> outputs = new HashMap<>();
        outputs.put("webDriver", webDriver);
        return outputs;
    }
}
