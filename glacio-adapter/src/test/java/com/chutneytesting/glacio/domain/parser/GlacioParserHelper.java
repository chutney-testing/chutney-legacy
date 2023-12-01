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

package com.chutneytesting.glacio.domain.parser;

import static java.util.Collections.emptyList;
import static java.util.Optional.empty;
import static java.util.Optional.of;

import com.github.fridujo.glacio.model.DataTable;
import com.github.fridujo.glacio.model.Step;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.IntStream;
import org.apache.commons.lang3.RandomStringUtils;

public final class GlacioParserHelper {

    public static void loopOverRandomString(int stepTextMinLength, int stepTextMaxLength, int randomLoopMax, Consumer<String> assertToRun) {
        IntStream.range(stepTextMinLength, stepTextMaxLength)
            .forEach(length -> IntStream.range(1, randomLoopMax)
                .forEach(i ->
                    assertToRun.accept(RandomStringUtils.random(length, true, true))
                )
            );
    }

    public static Step buildSimpleStepWithText(String stepText) {
        return new Step(false, empty(), stepText, empty(), emptyList());
    }

    public static Step buildDataTableStepWithText(String stepText, String dataTableString) {
        return new Step(false, empty(), stepText, of(buildDataTableFromString(dataTableString)), emptyList());
    }

    public static Step buildSubStepsStepWithText(String stepText, String subStepsString) {
        return new Step(false, empty(), stepText, empty(), buildSimpleSubStepsFromString(subStepsString));
    }

    public static DataTable buildDataTableFromString(String dataTableString) {
        List<DataTable.Row> rows = new ArrayList<>();
        String[] lines = dataTableString.split("\n");
        for (String line : lines) {
            List<String> cells = new ArrayList<>();
            for (String value : line.split("\\|")) {
                if (value.length() > 0) {
                    cells.add(value.trim());
                }
            }
            rows.add(new DataTable.Row(cells));
        }
        return new DataTable(rows);
    }

    public static List<Step> buildSimpleSubStepsFromString(String subStepsString) {
        List<Step> subSteps = new ArrayList<>();
        for (String line : subStepsString.split("\n")) {
            subSteps.add(buildSimpleStepWithText(line));
        }
        return subSteps;
    }
}
