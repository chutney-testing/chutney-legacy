package test.unit.com.chutneytesting.engine.api.glacio.parse.default_;

import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.chutneytesting.design.domain.environment.EnvironmentService;
import com.chutneytesting.engine.api.glacio.parse.default_.TargetStepParser;
import com.chutneytesting.engine.domain.environment.TargetImpl;
import com.chutneytesting.task.spi.injectable.Target;
import com.github.fridujo.glacio.ast.Step;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class TargetStepParserTest {

    private EnvironmentService environmentService;

    private TargetStepParser sut;

    @BeforeEach
    public void setUp() {
        environmentService = mock(EnvironmentService.class);
        sut = new TargetStepParser(environmentService, "On");
    }

    @Test
    public void should_build_target_from_step_by_name() {
        com.chutneytesting.design.domain.environment.Target expectedTarget = com.chutneytesting.design.domain.environment.Target.builder()
            .withId(com.chutneytesting.design.domain.environment.Target.TargetId.of("My target name", ""))
            .withUrl("")
            .build();
        Step stepParent = mock(Step.class);
        Step step = mock(Step.class);
        when(stepParent.getSubsteps()).thenReturn(singletonList(step));
        when(step.getText()).thenReturn("On " + expectedTarget.name);
        when(environmentService.getTargetForExecution("", expectedTarget.name))
            .thenReturn(expectedTarget);

        Target targetFound = sut.parseStep(stepParent);

        assertThat(targetFound).isInstanceOf(TargetImpl.class);
        TargetImpl targetEngineFound = (TargetImpl) targetFound;
        assertThat(targetEngineFound).isNotEqualTo(TargetImpl.NONE);
        assertThat(targetEngineFound.name()).isEqualTo(expectedTarget.name);
        assertThat(targetEngineFound.url()).isEqualTo(expectedTarget.url);
    }

}
