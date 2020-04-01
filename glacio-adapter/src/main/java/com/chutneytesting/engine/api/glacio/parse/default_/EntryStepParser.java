package com.chutneytesting.engine.api.glacio.parse.default_;

import static java.util.Optional.empty;

import com.chutneytesting.engine.api.glacio.parse.StepParser;
import com.github.fridujo.glacio.ast.DataTable;
import com.github.fridujo.glacio.ast.DocString;
import com.github.fridujo.glacio.ast.Step;
import com.github.fridujo.glacio.ast.TableCell;
import java.util.AbstractMap.SimpleEntry;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.stream.Collectors;

public class EntryStepParser implements StepParser<Entry<String, Object>> {

    @Override
    public Entry<String, Object> parseStep(Step step) {
        String text = step.getText().trim();
        Optional<DataTable> dataTable = step.getDataTable();
        Optional<DocString> docString = step.getDocString();
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
                .map(tableRow -> tableRow.getCells().get(0).getValue())
                .collect(Collectors.toList());
        } else {
            return dataTable.getRows().stream()
                .collect(Collectors.toMap(
                    tableRow -> tableRow.getCells().get(0).getValue(),
                    tableRow -> {
                        if (tableRow.getCells().size() > 2) {
                            return tableRow.getCells().subList(1, tableRow.getCells().size()).stream()
                                .map(TableCell::getValue)
                                .collect(Collectors.toList());
                        } else if (tableRow.getCells().size() == 2) {
                            return tableRow.getCells().get(1).getValue();
                        }
                        return empty();
                    })
                );
        }
    }
}
