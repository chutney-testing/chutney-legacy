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
import java.text.NumberFormat;
import java.text.ParseException;

public class GreaterThanAsserter implements PlaceholderAsserter {

    private static final String IS_GREATER_THAN = "$isGreaterThan:";
    private static final NumberFormat nb = NumberFormat.getInstance();

    @Override
    public boolean canApply(String value) {
        return value.startsWith(IS_GREATER_THAN);
    }

    @Override
    public boolean assertValue(Logger logger, Object actual, Object expected) {
        String expect = expected.toString().substring(IS_GREATER_THAN.length());
        try {
            Number numActual = nb.parse(actual.toString().replaceAll(" ", ""));
            Number numExpected = nb.parse(expect.replaceAll(" ", ""));
            logger.info("Verify " + numActual.doubleValue() + " > " + numExpected.doubleValue());
            return numActual.doubleValue() > numExpected.doubleValue();
        } catch (ParseException e) {
            logger.error(e.getMessage());
            return false;
        }
    }

}
