package com.chutneytesting.engine.api.glacio.parse.default_;

import static java.util.Optional.ofNullable;

import com.chutneytesting.engine.api.glacio.parse.GlacioParser;
import com.chutneytesting.task.domain.TaskTemplateRegistry;
import com.github.fridujo.glacio.ast.Step;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DefaultGlacioParser extends GlacioParser {

    private final static Pattern STEP_TEXT_PATTERN = Pattern.compile("^(?<task>\\(.*\\) )?(?<text>.*)$");

    private final TaskTemplateRegistry taskTemplateRegistry;

    public DefaultGlacioParser(TaskTemplateRegistry taskTemplateRegistry) {
        this.targetParser  = new DefaultTargetParser();
        this.inputsParser = new DefaultInputsParser();
        this.outputsParser = new DefaultOutputsParser();
        this.strategyParser = EmptyParser.instance;
        this.taskTemplateRegistry = taskTemplateRegistry;
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

    private String extractTaskId(String taskGroup) {
        String withoutFirstChar = taskGroup.substring(1);
        return withoutFirstChar.substring(0, withoutFirstChar.length() - 2);
    }

    @Override
    public Map<Locale, Set<String>> keywords() {
        throw new UnsupportedOperationException();
    }

}
