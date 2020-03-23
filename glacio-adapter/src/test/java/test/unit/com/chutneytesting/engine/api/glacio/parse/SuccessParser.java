package test.unit.com.chutneytesting.engine.api.glacio.parse;

import com.chutneytesting.engine.api.glacio.parse.GlacioParser;
import com.github.fridujo.glacio.ast.Step;
import java.util.function.Predicate;
import java.util.regex.Pattern;

public class SuccessParser extends GlacioParser {

    private final Pattern STEP_TEXT_PATTERN = Pattern.compile("^This step is always a success !!$");
    private final Predicate<String> STEP_TEXT_PREDICATE = STEP_TEXT_PATTERN.asPredicate();

    @Override
    public Integer priority() {
        return 1000;
    }

    @Override
    public boolean couldParse(Step step) {
        return STEP_TEXT_PREDICATE.test(step.getText());
    }

    @Override
    public String parseTaskType(Step step) {
        return "success";
    }
}
