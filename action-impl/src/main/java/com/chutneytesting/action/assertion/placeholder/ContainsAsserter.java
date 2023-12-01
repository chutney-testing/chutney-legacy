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

public class ContainsAsserter implements PlaceholderAsserter {

    private static final String CONTAINS = "$contains:";

    @Override
    public boolean canApply(String value) {
        return value.startsWith(CONTAINS);
    }

    @Override
    public boolean assertValue(Logger logger, Object actual, Object expected) {
        String toFind = expected.toString().substring(CONTAINS.length());
        logger.info("Verify " + actual.toString() + " contains " + toFind);
        return actual.toString().contains(toFind);
    }

}
