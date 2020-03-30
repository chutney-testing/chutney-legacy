package com.chutneytesting.engine.api.glacio.parse.default_;

import static java.util.Optional.empty;

import com.chutneytesting.engine.api.glacio.parse.InputsParser;
import com.github.fridujo.glacio.ast.DataTable;
import com.github.fridujo.glacio.ast.DocString;
import com.github.fridujo.glacio.ast.Step;
import com.github.fridujo.glacio.ast.TableCell;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import org.apache.commons.lang3.tuple.Pair;

class DefaultInputsParser implements InputsParser {

    @Override
    public Map<String, Object> parseTaskInputs(Step step) {
        return step.getSubsteps().stream()
            .filter(substep -> substep.getText().startsWith("With "))
            .map(substep -> {
                String text = substep.getText().substring(5).trim();
                Optional<DataTable> dataTable = substep.getDataTable();
                Optional<DocString> docString = substep.getDocString();
                if (dataTable.isPresent()) {
                    Object inputValue = parseDataTableForInputs(dataTable.get());
                    return Pair.of(text, inputValue);
                } else if (docString.isPresent()) {
                    return Pair.of(text, docString.get().getContent());
                } else {
                    int spaceIdx = text.indexOf(" ");
                    if (spaceIdx > 0) {
                        return Pair.of(text.substring(0, spaceIdx), text.substring(spaceIdx + 1));
                    } else {
                        return Pair.of(text, "");
                    }
                }
            })
            .collect(Collectors.toMap(Pair::getLeft, Pair::getRight));
    }

    private Object parseDataTableForInputs(DataTable dataTable) {
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
