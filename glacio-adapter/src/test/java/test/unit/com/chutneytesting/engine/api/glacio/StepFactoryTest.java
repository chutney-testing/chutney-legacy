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

import com.chutneytesting.engine.api.glacio.StepFactory;
import com.chutneytesting.engine.api.glacio.StepFactory.EXECUTABLE_KEYWORD;
import com.chutneytesting.engine.api.glacio.parse.IParseBusinessStep;
import com.chutneytesting.engine.api.glacio.parse.IParseExecutableStep;
import com.chutneytesting.engine.api.glacio.parse.default_.StrategyParser;
import com.github.fridujo.glacio.model.Step;
import com.google.common.collect.Sets;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.groovy.util.Maps;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;

public class StepFactoryTest {

    private final static Set<String> ENGLISH_EXECUTABLE_STEP_KEYWORD = Sets.newHashSet("Do", "Run");
    private static final String ENVIRONMENT = "ENV";

    private StepFactory sut;
    private IParseExecutableStep defaultExecutableStepParser;
    private IParseBusinessStep defaultBusinessStepParser;
    private StrategyParser strategyParser;
    private Map<Pair<Locale, String>, IParseExecutableStep> glacioExecutableStepParsersLanguages;

    @BeforeEach
    public void setUp() {
        defaultExecutableStepParser = mock(IParseExecutableStep.class);
        defaultBusinessStepParser = mock(IParseBusinessStep.class);
        strategyParser = mock(StrategyParser.class);
        glacioExecutableStepParsersLanguages = mock(HashMap.class);
        sut = new StepFactory(
            Maps.of(ENGLISH, Maps.of(EXECUTABLE_KEYWORD.DO, ENGLISH_EXECUTABLE_STEP_KEYWORD)),
            glacioExecutableStepParsersLanguages,
            defaultExecutableStepParser,
            defaultBusinessStepParser);
    }

    @ParameterizedTest
    @ValueSource(strings = {"Do", "Run", "", "not executable"})
    public void should_qualify_step_as_executable_when_start_with_given_keywords(String executableKeyword) {
        // Given
        boolean executable = ENGLISH_EXECUTABLE_STEP_KEYWORD.contains(executableKeyword);
        String stepTextWithTaskHint = executableKeyword + " a fantastic thing";

        // When / Then
        assertThat(
            sut.isExecutableStep(ENGLISH, buildSimpleStepWithText(stepTextWithTaskHint))
        ).isEqualTo(executable);
    }

    @ParameterizedTest
    @ValueSource(strings = {"Do i'm executable", "I'm not executable"})
    public void should_throw_exception_when_qualifying_step_with_unknown_language(String stepText) {
        Step step = buildSimpleStepWithText(stepText);

        assertThatThrownBy(() -> sut.isExecutableStep(new Locale("ww"), step))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @ParameterizedTest
    @ValueSource(strings = {"Do i'm executable", "I'm not executable"})
    public void should_throw_exception_when_build_a_non_qualified_step(String stepText) {
        Step step = buildSimpleStepWithText(stepText);

        assertThatThrownBy(() -> sut.buildExecutableStep(ENGLISH, ENVIRONMENT, step))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    public void should_delegate_step_parsing_to_parser_when_parser_keyword_known() {
        // Given
        String parserKeyword = "OMG";
        String stepName = "step name";
        IParseExecutableStep parserMock = mock(IParseExecutableStep.class);
        when(glacioExecutableStepParsersLanguages.get(Pair.of(ENGLISH, parserKeyword))).thenReturn(parserMock);
        Step successStep = buildSimpleStepWithText("Run " + parserKeyword + " " + stepName);
        assertThat(sut.isExecutableStep(ENGLISH, successStep)).isTrue();

        // When
        sut.buildExecutableStep(ENGLISH, ENVIRONMENT, successStep);

        // Then
        ArgumentCaptor<Step> stepArg = ArgumentCaptor.forClass(Step.class);
        verify(parserMock).mapToStepDefinition(eq(ENGLISH), eq(ENVIRONMENT), stepArg.capture());
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
        sut.buildExecutableStep(ENGLISH, ENVIRONMENT, successStep);

        // Then
        ArgumentCaptor<Step> stepArg = ArgumentCaptor.forClass(Step.class);
        verify(defaultExecutableStepParser).mapToStepDefinition(eq(ENGLISH), eq(ENVIRONMENT), stepArg.capture());
        assertThat(stepArg.getValue().getText()).isEqualTo(unknownParserKeyword + " " + stepName);
    }

    private Step buildSimpleStepWithText(String stepText) {
        return new Step(false, empty(), stepText, empty(), emptyList());
    }
}
