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

package com.chutneytesting.action.assertion.placeholder;

import com.chutneytesting.action.spi.injectable.Logger;
import net.minidev.json.JSONArray;

public class IsEmptyAsserter implements PlaceholderAsserter {

    private static final String IS_EMPTY = "$isEmpty";

    @Override
    public boolean canApply(String value) {
        return IS_EMPTY.equals(value);
    }

    @Override
    public boolean assertValue(Logger logger, Object actual, Object expected) {
        return assertValue(logger, actual);
    }

    public boolean assertValue(Logger logger, Object actual) {
        logger.info("Verify " + actual + " is empty");
        if (actual instanceof JSONArray jsonArray) {
            return jsonArray.stream().map(e -> assertValue(logger, e)).reduce((a, b) -> a && b).orElse(true);
        } else {
            return actual.toString().isEmpty();
        }
    }
}
