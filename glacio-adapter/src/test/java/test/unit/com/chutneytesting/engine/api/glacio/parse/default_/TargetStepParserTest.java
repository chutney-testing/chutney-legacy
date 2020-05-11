package test.unit.com.chutneytesting.engine.api.glacio.parse.default_;

import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.chutneytesting.engine.api.glacio.parse.default_.TargetStepParser;
import com.chutneytesting.engine.domain.environment.TargetImpl;
import com.chutneytesting.task.spi.injectable.Target;
import com.github.fridujo.glacio.ast.Step;
import org.junit.Test;

public class TargetStepParserTest {

    private TargetStepParser sut = new TargetStepParser("On");

    @Test
    public void should_set_id_and_name_from_step_text() {
        String targetName = "My target name";
        Step stepParent = mock(Step.class);
        Step step = mock(Step.class);
        when(stepParent.getSubsteps()).thenReturn(singletonList(step));
        when(step.getText()).thenReturn("On " + targetName);
        Target targetFound = sut.parseStep(stepParent);
        assertThat(targetFound).isNotEqualTo(TargetImpl.NONE);
        assertThat(targetFound.name()).isEqualTo(targetName);
    }

}
