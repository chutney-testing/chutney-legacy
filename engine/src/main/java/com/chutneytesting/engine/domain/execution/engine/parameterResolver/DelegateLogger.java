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

package com.chutneytesting.engine.domain.execution.engine.parameterResolver;

import com.chutneytesting.action.spi.injectable.Logger;
import java.util.function.Consumer;
import org.slf4j.LoggerFactory;

public class DelegateLogger implements Logger {

    private final Consumer<String> infoConsumer;
    private final Consumer<String> errorConsumer;

    private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(DelegateLogger.class);
    private final Logger reportOnly = new ReportLogger();

    public DelegateLogger(Consumer<String> infoConsumer, Consumer<String> errorConsumer) {
        this.infoConsumer = infoConsumer;
        this.errorConsumer = errorConsumer;
    }

    @Override
    public void info(String message) {
        infoConsumer.accept(message);
        LOGGER.debug(message);
    }

    @Override
    public void error(String message) {
        errorConsumer.accept(message);
        LOGGER.debug(message);
    }

    @Override
    public void error(Throwable exception) {
        errorConsumer.accept(exception.getMessage());
        LOGGER.debug(exception.getMessage(), exception);
    }

    @Override
    public Logger reportOnly() {
        return reportOnly;
    }

    private class ReportLogger implements Logger {

        @Override
        public void info(String message) {
            infoConsumer.accept(message);
        }

        @Override
        public void error(String message) {
            errorConsumer.accept(message);
        }

        @Override
        public void error(Throwable exception) {
            errorConsumer.accept(exception.getMessage());
        }

        @Override
        public Logger reportOnly() {
            return this;
        }
    }
}
