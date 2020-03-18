package test.unit.com.chutneytesting.engine.api.glacio.parse;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;
import static test.unit.com.chutneytesting.engine.api.glacio.parse.GlacioParserHelper.buildSimpleStepWithText;
import static test.unit.com.chutneytesting.engine.api.glacio.parse.GlacioParserHelper.loopOverRandomString;

import com.chutneytesting.engine.api.glacio.parse.GlacioSleepParser;
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
        "sleep for",
        "sleep during",
        "await",
        "wait",
        "stop",
        "rest",
        "stand by"
    })
    public void should_parse_some_step_text(String stepText) {
        loopOverRandomString(4, 10, 100, (randomString) ->
            assertThat(sut.couldParse(stepText + " " + randomString)).isTrue()
        );
    }

    @Test
    public void should_parse_only_sleep_task() {
        String sleepTaskType = "sleep";
        loopOverRandomString(10, 30, 30, (randomString) ->
                assertThat(sut.parseTaskType(buildSimpleStepWithText(randomString)))
                    .isEqualTo(sleepTaskType)
        );
    }

    @Test
    public void should_parse_duration_input_from_step_text() {
        loopOverRandomString(4, 10, 100, (randomString) ->
            assertThat(sut.parseTaskInputs(buildSimpleStepWithText("sleep for " + randomString)))
                .containsExactly(entry("duration", randomString))
        );
    }
}
