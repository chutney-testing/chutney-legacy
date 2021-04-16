package com.chutneytesting.glacio.domain.parser.executable.common;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static java.util.Optional.empty;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.chutneytesting.glacio.domain.parser.ParsingContext;
import com.github.fridujo.glacio.model.DataTable;
import com.github.fridujo.glacio.model.DocString;
import com.github.fridujo.glacio.model.Step;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

public class EntryStepParserTest {

    private static final ParsingContext parsingContext = new ParsingContext();
    private final EntryStepParser sut = new EntryStepParser();

    @ParameterizedTest
    @MethodSource("stepTextParameters")
    public void should_use_step_text_as_simple_entry(String stepKey, String stepValue) {
        Step step = mock(Step.class);
        when(step.getText()).thenReturn(stepKey + " " + stepValue);
        assertThat(sut.parseGlacioStep(parsingContext, step)).isEqualTo(entry(stepKey, stepValue));
    }

    @ParameterizedTest
    @MethodSource("docstringParameters")
    public void should_use_step_docstring_as_simple_entry_value(String stepText, DocString stepDocString) {
        Step step = mock(Step.class);
        when(step.getText()).thenReturn(stepText);
        when(step.getArgument()).thenReturn(Optional.of(stepDocString));
        assertThat(sut.parseGlacioStep(parsingContext, step)).isEqualTo(entry(stepText, stepDocString.getContent()));
    }

    @ParameterizedTest
    @MethodSource("datatableListParameters")
    public void should_use_step_datatable_as_list_entry_value_when_datatable_with_one_column(String stepText, DataTable stepDatatable) {
        Step step = mock(Step.class);
        when(step.getText()).thenReturn(stepText);
        when(step.getArgument()).thenReturn(Optional.of(stepDatatable));
        assertThat(sut.parseGlacioStep(parsingContext, step)).isEqualTo(entry(stepText, asList("a11", "a21", "a31")));
    }

    @ParameterizedTest
    @MethodSource("datatableSimpleMapParameters")
    public void should_use_step_datatable_as_map_entry_value_when_datatable_with_two_columns(String stepText, DataTable stepDatatable) {
        Step step = mock(Step.class);
        when(step.getText()).thenReturn(stepText);
        when(step.getArgument()).thenReturn(Optional.of(stepDatatable));
        assertThat(sut.parseGlacioStep(parsingContext, step))
            .isEqualTo(entry(stepText, Map.of("a11", "a12", "a21", "a22", "a31", "a32")));
    }

    @ParameterizedTest
    @MethodSource("datatableListMapParameters")
    public void should_use_step_datatable_as_map_entry_value_when_datatable_with_more_than_two_columns(String stepText, DataTable stepDatatable) {
        Step step = mock(Step.class);
        when(step.getText()).thenReturn(stepText);
        when(step.getArgument()).thenReturn(Optional.of(stepDatatable));
        assertThat(sut.parseGlacioStep(parsingContext, step))
            .isEqualTo(entry(stepText, Map.of("a11", asList("a12", "a13"), "a21", asList("a22", "a23"), "a31", asList("a32", "a33"))));
    }

    private static Object[] stepTextParameters() {
        return new Object[]{
            new Object[]{"key", "value"},
            new Object[]{"key", "value with space"}
        };
    }

    private static Object[] docstringParameters() {
        return new Object[]{
            new Object[]{"stepTextForKey", new DocString(empty(), "simple one line docString")},
            new Object[]{"i'am a key", new DocString(empty(), "docstring\non\nmultipleline")}
        };
    }

    private static Object[] datatableListParameters() {
        return new Object[]{
            new Object[]{"stepTextForKey",
                new DataTable(
                    asList(
                        new DataTable.Row(singletonList("a11")),
                        new DataTable.Row(singletonList("a21")),
                        new DataTable.Row(singletonList("a31"))
                    )
                )
            }
        };
    }

    private static Object[] datatableSimpleMapParameters() {
        return new Object[]{
            new Object[]{"stepTextForKey",
                new DataTable(
                    asList(
                        new DataTable.Row(asList("a11", "a12")),
                        new DataTable.Row(asList("a21", "a22")),
                        new DataTable.Row(asList("a31", "a32"))
                    )
                )
            }
        };
    }

    private static Object[] datatableListMapParameters() {
        return new Object[]{
            new Object[]{"stepTextForKey",
                new DataTable(
                    asList(
                        new DataTable.Row(asList("a11", "a12", "a13")),
                        new DataTable.Row(asList("a21", "a22", "a23")),
                        new DataTable.Row(asList("a31", "a32", "a33"))
                    )
                )
            }
        };
    }
}
