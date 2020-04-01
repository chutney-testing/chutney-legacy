package test.unit.com.chutneytesting.engine.api.glacio.parse.default_;

import static org.assertj.core.api.Assertions.assertThat;
import static test.unit.com.chutneytesting.engine.api.glacio.parse.GlacioParserHelper.buildSimpleStepWithText;

import com.chutneytesting.engine.api.glacio.parse.default_.EmptyParser;
import com.github.fridujo.glacio.ast.Step;
import org.junit.Test;

public class EmptyParserTest {

    private Step step = buildSimpleStepWithText("");

    @Test
    public void should_give_static_access_to_empty_map_step_parser() {
        assertThat(EmptyParser.emptyMapParser.parseStep(step)).isEmpty();
    }

    @Test
    public void should_give_static_access_to_no_target_step_parser() {
        assertThat(EmptyParser.noTargetParser.parseStep(step)).isNull();
    }

    @Test
    public void should_give_static_access_to_no_strategy_step_parser() {
        assertThat(EmptyParser.noStrategyParser.parseStep(step)).isNull();
    }
}
