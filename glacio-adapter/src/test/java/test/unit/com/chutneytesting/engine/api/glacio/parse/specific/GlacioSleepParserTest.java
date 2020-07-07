package test.unit.com.chutneytesting.engine.api.glacio.parse.specific;

import static org.apache.commons.lang3.StringUtils.capitalize;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;
import static test.unit.com.chutneytesting.engine.api.glacio.parse.GlacioParserHelper.buildSimpleStepWithText;
import static test.unit.com.chutneytesting.engine.api.glacio.parse.GlacioParserHelper.loopOverRandomString;

import com.chutneytesting.engine.api.glacio.parse.default_.EmptyParser;
import com.chutneytesting.engine.api.glacio.parse.specific.GlacioSleepParser;
import com.chutneytesting.engine.domain.execution.strategies.StepStrategyDefinition;
import java.util.Locale;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

public class GlacioSleepParserTest {

    private static final String ENVIRONMENT = "ENV";
    private static final StepStrategyDefinition NO_STRATEGY_DEF = null;

    private GlacioSleepParser sut = new GlacioSleepParser();

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
    public void should_parse_only_sleep_task() {
        String sleepTaskType = "sleep";
        loopOverRandomString(10, 30, 30, (randomString) ->
            assertThat(
                sut.parseTaskType(buildSimpleStepWithText(randomString)))
                .isEqualTo(sleepTaskType)
        );
    }

    @Test
    public void should_parse_duration_input_from_step_text() {
        loopOverRandomString(4, 10, 100, (randomString) ->
            assertThat(sut
                .mapToStepDefinition(ENVIRONMENT, buildSimpleStepWithText("sleep for " + randomString), NO_STRATEGY_DEF)
                .inputs).containsExactly(entry("duration", randomString))
        );
    }
}
