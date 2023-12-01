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

package com.chutneytesting.action.assertion.compare;

public class CompareActionFactory {

    private CompareActionFactory() {}

    public static CompareExecutor createCompareAction(String mode) {
        if ("equals".equalsIgnoreCase(mode)) {
            return new CompareEqualsAction();
        }
        if (isEquals(mode, "not-equals", "not equals")) {
            return new CompareNotEqualsAction();
        }
        if ("contains".equalsIgnoreCase(mode)) {
            return new CompareContainsAction();
        }
        if (isEquals(mode, "not-contains", "not contains")) {
            return new CompareNotContainsAction();
        }
        if (isEquals(mode, "greater-than", "greater than")) {
            return new CompareGreaterThanAction();
        }
        if (isEquals(mode, "less-than", "less than")) {
            return new CompareLessThanAction();
        }
        return new NoCompareAction();
    }

    private static boolean isEquals(String mode, String s, String s2) {
        return s.equalsIgnoreCase(mode) || s2.equalsIgnoreCase(mode);
    }
}
