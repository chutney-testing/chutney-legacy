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

package com.chutneytesting.glacio.domain.parser.executable.common;

import static java.util.Optional.empty;

import com.chutneytesting.glacio.domain.parser.GlacioStepParser;
import com.chutneytesting.glacio.domain.parser.ParsingContext;
import com.chutneytesting.glacio.domain.parser.util.ParserHelper;
import com.github.fridujo.glacio.model.DataTable;
import com.github.fridujo.glacio.model.DocString;
import com.github.fridujo.glacio.model.Step;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.stream.Collectors;

public class EntryStepParser implements GlacioStepParser<Entry<String, Object>> {

    @Override
    public Entry<String, Object> parseGlacioStep(ParsingContext context, Step step) {
        String text = step.getText().trim();
        Optional<DataTable> dataTable = ParserHelper.stepDataTable(step);
        Optional<DocString> docString = ParserHelper.stepDocString(step);
        if (dataTable.isPresent()) {
            Object inputValue = parseDataTable(dataTable.get());
            return new SimpleEntry<>(text, inputValue);
        } else if (docString.isPresent()) {
            return new SimpleEntry<>(text, docString.get().getContent());
        } else {
            int spaceIdx = text.indexOf(" ");
            if (spaceIdx > -1) {
                return new SimpleEntry<>(text.substring(0, spaceIdx), text.substring(spaceIdx + 1));
            } else {
                return new SimpleEntry<>(text, "");
            }
        }
    }

    private Object parseDataTable(DataTable dataTable) {
        if (dataTable.getRows().get(0).getCells().size() == 1) {
            return dataTable.getRows().stream()
                .map(tableRow -> tableRow.getCells().get(0))
                .collect(Collectors.toList());
        } else {
            return dataTable.getRows().stream()
                .collect(Collectors.toMap(
                    tableRow -> tableRow.getCells().get(0),
                    tableRow -> {
                        if (tableRow.getCells().size() > 2) {
                            return new ArrayList<>(tableRow.getCells().subList(1, tableRow.getCells().size()));
                        } else if (tableRow.getCells().size() == 2) {
                            return tableRow.getCells().get(1);
                        }
                        return empty();
                    })
                );
        }
    }
}
