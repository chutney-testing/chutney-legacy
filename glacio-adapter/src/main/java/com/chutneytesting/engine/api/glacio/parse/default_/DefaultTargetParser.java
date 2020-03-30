package com.chutneytesting.engine.api.glacio.parse.default_;

import com.chutneytesting.engine.api.glacio.parse.TargetParser;
import com.chutneytesting.engine.domain.environment.ImmutableTarget;
import com.chutneytesting.engine.domain.environment.Target;
import com.github.fridujo.glacio.ast.DataTable;
import com.github.fridujo.glacio.ast.Step;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

class DefaultTargetParser implements TargetParser {
    @Override
    public Target parseStepTarget(Step step) {
        return step.getSubsteps().stream()
            .filter(substep -> substep.getText().startsWith("On "))
            .findFirst()
            .map(substep -> {
                String text = substep.getText().substring(3);
                Optional<DataTable> dataTable = substep.getDataTable();
                int spaceIdx = text.indexOf(" ");
                return ImmutableTarget.builder()
                    .id(ImmutableTarget.TargetId.of(text.substring(0, spaceIdx)))
                    .url(text.substring(spaceIdx + 1))
                    .properties(dataTable.map(this::dataTableToSimpleMap).orElseGet(Collections::emptyMap))
                    .build();
            })
            .orElse(null);
    }

    private Map<String, String> dataTableToSimpleMap(DataTable dataTable) {
        return dataTable.getRows().stream()
            .collect(Collectors.toMap(
                tableRow -> tableRow.getCells().get(0).getValue(),
                tableRow -> tableRow.getCells().get(1).getValue()
            ));
    }
}
