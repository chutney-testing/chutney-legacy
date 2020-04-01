package test.unit.com.chutneytesting.engine.api.glacio.parse.default_;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.entry;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static test.unit.com.chutneytesting.engine.api.glacio.parse.GlacioParserHelper.zeroPosition;

import com.chutneytesting.engine.api.glacio.parse.default_.TargetStepParser;
import com.github.fridujo.glacio.ast.DataTable;
import com.github.fridujo.glacio.ast.Step;
import com.github.fridujo.glacio.ast.TableCell;
import com.github.fridujo.glacio.ast.TableRow;
import java.util.Optional;
import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(JUnitParamsRunner.class)
public class TargetStepParserTest {

    private TargetStepParser sut = new TargetStepParser();

    @Test
    @Parameters(method = "stepTextExceptionParameters")
    public void should_throw_exception_when_step_text_have_no_sentence_separated_with_space(String stepText) {
        Step step = mock(Step.class);
        when(step.getText()).thenReturn(stepText);
        assertThatThrownBy(() -> sut.parseStep(step)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @Parameters(method = "targetIdUrlParameters")
    public void should_set_id_and_name_from_step_text(String targetId, String targetUrl) {
        Step step = mock(Step.class);
        when(step.getText()).thenReturn(targetId + " " + targetUrl);
        assertThat(sut.parseStep(step))
            .extracting(target -> Pair.of(target.id().name(), target.url()))
            .first()
            .isEqualTo(Pair.of(targetId, targetUrl));
    }

    @Test
    @Parameters(method = "targetDatatableParameters")
    public void should_use_step_datatable_first_two_columns_as_target_properties(DataTable dataTable) {
        Step step = mock(Step.class);
        when(step.getText()).thenReturn("id url");
        when(step.getDataTable()).thenReturn(Optional.of(dataTable));
        assertThat(sut.parseStep(step).properties())
            .containsOnly(entry("a11", "a12"), entry("a21", "a22"), entry("a31", "a32"));
    }

    public static Object stepTextExceptionParameters() {
        return asList("", "alongsentencewithoutsapce", " istartwithaspace", "ifinishwithaspace ");
    }

    public static Object targetIdUrlParameters() {
        return new Object[] {
            new Object[] {"targetId", "tcp://host:port/path"}
        };
    }

    public static Object targetDatatableParameters() {
        return asList(
            new DataTable(zeroPosition,
                asList(
                    new TableRow(zeroPosition, asList(new TableCell("a11"), new TableCell("a12"), new TableCell("a13"))),
                    new TableRow(zeroPosition, asList(new TableCell("a21"), new TableCell("a22"))),
                    new TableRow(zeroPosition, asList(new TableCell("a31"), new TableCell("a32"), new TableCell("a33")))
                )
            )
        );
    }
}
