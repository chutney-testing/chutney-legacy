package com.chutneytesting.glacio.domain.parser.executable;

import static java.util.Optional.ofNullable;

import com.chutneytesting.environment.api.EnvironmentApi;
import com.chutneytesting.glacio.domain.parser.ExecutableGlacioStepParser;
import com.chutneytesting.glacio.domain.parser.executable.common.EntryStepParser;
import com.chutneytesting.glacio.domain.parser.executable.common.FilteredByKeywordsSubStepMapStepParser;
import com.chutneytesting.glacio.domain.parser.executable.common.TargetStepParser;
import com.chutneytesting.action.domain.ActionTemplateRegistry;
import com.github.fridujo.glacio.model.Step;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DefaultGlacioParser extends ExecutableGlacioStepParser {

    private final static Pattern STEP_TEXT_PATTERN = Pattern.compile("^(?<action>[a-zA-Z\\-_0-9]*)?\\s*(?<text>.*)$");

    private final ActionTemplateRegistry actionTemplateRegistry;

    public DefaultGlacioParser(ActionTemplateRegistry actionTemplateRegistry, EnvironmentApi environmentApplication) {
        super(new TargetStepParser(environmentApplication, "On"),
            new FilteredByKeywordsSubStepMapStepParser(new EntryStepParser(), "With"),
            new FilteredByKeywordsSubStepMapStepParser(new EntryStepParser(), "Take", "Keep"),
            new FilteredByKeywordsSubStepMapStepParser(new EntryStepParser(), "Validate"));
        this.actionTemplateRegistry = actionTemplateRegistry;
    }

    @Override
    public String parseActionType(Step step) {
        Matcher matcher = STEP_TEXT_PATTERN.matcher(step.getText());
        if (matcher.matches()) {
            return ofNullable(matcher.group("action"))
                .filter(actionId -> this.actionTemplateRegistry.getByIdentifier(actionId).isPresent())
                .orElseGet(() ->
                    ofNullable(matcher.group("text"))
                        .filter(actionId -> this.actionTemplateRegistry.getByIdentifier(actionId).isPresent())
                        .orElseThrow(() -> new IllegalArgumentException("Cannot identify action from step text : " + step.getText())));
        }
        throw new IllegalArgumentException("Cannot extract action type from step text : " + step.getText());
    }

    @Override
    public Map<Locale, Set<String>> keywords() {
        throw new UnsupportedOperationException();
    }
}
