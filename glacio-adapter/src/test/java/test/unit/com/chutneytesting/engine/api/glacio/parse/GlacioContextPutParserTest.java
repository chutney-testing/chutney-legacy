package test.unit.com.chutneytesting.engine.api.glacio.parse;

import static org.apache.commons.lang3.StringUtils.capitalize;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;
import static test.unit.com.chutneytesting.engine.api.glacio.parse.GlacioParserHelper.buildDataTableStepWithText;
import static test.unit.com.chutneytesting.engine.api.glacio.parse.GlacioParserHelper.buildSimpleStepWithText;
import static test.unit.com.chutneytesting.engine.api.glacio.parse.GlacioParserHelper.buildSubStepsStepWithText;
import static test.unit.com.chutneytesting.engine.api.glacio.parse.GlacioParserHelper.loopOverRandomString;

import com.chutneytesting.engine.api.glacio.parse.GlacioContextPutParser;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.stream.IntStream;
import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import org.apache.groovy.util.Maps;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(JUnitParamsRunner.class)
public class GlacioContextPutParserTest {

    private GlacioContextPutParser sut = new GlacioContextPutParser();

    @Test
    @Parameters({
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
    @Parameters({
        "var1 value1",
        "var1 value1 var2 value2",
        "var1 value1 var2 value2 var3 value3"
    })
    public void should_parse_entries_input_from_step_text_without_spaces(String entriesString) {
        int wordsCount = new StringTokenizer(entriesString).countTokens();
        Map<String, Object> expectedEntriesInput = buildExpectedEntriesInput(wordsCount / 2 + 1, "var", "value");

        assertThat(
            sut.parseTaskInputs(buildSimpleStepWithText("add " + entriesString)))
            .containsExactly(entry("entries", expectedEntriesInput));
    }

    @Test
    @Parameters({
        "\"var 1\" \"val ue1 \"",
        "\"var 1 \" \"val ue1\" \"var 2\" \"val ue2 \"",
        "\"var 1\" \"val ue1 \" \"var 2\" \"val ue2\" \"var 3 \" \"val ue3\""
    })
    public void should_parse_entries_input_from_step_text_with_spaces(String entriesString) {
        int wordsCount = new StringTokenizer(entriesString).countTokens();
        Map<String, Object> expectedEntriesInput = buildExpectedEntriesInput(wordsCount / 4 + 1, "var ", "val ue");

        assertThat(
            sut.parseTaskInputs(buildSimpleStepWithText("add " + entriesString)))
            .containsExactly(entry("entries", expectedEntriesInput));
    }

    @Test
    @Parameters(method = "dataTableParameters")
    public void should_parse_entries_input_from_step_datatable(String dataTableString) {
        int wordsCount = new StringTokenizer(dataTableString, "|").countTokens();
        Map<String, Object> expectedEntriesInput = buildExpectedEntriesInput(wordsCount / 2 + 1, "var ", "val ue");

        assertThat(
            sut.parseTaskInputs(buildDataTableStepWithText("add variables", dataTableString)))
            .containsExactly(entry("entries", expectedEntriesInput));
    }

    @Test
    @Parameters(method = "subStepsWithoutSpacesParameters")
    public void should_parse_entries_input_from_step_substeps_without_spaces(String subStepsString, Integer count) {
        Map<String, Object> expectedEntriesInput = buildExpectedEntriesInput(count + 1, "var", "value");

        assertThat(
            sut.parseTaskInputs(buildSubStepsStepWithText("add variables", subStepsString)))
            .containsExactly(entry("entries", expectedEntriesInput));
    }

    @Test
    @Parameters(method = "subStepsWithSpacesParameters")
    public void should_parse_entries_input_from_step_substeps_with_spaces(String subStepsString, Integer count) {
        Map<String, Object> expectedEntriesInput = buildExpectedEntriesInput(count + 1, "var ", "val ue");

        assertThat(
            sut.parseTaskInputs(buildSubStepsStepWithText("add variables", subStepsString)))
            .containsExactly(entry("entries", expectedEntriesInput));
    }

    @Test
    public void should_agregate_parse_entries_input_from_step() {
        Map<String, String> expectedEntriesInput = Maps.of("var1", "value1", "var 2", "value2", "va r3", "value3", "var4", "value4");

        // Agregate text and datatable
        assertThat(
            sut.parseTaskInputs(buildDataTableStepWithText("add variables var1 value1 \"var 2\" value2", "| va r3 | value3 | var4 | value4 |")))
            .containsExactly(entry("entries", expectedEntriesInput));

        // Agregate text and substeps
        assertThat(
            sut.parseTaskInputs(buildSubStepsStepWithText("add variables var1 value1 \"var 2\" value2", "\" va r3\" value3 var4 value4")))
            .containsExactly(entry("entries", expectedEntriesInput));
    }

    @SuppressWarnings("unused")
    private Object[] dataTableParameters() {
        return new Object[]{
            new Object[]{"| var 1 | val ue1 |"},
            new Object[]{"| var 1  | val ue1 | var 2 | val ue2 |"},
            new Object[]{"| var 1 | val ue1 | var 2  | val ue2 |\n| var 3 | val ue3 |"}
        };
    }

    @SuppressWarnings("unused")
    private Object[] subStepsWithoutSpacesParameters() {
        return new Object[]{
            new Object[]{
                "var1 value1", 1
            },
            new Object[]{
                "var1 value1" + "\n" +
                    "var2 value2 var3 value3", 3
            },
            new Object[]{
                "var1 value1 var2 value2" + "\n" +
                    "var3 value3" + "\n" +
                    "var4 value4 var5 value5 var6 value6", 6
            }
        };
    }

    @SuppressWarnings("unused")
    private Object[] subStepsWithSpacesParameters() {
        return new Object[]{
            new Object[]{
                "\"var 1\" \"val ue1\"", 1
            },
            new Object[]{
                "\"var 1 \" \" val ue1\"" + "\n" +
                    "\"var 2\" \"val ue2\" \" var 3\" \"val ue3\"", 3
            },
            new Object[]{
                "\"var 1\" \" val ue1\" \"var 2\" \"val ue2 \"" + "\n" +
                    "\"var 3\" \"val ue3\"" + "\n" +
                    "\"var 4\" \"val ue4 \" \"var 5\" \"val ue5\" \"var 6\" \" val ue6\"", 6
            }
        };
    }

    private Map<String, Object> buildExpectedEntriesInput(int count, String keyStringPrefix, String valueStringPrefix) {
        Map<String, Object> expectedEntriesInput = new HashMap<>();
        IntStream.range(1, count).forEach(idx ->
            expectedEntriesInput.put(keyStringPrefix + idx, valueStringPrefix + idx)
        );
        return expectedEntriesInput;
    }
}
