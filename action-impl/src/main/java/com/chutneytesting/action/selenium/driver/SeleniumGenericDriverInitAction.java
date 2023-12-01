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

import com.chutneytesting.action.spi.injectable.FinallyActionRegistry;
import com.chutneytesting.action.spi.injectable.Input;
import com.chutneytesting.action.spi.injectable.Logger;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;
import org.openqa.selenium.Capabilities;
import org.openqa.selenium.MutableCapabilities;
import org.openqa.selenium.WebDriver;

public class SeleniumGenericDriverInitAction extends AbstractSeleniumDriverInitAction {

    private final ObjectMapper mapper = new ObjectMapper();
    private final String seleniumConfigurationAsJson;
    protected SeleniumGenericDriverInitAction(FinallyActionRegistry finallyActionRegistry,
                                              Logger logger,
                                              @Input("hub") String hubUrl,
                                              @Input("jsonConfiguration") String seleniumConfigurationAsJson) {
        super(finallyActionRegistry, logger, hubUrl, null, null, null);
        this.seleniumConfigurationAsJson = seleniumConfigurationAsJson;
    }

    @Override
    protected MutableCapabilities buildOptions() {
        Map<String, Object> readFromJson;
        try {
            readFromJson = mapper.readValue(seleniumConfigurationAsJson, new TypeReference<>() {
            });
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Parsing error. Cannot transform json configuration to Selenium capabilities");
        }
        return new MutableCapabilities(readFromJson);
    }

    @Override
    protected WebDriver localWebDriver(Capabilities capabilities) {
        throw new IllegalStateException("Cannot create generic local web driver");
    }

    @Override
    protected Class<?> getChildClass() {
        return SeleniumGenericDriverInitAction.class;
    }
}
