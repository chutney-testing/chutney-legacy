package test.unit.com.chutneytesting.engine.api.glacio;

import static java.util.Collections.emptyList;
import static java.util.Locale.ENGLISH;
import static java.util.Optional.empty;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.chutneytesting.engine.api.glacio.ExecutableStepFactory;
import com.chutneytesting.engine.api.glacio.ExecutableStepFactory.EXECUTABLE_KEYWORD;
import com.chutneytesting.engine.api.glacio.parse.GlacioExecutableStepParser;
import com.github.fridujo.glacio.ast.Position;
import com.github.fridujo.glacio.ast.Step;
import com.google.common.collect.Sets;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.groovy.util.Maps;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;

@RunWith(JUnitParamsRunner.class)
public class ExecutableStepFactoryTest {

    private final static Set<String> ENGLISH_EXECUTABLE_STEP_KEYWORD = Sets.newHashSet("Do", "Run");
    private static final String ENVIRONMENT = "ENV";

    private ExecutableStepFactory sut;
    private GlacioExecutableStepParser defaultGlacioParser;
    private Map<Pair<Locale, String>, GlacioExecutableStepParser> glacioExecutableStepParsersLanguages;

    @Before
    public void setUp() {
        defaultGlacioParser = mock(GlacioExecutableStepParser.class);
        glacioExecutableStepParsersLanguages = mock(HashMap.class);
        sut = new ExecutableStepFactory(
            Maps.of(ENGLISH, Maps.of(EXECUTABLE_KEYWORD.DO, ENGLISH_EXECUTABLE_STEP_KEYWORD)),
            glacioExecutableStepParsersLanguages,
            defaultGlacioParser);
    }

    @Test
    @Parameters(value = {"Do", "Run", "", "not executable"})
    public void should_qualify_step_as_executable_when_start_with_given_keywords(String executableKeyword) {
        // Given
        boolean executable = ENGLISH_EXECUTABLE_STEP_KEYWORD.contains(executableKeyword);
        String stepTextWithTaskHint = executableKeyword + " a fantastic thing";

        // When / Then
        assertThat(
            sut.isExecutableStep(ENGLISH, buildSimpleStepWithText(stepTextWithTaskHint))
        ).isEqualTo(executable);
    }

    @Test
    @Parameters(value = {"Do i'm exectuable", "I'm not executable"})
    public void should_throw_exception_when_qualifying_step_with_unknown_language(String stepText) {
        Step step = buildSimpleStepWithText(stepText);

        assertThatThrownBy(() -> sut.isExecutableStep(new Locale("ww"), step))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @Parameters(value = {"Do i'm exectuable", "I'm not executable"})
    public void should_throw_exception_when_build_a_non_qualified_step(String stepText) {
        Step step = buildSimpleStepWithText(stepText);

        assertThatThrownBy(() -> sut.build(ENGLISH, ENVIRONMENT, step))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    public void should_delegate_step_parsing_to_parser_when_parser_keyword_known() {
        // Given
        String parserKeyword = "OMG";
        String stepName = "step name";
        GlacioExecutableStepParser parserMock = mock(GlacioExecutableStepParser.class);
        when(glacioExecutableStepParsersLanguages.get(Pair.of(ENGLISH, parserKeyword))).thenReturn(parserMock);
        Step successStep = buildSimpleStepWithText("Run " + parserKeyword + " " + stepName);
        assertThat(sut.isExecutableStep(ENGLISH, successStep)).isTrue();

        // When
        sut.build(ENGLISH, ENVIRONMENT, successStep);

        // Then
        ArgumentCaptor<Step> stepArg = ArgumentCaptor.forClass(Step.class);
        verify(parserMock).mapToStepDefinition(eq(ENVIRONMENT), stepArg.capture());
        assertThat(stepArg.getValue().getText()).isEqualTo(parserKeyword + " " + stepName);
    }

    @Test
    public void should_delegate_step_parsing_to_default_parser_when_parser_keyword_unknown() {
        // Given
        String unknownParserKeyword = "OMG";
        String stepName = "step name";
        Step successStep = buildSimpleStepWithText("Run " + unknownParserKeyword + " " + stepName);
        assertThat(sut.isExecutableStep(ENGLISH, successStep)).isTrue();

        // When
        sut.build(ENGLISH, ENVIRONMENT, successStep);

        // Then
        ArgumentCaptor<Step> stepArg = ArgumentCaptor.forClass(Step.class);
        verify(defaultGlacioParser).mapToStepDefinition(eq(ENVIRONMENT), stepArg.capture());
        assertThat(stepArg.getValue().getText()).isEqualTo(unknownParserKeyword + " " + stepName);
    }

    private Step buildSimpleStepWithText(String stepText) {
        return new Step(new Position(0, 0), stepText, emptyList(), empty(), empty());
    }
}
