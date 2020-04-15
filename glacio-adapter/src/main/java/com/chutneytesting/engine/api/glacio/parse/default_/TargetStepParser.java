package com.chutneytesting.engine.api.glacio.parse.default_;

import static com.chutneytesting.engine.api.glacio.parse.default_.ParsingTools.arrayToOrPattern;
import static com.chutneytesting.engine.api.glacio.parse.default_.ParsingTools.removeKeyword;

import com.chutneytesting.engine.api.glacio.parse.StepParser;
import com.chutneytesting.engine.domain.environment.ImmutableTarget;
import com.chutneytesting.engine.domain.environment.Target;
import com.github.fridujo.glacio.ast.Step;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.regex.Pattern;

public class TargetStepParser implements StepParser<Optional<Target>> {

    private final Pattern startWithPredicate;
    private final Predicate<String> predicate;

    public TargetStepParser(String... startingWords) {
        this.startWithPredicate = Pattern.compile("^(?<keyword>" + arrayToOrPattern(startingWords) + ")(?: .*)$");
        this.predicate = startWithPredicate.asPredicate();
    }

    @Override
    public Optional<Target> parseStep(Step step) {
        return step.getSubsteps().stream()
            .filter(substep -> {
                System.out.println(substep);
              return  predicate.test(substep.getText());
            })
            .map(s -> removeKeyword(startWithPredicate, s))
            .map(this::parseTargetStep)
            .findFirst();
    }

    private Target parseTargetStep(Step step) {
        String targetName = step.getText().trim();
        // Todo map with target files.
        return ImmutableTarget.builder()
            .id(ImmutableTarget.TargetId.of(targetName))
            .url("fakeurl")
            .build();
    }

}
