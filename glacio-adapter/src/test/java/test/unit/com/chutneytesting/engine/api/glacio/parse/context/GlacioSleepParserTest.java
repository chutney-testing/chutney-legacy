package test.unit.com.chutneytesting.engine.api.glacio.parse.context;

import static org.apache.commons.lang3.StringUtils.capitalize;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;
import static test.unit.com.chutneytesting.engine.api.glacio.parse.GlacioParserHelper.buildSimpleStepWithText;
import static test.unit.com.chutneytesting.engine.api.glacio.parse.GlacioParserHelper.loopOverRandomString;

import com.chutneytesting.engine.api.glacio.parse.context.GlacioSleepParser;
import java.util.Locale;
import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(JUnitParamsRunner.class)
public class GlacioSleepParserTest {

    private GlacioSleepParser sut = new GlacioSleepParser();

    @Test
    @Parameters({
        "sleep",
        "await",
        "wait",
        "stop",
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
            assertThat(
                sut.parseStep(buildSimpleStepWithText("sleep for " + randomString)).inputs)
                .containsExactly(entry("duration", randomString))
        );
    }
}
