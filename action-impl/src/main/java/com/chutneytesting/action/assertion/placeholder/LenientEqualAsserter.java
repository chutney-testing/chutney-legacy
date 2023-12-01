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

import static com.chutneytesting.action.common.JsonUtils.lenientEqual;

import com.chutneytesting.action.spi.injectable.Logger;
import com.jayway.jsonpath.JsonPath;

public class LenientEqualAsserter implements PlaceholderAsserter {

    private static final String IS_LENIENT_EQUAL = "$lenientEqual:";

    @Override
    public boolean canApply(String value) {
        return value.startsWith(IS_LENIENT_EQUAL);
    }

    @Override
    public boolean assertValue(Logger logger, Object actual, Object expected) {
        String expect = expected.toString().substring(IS_LENIENT_EQUAL.length());
        Object expectedRead = JsonPath.parse(expect).read("$");
        return lenientEqual(actual, expectedRead, true);
    }
}
