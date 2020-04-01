package com.chutneytesting.engine.api.glacio.parse.default_;

import com.chutneytesting.engine.api.glacio.parse.StepParser;
import com.chutneytesting.engine.domain.environment.ImmutableTarget;
import com.chutneytesting.engine.domain.environment.Target;
import com.github.fridujo.glacio.ast.DataTable;
import com.github.fridujo.glacio.ast.Step;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class TargetStepParser implements StepParser<Target> {

    @Override
    public Target parseStep(Step step) {
        String text = step.getText().trim();
        Optional<DataTable> dataTable = step.getDataTable();
        int spaceIdx = text.indexOf(" ");
        if (spaceIdx > -1) {
            return ImmutableTarget.builder()
                .id(ImmutableTarget.TargetId.of(text.substring(0, spaceIdx)))
                .url(text.substring(spaceIdx + 1))
                .properties(dataTable.map(this::dataTableToSimpleMap).orElseGet(Collections::emptyMap))
                .build();
        }
        throw new IllegalArgumentException("Target id and url cannot be extracted from step text : " + step.getText());
    }

    private Map<String, String> dataTableToSimpleMap(DataTable dataTable) {
        return dataTable.getRows().stream()
            .collect(Collectors.toMap(
                tableRow -> tableRow.getCells().get(0).getValue(),
                tableRow -> tableRow.getCells().get(1).getValue()
            ));
    }
}
