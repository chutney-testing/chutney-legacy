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

import java.util.function.BiFunction;

public class CompareLessThanAction extends AbstractCompareNumberAction {

    @Override
    protected BiFunction<Double, Double, Boolean> compareFunction() {
        return (d1, d2) -> d1 < d2;
    }

    @Override
    protected String getFunctionName() {
        return "IS LESS THAN";
    }

    @Override
    protected String getOppositeFunctionName() {
        return "IS GREATER THAN";
    }
}
