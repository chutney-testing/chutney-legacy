package com.chutneytesting.engine.api.glacio;

import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;
import static java.util.Optional.ofNullable;

import com.chutneytesting.engine.domain.execution.StepDefinition;
import com.github.fridujo.glacio.ast.Step;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.springframework.stereotype.Component;

@Component
public
class ExecutableStepFactory {

    public final static String EXECUTABLE_KEYWORD_DO = "Do";
    public final static String EXECUTABLE_KEYWORD_EXECUTE = "Execute";
    private final static Pattern EXECUTABLE_STEP_TEXT_PATTERN = Pattern.compile("^("+EXECUTABLE_KEYWORD_DO+"|"+EXECUTABLE_KEYWORD_EXECUTE+") (?<task>\\(.*\\) )?(?<text>.*)$");

    private final static Predicate<String> EXECUTABLE_STEP_TEXT_PREDICATE = EXECUTABLE_STEP_TEXT_PATTERN.asPredicate();

    public ExecutableStepFactory() {
    }

    public StepDefinition build(Step step) {
        Matcher matcher = EXECUTABLE_STEP_TEXT_PATTERN.matcher(step.getText());
        while(matcher.find()) {
            if (matcher.groupCount() == 3) {
                Optional<String> taskgroup = ofNullable(matcher.group("task"));
                Optional<String> textGroup = ofNullable(matcher.group("text"));

                // If no task hint, suppose text is the task id
                String stepName = textGroup.orElse("");
                String taskId = taskgroup.map(this::extractTaskId).orElse(stepName);

                return new StepDefinition(stepName, null, taskId, null, emptyMap(), emptyList(), emptyMap());
            }
        }
        throw new IllegalArgumentException("Step is not executable : " + step);
    }

    public boolean isExecutableStep(Step step) {
        return EXECUTABLE_STEP_TEXT_PREDICATE.test(step.getText());
    }

    private String extractTaskId(String taskGroup) {
        String withoutFirstChar = taskGroup.substring(1);
        return withoutFirstChar.substring(0, withoutFirstChar.length() - 2);
    }
}
