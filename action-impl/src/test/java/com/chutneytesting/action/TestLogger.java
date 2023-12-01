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

package com.chutneytesting.action;

import com.chutneytesting.action.spi.injectable.Logger;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.LoggerFactory;

public class TestLogger implements Logger {


    public final List<String> info = new ArrayList<>();
    public final List<String> errors = new ArrayList<>();
    public final Logger reportOnly = new TestReportLogger();

    private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(TestLogger.class);

    @Override
    public void info(String message) {
        info.add(message);
        LOGGER.info(message);
    }

    @Override
    public void error(String message) {
        errors.add(message);
        LOGGER.error(message);
    }

    @Override
    public void error(Throwable exception) {
        errors.add(exception.getMessage());
        LOGGER.debug(exception.getMessage(), exception);
    }

    @Override
    public Logger reportOnly() {
        return reportOnly;
    }

    private class TestReportLogger implements Logger {

        @Override
        public void info(String message) {
            info.add(message);
        }

        @Override
        public void error(String message) {
            errors.add(message);
        }

        @Override
        public void error(Throwable exception) {
            errors.add(exception.getMessage());
        }

        @Override
        public Logger reportOnly() {
            return this;
        }
    }
}
