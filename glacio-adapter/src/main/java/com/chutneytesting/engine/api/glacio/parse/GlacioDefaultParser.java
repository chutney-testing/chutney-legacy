package com.chutneytesting.engine.api.glacio.parse;

import static java.util.Optional.empty;
import static java.util.Optional.ofNullable;

import com.chutneytesting.engine.domain.environment.ImmutableTarget;
import com.chutneytesting.engine.domain.environment.Target;
import com.chutneytesting.task.domain.TaskTemplateRegistry;
import com.github.fridujo.glacio.ast.DataTable;
import com.github.fridujo.glacio.ast.DocString;
import com.github.fridujo.glacio.ast.Step;
import com.github.fridujo.glacio.ast.TableCell;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.apache.commons.lang3.tuple.Pair;

public class GlacioDefaultParser extends GlacioParser {

    private final static Pattern STEP_TEXT_PATTERN = Pattern.compile("^(?<task>\\(.*\\) )?(?<text>.*)$");
    private final static Predicate<String> STEP_TEXT_PREDICATE = STEP_TEXT_PATTERN.asPredicate();

    private final TaskTemplateRegistry taskTemplateRegistry;

    public GlacioDefaultParser(TaskTemplateRegistry taskTemplateRegistry) {
        this.taskTemplateRegistry = taskTemplateRegistry;
    }

    @Override
    public Integer priority() {
        return Integer.MAX_VALUE;
    }

    @Override
    public boolean couldParse(Step step) {
        return STEP_TEXT_PREDICATE.test(step.getText());
    }

    @Override
    public String parseTaskType(Step step) {
        Matcher matcher = STEP_TEXT_PATTERN.matcher(step.getText());
        if (matcher.matches()) {
            return ofNullable(matcher.group("task"))
                .map(this::extractTaskId)
                .filter(taskId -> this.taskTemplateRegistry.getByIdentifier(taskId).isPresent())
                .orElseGet(() ->
                    ofNullable(matcher.group("text"))
                        .filter(taskId -> this.taskTemplateRegistry.getByIdentifier(taskId).isPresent())
                        .orElseThrow(() -> new IllegalArgumentException("Cannot identify task from step text : " + step.getText())));
        }
        throw new IllegalArgumentException("Cannot extract task type from step text : "+step.getText());
    }

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

    @Override
    public Map<String, Object> parseTaskOutputs(Step step) {
        return step.getSubsteps().stream()
            .filter(substep -> substep.getText().matches("^(?!On .*).*$"))
            .filter(substep -> substep.getText().matches("^(?!With .*).*$"))
            .map(Step::getText)
            .map(text -> {
                int spaceIdx = text.indexOf(" ");
                return Pair.of(
                    text.substring(0, spaceIdx),
                    text.substring(spaceIdx)
                );
            })
            .collect(Collectors.toMap(Pair::getLeft, Pair::getRight));
    }

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

    private String extractTaskId(String taskGroup) {
        String withoutFirstChar = taskGroup.substring(1);
        return withoutFirstChar.substring(0, withoutFirstChar.length() - 2);
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

    private Map<String, String> dataTableToSimpleMap(DataTable dataTable) {
        return dataTable.getRows().stream()
            .collect(Collectors.toMap(
                tableRow -> tableRow.getCells().get(0).getValue(),
                tableRow -> tableRow.getCells().get(1).getValue()
            ));
    }
}
