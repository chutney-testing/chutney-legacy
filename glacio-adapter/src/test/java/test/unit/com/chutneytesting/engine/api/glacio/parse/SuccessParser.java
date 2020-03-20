package test.unit.com.chutneytesting.engine.api.glacio.parse;

import com.chutneytesting.engine.api.glacio.parse.GlacioExecutableStepParser;
import com.github.fridujo.glacio.ast.Step;
import java.util.function.Predicate;
import java.util.regex.Pattern;

public class SuccessParser implements GlacioExecutableStepParser {

    private final Pattern STEP_TEXT_PATTERN = Pattern.compile("^This step is always a success !!$");
    private final Predicate<String> STEP_TEXT_PREDICATE = STEP_TEXT_PATTERN.asPredicate();

    @Override
    public Integer priority() {
        return 1000;
    }

    @Override
    public boolean couldParse(String stepText) {
        return STEP_TEXT_PREDICATE.test(stepText);
    }

    @Override
    public String parseTaskType(Step step) {
        return "success";
    }
}
