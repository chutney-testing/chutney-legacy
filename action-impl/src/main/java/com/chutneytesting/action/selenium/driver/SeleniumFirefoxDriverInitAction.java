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

import static java.util.Collections.emptyMap;
import static java.util.Optional.ofNullable;

import com.chutneytesting.action.spi.injectable.FinallyActionRegistry;
import com.chutneytesting.action.spi.injectable.Input;
import com.chutneytesting.action.spi.injectable.Logger;
import java.io.IOException;
import java.util.Map;
import org.openqa.selenium.Capabilities;
import org.openqa.selenium.MutableCapabilities;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxDriverLogLevel;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.firefox.FirefoxProfile;

public class SeleniumFirefoxDriverInitAction extends AbstractSeleniumDriverInitAction {

    private final String firefoxProfile;
    private final Map<String, String> firefoxPreferences;

    public SeleniumFirefoxDriverInitAction(FinallyActionRegistry finallyActionRegistry,
                                           Logger logger,
                                           @Input("hub") String hubUrl,
                                           @Input("headless") Boolean headless,
                                           @Input("driverPath") String driverPath,
                                           @Input("browserPath") String browserPath,
                                           @Input("firefoxProfile")String firefoxProfile,
                                           @Input("firefoxPreferences")Map<String, String> firefoxPreferences) {
        super(finallyActionRegistry, logger, hubUrl, headless, driverPath, browserPath);
        this.firefoxProfile = firefoxProfile;
        this.firefoxPreferences = ofNullable(firefoxPreferences).orElse(emptyMap());
    }

    @Override
    protected MutableCapabilities buildOptions() {
        FirefoxOptions firefoxOptions = new FirefoxOptions();
        firefoxOptions.addArguments("-headless");
        firefoxOptions.setLogLevel(FirefoxDriverLogLevel.FATAL);
        try {
            FirefoxProfile profile = firefoxProfile != null ? FirefoxProfile.fromJson(firefoxProfile) : new FirefoxProfile();
            firefoxPreferences.forEach(profile::setPreference);
            firefoxOptions.setProfile(profile);
        } catch (IOException e) {
            logger.error("Failed to read firefox profile" + e.getMessage());
        }
        return firefoxOptions;
    }

    @Override
    protected WebDriver localWebDriver(Capabilities capabilities) {
        System.setProperty("webdriver.gecko.driver", driverPath);
        FirefoxOptions firefoxOptions = new FirefoxOptions(capabilities);
        firefoxOptions.setBinary(browserPath);
        return new FirefoxDriver(firefoxOptions);
    }

    @Override
    protected Class<?> getChildClass() {
        return SeleniumFirefoxDriverInitAction.class;
    }
}
