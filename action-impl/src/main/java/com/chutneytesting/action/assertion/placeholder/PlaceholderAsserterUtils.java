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

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class PlaceholderAsserterUtils {

    private static final List<PlaceholderAsserter> asserters = new ArrayList<>();

    static {
        asserters.add(new IsNullAsserter());
        asserters.add(new NotNullAsserter());
        asserters.add(new ContainsAsserter());
        asserters.add(new BeforeDateAsserter());
        asserters.add(new AfterDateAsserter());
        asserters.add(new EqualDateAsserter());
        asserters.add(new MatchesStringAsserter());
        asserters.add(new LessThanAsserter());
        asserters.add(new GreaterThanAsserter());
        asserters.add(new ValueArrayAsserter());
        asserters.add(new IsEmptyAsserter());
        asserters.add(new LenientEqualAsserter());
    }

    public static Optional<PlaceholderAsserter> getAsserterMatching(Object toMatch) {
        if (toMatch == null) {
            return Optional.of(new IsNullAsserter());
        }
        return asserters.stream().filter(a -> a.canApply(toMatch.toString())).findFirst();
    }
}
