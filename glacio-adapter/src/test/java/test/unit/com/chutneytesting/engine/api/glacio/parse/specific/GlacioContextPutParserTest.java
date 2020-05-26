package test.unit.com.chutneytesting.engine.api.glacio.parse.specific;

import static org.apache.commons.lang3.StringUtils.capitalize;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;
import static test.unit.com.chutneytesting.engine.api.glacio.parse.GlacioParserHelper.buildDataTableStepWithText;
import static test.unit.com.chutneytesting.engine.api.glacio.parse.GlacioParserHelper.buildSimpleStepWithText;
import static test.unit.com.chutneytesting.engine.api.glacio.parse.GlacioParserHelper.buildSubStepsStepWithText;
import static test.unit.com.chutneytesting.engine.api.glacio.parse.GlacioParserHelper.loopOverRandomString;

import com.chutneytesting.engine.api.glacio.parse.specific.GlacioContextPutParser;
import java.util.Locale;
import org.apache.groovy.util.Maps;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

public class GlacioContextPutParserTest {

    private static final String ENVIRONMENT = "ENV";

    private GlacioContextPutParser sut = new GlacioContextPutParser();

    @ParameterizedTest
    @ValueSource(strings = {
        "add",
        "put",
        "store"
    })
    public void keywords(String keyword) {
        assertThat(sut.keywords().get(Locale.ENGLISH)).contains(keyword);
        assertThat(sut.keywords().get(Locale.ENGLISH)).contains(capitalize(keyword));
    }

    @Test
    public void should_parse_only_context_put_task() {
        String sleepTaskType = "context-put";
        loopOverRandomString(10, 30, 30, (randomString) ->
            assertThat(
                sut.parseTaskType(buildSimpleStepWithText(randomString)))
                .isEqualTo(sleepTaskType)
        );
    }

    @Test
    public void should_parse_entries_input_from_step_datatable() {
        String dataTableString = "| var1 | val ue1 |\n| var 2 | value2 |";
        assertThat(
            sut.mapToStepDefinition(ENVIRONMENT, buildDataTableStepWithText("add variables", dataTableString)).inputs)
            .containsExactly(entry("entries", Maps.of("var1", "val ue1", "var 2", "value2")));
    }

    @Test
    public void should_parse_entries_input_from_step_substeps_without_spaces() {
        String subStepsString = "var1 value1\nvar2 value2";
        assertThat(
            sut.mapToStepDefinition(ENVIRONMENT, buildSubStepsStepWithText("add variables", subStepsString)).inputs)
            .containsExactly(entry("entries", Maps.of("var1", "value1", "var2", "value2")));
    }

    @Test
    public void should_parse_entries_input_from_step_substeps_with_spaces() {
        String subStepsString = "var1 val ue1\n\"var 2\" value2";
        assertThat(
            sut.mapToStepDefinition(ENVIRONMENT, buildSubStepsStepWithText("add variables", subStepsString)).inputs)
            .containsExactly(entry("entries", Maps.of("var1", "val ue1", "var 2", "value2")));
    }
}
