package test.unit.com.chutneytesting.engine.api.glacio;

import static com.chutneytesting.engine.api.glacio.ExecutableStepFactory.EXECUTABLE_KEYWORD_DO;
import static com.chutneytesting.engine.api.glacio.ExecutableStepFactory.EXECUTABLE_KEYWORD_EXECUTE;
import static java.util.Collections.emptyList;
import static java.util.Optional.empty;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.chutneytesting.engine.api.glacio.ExecutableStepFactory;
import com.chutneytesting.engine.api.glacio.parse.GlacioExecutableStepParser;
import com.github.fridujo.glacio.ast.Position;
import com.github.fridujo.glacio.ast.Step;
import java.util.List;
import java.util.TreeSet;
import java.util.stream.Stream;
import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import org.assertj.core.util.Lists;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;

@RunWith(JUnitParamsRunner.class)
public class ExecutableStepFactoryTest {

    private static List<String> EXECUTABLE_STEP_KEYWORDS = Lists.list(EXECUTABLE_KEYWORD_DO, EXECUTABLE_KEYWORD_EXECUTE);

    private ExecutableStepFactory sut;
    private TreeSet<GlacioExecutableStepParser> glacioExecutableStepParsers;

    @Before
    public void setUp() {
        glacioExecutableStepParsers = mock(TreeSet.class);
        sut = new ExecutableStepFactory(glacioExecutableStepParsers);
    }

    @Test
    @Parameters(value = {EXECUTABLE_KEYWORD_DO, EXECUTABLE_KEYWORD_EXECUTE, "", "not executable"})
    public void should_qualify_step_as_executable(String executableKeyword) {
        // Given
        boolean expected = EXECUTABLE_STEP_KEYWORDS.contains(executableKeyword);
        String stepTextWithTaskHint = executableKeyword + " a fantastic thing";

        // When / Then
        assertThat(
            sut.isExecutableStep(buildSimpleStepWithText(stepTextWithTaskHint))
        ).isEqualTo(expected);
    }

    @Test(expected = IllegalArgumentException.class)
    public void should_throw_exception_when_build_a_non_executable_step() {
        sut.build(buildSimpleStepWithText("not an executable step text !!"));
    }

    @Test
    public void should_delegate_step_parsing_to_filtered_parsers() {
        // Given
        GlacioExecutableStepParser parserToBeSelected = mock(GlacioExecutableStepParser.class);
        when(parserToBeSelected.couldParse(any())).thenReturn(true);
        GlacioExecutableStepParser parserFiltered = mock(GlacioExecutableStepParser.class);
        when(parserFiltered.couldParse(any())).thenReturn(false);
        when(glacioExecutableStepParsers.stream())
            .thenReturn(Stream.of(parserToBeSelected, parserFiltered));

        Step successStep = buildSimpleStepWithText(EXECUTABLE_KEYWORD_EXECUTE + " success");

        // When
        sut.build(successStep);

        // Then
        verify(parserToBeSelected).couldParse(any());
        verify(parserFiltered).couldParse(any());
        verify(parserToBeSelected).parseStep(any());
        verify(parserFiltered, times(0)).parseStep(any());
    }

    @Test
    public void should_try_selected_parsers_one_after_the_other_if_parsing_failed() {
        // Given
        GlacioExecutableStepParser parserWithException = mock(GlacioExecutableStepParser.class);
        when(parserWithException.couldParse(any())).thenReturn(true);
        when(parserWithException.parseStep(any())).thenThrow(Exception.class);
        GlacioExecutableStepParser secondParser = mock(GlacioExecutableStepParser.class);
        when(secondParser.couldParse(any())).thenReturn(true);
        when(glacioExecutableStepParsers.stream())
            .thenReturn(Stream.of(parserWithException, secondParser));

        Step successStep = buildSimpleStepWithText(EXECUTABLE_KEYWORD_EXECUTE + " success");

        // When
        sut.build(successStep);

        // Then
        verify(parserWithException).couldParse(any());
        verify(secondParser).couldParse(any());
        verify(parserWithException).parseStep(any());
        verify(secondParser).parseStep(any());
    }

    @Test(expected = IllegalArgumentException.class)
    public void should_throw_exception_when_no_parser_found() {
        String stepText = "success";
        when(glacioExecutableStepParsers.stream()).thenReturn(Stream.empty());
        Step successStep = buildSimpleStepWithText(EXECUTABLE_KEYWORD_EXECUTE + " " + stepText);

        // When
        sut.build(successStep);
    }

    @Test
    @Parameters(value = {EXECUTABLE_KEYWORD_DO, EXECUTABLE_KEYWORD_EXECUTE})
    public void should_remove_keyword_from_step_text_before_delegating_parsing(String keyword) {
        // Given
        String stepName = "success";
        Step successStep = buildSimpleStepWithText(keyword + " " + stepName);
        GlacioExecutableStepParser parser = mock(GlacioExecutableStepParser.class);
        when(parser.couldParse(any())).thenReturn(true);
        when(glacioExecutableStepParsers.stream()).thenReturn(Stream.of(parser));

        // When
        sut.build(successStep);

        // Then
        ArgumentCaptor<Step> stepArg = ArgumentCaptor.forClass(Step.class);
        verify(parser).couldParse(any());
        verify(parser).parseStep(stepArg.capture());
        assertThat(stepArg.getValue().getText()).isEqualTo(stepName);
    }

    private Step buildSimpleStepWithText(String stepText) {
        return new Step(new Position(0, 0), stepText, emptyList(), empty(), empty());
    }
}
