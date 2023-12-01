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

import static java.util.Optional.ofNullable;

import com.chutneytesting.action.spi.injectable.Logger;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minidev.json.JSONArray;

public class ValueArrayAsserter implements PlaceholderAsserter {

    private static final Pattern IS_VALUE = Pattern.compile("^\\$value(\\[(?<index>[0-9]+)])?:(?<expected>.+)$");
    private static final Predicate<String> IS_VALUE_TEST = IS_VALUE.asMatchPredicate();

    @Override
    public boolean canApply(String value) {
        return IS_VALUE_TEST.test(value);
    }

    @Override
    public boolean assertValue(Logger logger, Object actual, Object expected) {
        if (actual instanceof JSONArray) {
            Matcher matcher = IS_VALUE.matcher(expected.toString());
            if (matcher.matches()) {
                JSONArray actualArray = (JSONArray) actual;
                AtomicInteger arrayIndex = new AtomicInteger(0);
                ofNullable(matcher.group("index")).ifPresent(s -> arrayIndex.set(Integer.parseInt(s)));
                String expect = matcher.group("expected");

                try {
                    String act = actualArray.get(arrayIndex.get()).toString();
                    logger.info("Verify " + expect + " = " + act);
                    return expect.equals(act);
                } catch (IndexOutOfBoundsException ioobe) {
                    logger.error("Index array is out of bound : " + ioobe);
                }
            } else {
                logger.error("Expected value don't match asserter pattern");
            }
        } else {
            logger.error("Actual value is not an array");
        }

        return false;
    }
}
