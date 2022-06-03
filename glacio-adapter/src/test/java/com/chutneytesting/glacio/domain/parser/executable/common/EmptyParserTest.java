package com.chutneytesting.glacio.domain.parser.executable.common;

import static com.chutneytesting.glacio.domain.parser.GlacioParserHelper.buildSimpleStepWithText;
import static org.assertj.core.api.Assertions.assertThat;

import com.chutneytesting.glacio.domain.parser.ParsingContext;
import com.github.fridujo.glacio.model.Step;
import org.junit.jupiter.api.Test;

public class EmptyParserTest {

    private static final ParsingContext parsingContext = new ParsingContext();
    private final Step step = buildSimpleStepWithText("");

    @Test
    public void should_give_static_access_to_empty_map_step_parser() {
        assertThat(EmptyParser.emptyMapParser.parseGlacioStep(parsingContext, step)).isEmpty();
    }

    @Test
    public void should_give_static_access_to_no_target_step_parser() {
        assertThat(EmptyParser.noTargetParser.parseGlacioStep(parsingContext, step)).isNull();
    }

    @Test
    public void should_give_static_access_to_no_strategy_step_parser() {
        assertThat(EmptyParser.noStrategyParser.parseGlacioStep(step)).isEmpty();
    }
}
