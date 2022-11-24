package com.chutneytesting.glacio.domain.parser.executable.specific;

import static com.chutneytesting.glacio.domain.parser.GlacioParserHelper.buildSimpleStepWithText;
import static com.chutneytesting.glacio.domain.parser.GlacioParserHelper.loopOverRandomString;
import static com.chutneytesting.glacio.domain.parser.ParsingContext.PARSING_CONTEXT_KEYS.ENVIRONMENT;
import static org.apache.commons.lang3.StringUtils.capitalize;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;

import com.chutneytesting.engine.api.execution.StepDefinitionDto;
import com.chutneytesting.glacio.domain.parser.ParsingContext;
import java.util.Locale;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

public class GlacioSleepParserTest {

    private static final ParsingContext CONTEXT = new ParsingContext();

    private static final StepDefinitionDto.StepStrategyDefinitionDto NO_STRATEGY_DEF = null;

    private final GlacioSleepParser sut = new GlacioSleepParser();

    @BeforeAll
    public static void setUp() {
        CONTEXT.values.put(ENVIRONMENT, "ENV");
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "sleep",
        "await",
        "wait",
        "rest"
    })
    public void english_keywords(String keyword) {
        assertThat(sut.keywords().get(Locale.ENGLISH)).contains(keyword);
        assertThat(sut.keywords().get(Locale.ENGLISH)).contains(capitalize(keyword));
    }

    @Test
    public void should_parse_only_sleep_action() {
        String sleepActionType = "sleep";
        loopOverRandomString(10, 30, 30, (randomString) ->
            assertThat(
                sut.parseActionType(buildSimpleStepWithText(randomString)))
                .isEqualTo(sleepActionType)
        );
    }

    @Test
    public void should_parse_duration_input_from_step_text() {
        loopOverRandomString(4, 10, 100, (randomString) ->
            assertThat(sut
                .mapToStepDefinition(CONTEXT, buildSimpleStepWithText("sleep for " + randomString), NO_STRATEGY_DEF)
                .inputs).containsExactly(entry("duration", randomString))
        );
    }
}
