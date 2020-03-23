package com.chutneytesting.engine.api.glacio;

import static java.util.Optional.ofNullable;

import com.chutneytesting.engine.api.glacio.parse.GlacioExecutableStepParser;
import com.chutneytesting.engine.domain.execution.StepDefinition;
import com.github.fridujo.glacio.ast.Step;
import java.util.List;
import java.util.TreeSet;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class ExecutableStepFactory {

    private static final Logger LOGGER = LoggerFactory.getLogger(ExecutableStepFactory.class);

    public final static String EXECUTABLE_KEYWORD_DO = "Do";
    public final static String EXECUTABLE_KEYWORD_RUN = "Run";
    private final static Pattern EXECUTABLE_STEP_TEXT_PATTERN = Pattern.compile("^(" + EXECUTABLE_KEYWORD_DO + "|" + EXECUTABLE_KEYWORD_RUN + ") (?<text>.*)$");
    private final static Predicate<String> EXECUTABLE_STEP_TEXT_PREDICATE = EXECUTABLE_STEP_TEXT_PATTERN.asPredicate();

    private TreeSet<GlacioExecutableStepParser> glacioExecutableStepParsers;

    public ExecutableStepFactory(TreeSet<GlacioExecutableStepParser> glacioExecutableStepParsers) {
        this.glacioExecutableStepParsers = glacioExecutableStepParsers;
    }

    public StepDefinition build(Step step) {
        Matcher matcher = EXECUTABLE_STEP_TEXT_PATTERN.matcher(step.getText());
        if (matcher.matches()) {
            String stepText = ofNullable(matcher.group("text")).orElse("");
            return delegateStepParsing(cleanStepText(stepText, step));
        }
        throw new IllegalArgumentException("Step is not executable : " + step);
    }

    private StepDefinition delegateStepParsing(Step step) {
        List<GlacioExecutableStepParser> avalaibleParsers = this.glacioExecutableStepParsers.stream()
            .filter(parser -> parser.couldParse(step))
            .collect(Collectors.toList());

        for (GlacioExecutableStepParser parser : avalaibleParsers) {
            try {
                return parser.parseStep(step);
            } catch (Exception e) {
                LOGGER.warn("Error parsing step : {}", step, e);
            }
        }

        throw new IllegalArgumentException("No parsers to parse step : " + step);
    }

    public boolean isExecutableStep(Step step) {
        return EXECUTABLE_STEP_TEXT_PREDICATE.test(step.getText());
    }

    private Step cleanStepText(String stepText, Step step) {
        return new Step(step.getPosition(), stepText, step.getSubsteps(), step.getDocString(), step.getDataTable());
    }
}
