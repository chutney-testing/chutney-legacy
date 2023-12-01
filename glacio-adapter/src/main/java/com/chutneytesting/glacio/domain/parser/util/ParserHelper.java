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

package com.chutneytesting.glacio.domain.parser.util;

import com.github.fridujo.glacio.model.DataTable;
import com.github.fridujo.glacio.model.DocString;
import com.github.fridujo.glacio.model.Step;
import com.github.fridujo.glacio.model.StepArgument;
import java.util.Optional;

public class ParserHelper {

    private ParserHelper() {}

    public static Optional<DataTable> stepDataTable(Step step) {
        return step.getArgument().map(ParserHelper::stepArgumentDataTable);
    }

    public static Optional<DocString> stepDocString(Step step) {
        return step.getArgument().map(ParserHelper::stepArgumentDocString);
    }

    private static DataTable stepArgumentDataTable(StepArgument stepArgument) {
        if (StepArgument.Type.DATA_TABLE.equals(stepArgument.getType())) {
            return ((DataTable) stepArgument);
        }
        return null;
    }

    private static DocString stepArgumentDocString(StepArgument stepArgument) {
        if (StepArgument.Type.DOC_STRING.equals(stepArgument.getType())) {
            return ((DocString) stepArgument);
        }
        return null;
    }
}
