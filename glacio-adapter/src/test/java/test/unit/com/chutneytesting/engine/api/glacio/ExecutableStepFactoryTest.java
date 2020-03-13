package test.unit.com.chutneytesting.engine.api.glacio;

import static com.chutneytesting.engine.api.glacio.ExecutableStepFactory.EXECUTABLE_KEYWORD_DO;
import static com.chutneytesting.engine.api.glacio.ExecutableStepFactory.EXECUTABLE_KEYWORD_EXECUTE;
import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;
import static java.util.Optional.empty;
import static org.assertj.core.api.Assertions.assertThat;

import com.chutneytesting.engine.api.glacio.ExecutableStepFactory;
import com.chutneytesting.engine.domain.execution.StepDefinition;
import com.github.fridujo.glacio.ast.Position;
import com.github.fridujo.glacio.ast.Step;
import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(JUnitParamsRunner.class)
public class ExecutableStepFactoryTest {

    private ExecutableStepFactory sut;

    @Before
    public void setUp() {
        sut = new ExecutableStepFactory();
    }

    @Test
    @Parameters(value = {EXECUTABLE_KEYWORD_DO, EXECUTABLE_KEYWORD_EXECUTE, ""})
    public void should_qualify_step_as_executable(String executableKeyword) {
        // Given
        boolean expected = !executableKeyword.isEmpty();
        String stepTextWhitoutTaskHint = executableKeyword+" a fantastic thing";
        String stepTextWithTaskHint = executableKeyword+" (task-hint) a fantastic thing";

        // When / Then
        assertThat(
            sut.isExecutableStep(buildSimpleStepWithText(stepTextWhitoutTaskHint))
        ).isEqualTo(expected);
        assertThat(
            sut.isExecutableStep(buildSimpleStepWithText(stepTextWithTaskHint))
        ).isEqualTo(expected);
    }

    @Test(expected = IllegalArgumentException.class)
    public void should_throw_exception_when_build_on_non_executable_step() {
        sut.build(buildSimpleStepWithText("not an executable step text !!"));
    }

    @Test
    public void should_build_step_without_task_hint_nor_inputs_nor_outputs() {
        // Given
        Step successStep = buildSimpleStepWithText(EXECUTABLE_KEYWORD_EXECUTE+" success");
        StepDefinition successAction = buldSimpleStepDefinition("success");

        // When
        StepDefinition stepDefinition = sut.build(successStep);

        // Then
        assertThat(stepDefinition).isEqualTo(successAction);
    }

    @Test
    public void should_build_step_with_task_hint_whitout_inputs_nor_outputs() {
        // Given
        Step successStep = buildSimpleStepWithText(EXECUTABLE_KEYWORD_DO+" (success) It's a great success for us");
        StepDefinition successAction = buldSimpleStepDefinition("It's a great success for us");

        // When
        StepDefinition stepDefinition = sut.build(successStep);

        // Then
        assertThat(stepDefinition).isEqualTo(successAction);
    }

    private Step buildSimpleStepWithText(String stepText) {
        return new Step(new Position(0, 0), stepText, emptyList(), empty(), empty());
    }

    private StepDefinition buldSimpleStepDefinition(String name) {
        return new StepDefinition(name, null, "success", null, emptyMap(), emptyList(), emptyMap());
    }
}
