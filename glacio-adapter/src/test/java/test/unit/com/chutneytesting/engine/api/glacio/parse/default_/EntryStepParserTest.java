package test.unit.com.chutneytesting.engine.api.glacio.parse.default_;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static java.util.Optional.empty;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static test.unit.com.chutneytesting.engine.api.glacio.parse.GlacioParserHelper.zeroPosition;

import com.chutneytesting.engine.api.glacio.parse.default_.EntryStepParser;
import com.github.fridujo.glacio.ast.DataTable;
import com.github.fridujo.glacio.ast.DocString;
import com.github.fridujo.glacio.ast.Step;
import com.github.fridujo.glacio.ast.TableCell;
import com.github.fridujo.glacio.ast.TableRow;
import java.util.Optional;
import org.apache.groovy.util.Maps;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

public class EntryStepParserTest {

    private EntryStepParser sut = new EntryStepParser();

    @ParameterizedTest
    @MethodSource("stepTextParameters")
    public void should_use_step_text_as_simple_entry(String stepKey, String stepValue) {
        Step step = mock(Step.class);
        when(step.getText()).thenReturn(stepKey + " " + stepValue);
        assertThat(sut.parseStep(step)).isEqualTo(entry(stepKey, stepValue));
    }

    @ParameterizedTest
    @MethodSource("docstringParameters")
    public void should_use_step_docstring_as_simple_entry_value(String stepText, DocString stepDocString) {
        Step step = mock(Step.class);
        when(step.getText()).thenReturn(stepText);
        when(step.getDocString()).thenReturn(Optional.of(stepDocString));
        assertThat(sut.parseStep(step)).isEqualTo(entry(stepText, stepDocString.getContent()));
    }

    @ParameterizedTest
    @MethodSource("datatableListParameters")
    public void should_use_step_datatable_as_list_entry_value_when_datatable_with_one_column(String stepText, DataTable stepDatatable) {
        Step step = mock(Step.class);
        when(step.getText()).thenReturn(stepText);
        when(step.getDataTable()).thenReturn(Optional.of(stepDatatable));
        assertThat(sut.parseStep(step)).isEqualTo(entry(stepText, asList("a11", "a21", "a31")));
    }

    @ParameterizedTest
    @MethodSource("datatableSimpleMapParameters")
    public void should_use_step_datatable_as_map_entry_value_when_datatable_with_two_columns(String stepText, DataTable stepDatatable) {
        Step step = mock(Step.class);
        when(step.getText()).thenReturn(stepText);
        when(step.getDataTable()).thenReturn(Optional.of(stepDatatable));
        assertThat(sut.parseStep(step))
            .isEqualTo(entry(stepText, Maps.of("a11", "a12", "a21", "a22", "a31", "a32")));
    }

    @ParameterizedTest
    @MethodSource("datatableListMapParameters")
    public void should_use_step_datatable_as_map_entry_value_when_datatable_with_more_than_two_columns(String stepText, DataTable stepDatatable) {
        Step step = mock(Step.class);
        when(step.getText()).thenReturn(stepText);
        when(step.getDataTable()).thenReturn(Optional.of(stepDatatable));
        assertThat(sut.parseStep(step))
            .isEqualTo(entry(stepText, Maps.of("a11", asList("a12", "a13"), "a21", asList("a22", "a23"), "a31", asList("a32", "a33"))));
    }

    private static Object[] stepTextParameters() {
        return new Object[]{
            new Object[]{"key", "value"},
            new Object[]{"key", "value with space"}
        };
    }

    private static Object[] docstringParameters() {
        return new Object[]{
            new Object[]{"stepTextForKey", new DocString(zeroPosition, empty(), "simple one line docString")},
            new Object[]{"i'am a key", new DocString(zeroPosition, empty(), "docstring\non\nmultipleline")}
        };
    }

    private static Object[] datatableListParameters() {
        return new Object[]{
            new Object[]{"stepTextForKey",
                new DataTable(zeroPosition,
                    asList(
                        new TableRow(zeroPosition, singletonList(new TableCell("a11"))),
                        new TableRow(zeroPosition, singletonList(new TableCell("a21"))),
                        new TableRow(zeroPosition, singletonList(new TableCell("a31")))
                    )
                )
            }
        };
    }

    private static Object[] datatableSimpleMapParameters() {
        return new Object[]{
            new Object[]{"stepTextForKey",
                new DataTable(zeroPosition,
                    asList(
                        new TableRow(zeroPosition, asList(new TableCell("a11"), new TableCell("a12"))),
                        new TableRow(zeroPosition, asList(new TableCell("a21"), new TableCell("a22"))),
                        new TableRow(zeroPosition, asList(new TableCell("a31"), new TableCell("a32")))
                    )
                )
            }
        };
    }

    private static Object[] datatableListMapParameters() {
        return new Object[]{
            new Object[]{"stepTextForKey",
                new DataTable(zeroPosition,
                    asList(
                        new TableRow(zeroPosition, asList(new TableCell("a11"), new TableCell("a12"), new TableCell("a13"))),
                        new TableRow(zeroPosition, asList(new TableCell("a21"), new TableCell("a22"), new TableCell("a23"))),
                        new TableRow(zeroPosition, asList(new TableCell("a31"), new TableCell("a32"), new TableCell("a33")))
                    )
                )
            }
        };
    }
}
