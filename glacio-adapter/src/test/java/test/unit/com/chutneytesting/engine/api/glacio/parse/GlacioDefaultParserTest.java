package test.unit.com.chutneytesting.engine.api.glacio.parse;

import static java.util.Collections.emptyList;
import static java.util.Optional.empty;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.chutneytesting.engine.api.glacio.parse.GlacioDefaultParser;
import com.chutneytesting.task.domain.TaskTemplate;
import com.chutneytesting.task.domain.TaskTemplateRegistry;
import com.github.fridujo.glacio.ast.Position;
import com.github.fridujo.glacio.ast.Step;
import java.util.Optional;
import junitparams.JUnitParamsRunner;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(JUnitParamsRunner.class)
public class GlacioDefaultParserTest {

    private GlacioDefaultParser sut;

    private TaskTemplateRegistry taskTemplateRegistry;

    @Before
    public void setUp() {
        taskTemplateRegistry = mock(TaskTemplateRegistry.class);
        sut = new GlacioDefaultParser(taskTemplateRegistry);
    }

    @Test
    public void should_have_the_lowest_priority() {
        assertThat(sut.priority()).isEqualTo(Integer.MAX_VALUE);
    }

    @Test(expected = IllegalArgumentException.class)
    public void should_throw_exception_when_taskid_not_found_in_registry() {
        sut.parseTaskType(buildSimpleStepWithText("taskId"));
    }

    @Test
    public void should_take_taskid_in_parenthesis() {
        String taskId = "succces";
        when(taskTemplateRegistry.getByIdentifier(taskId)).thenReturn(Optional.of(mock(TaskTemplate.class)));
        assertThat(sut.parseTaskType(buildSimpleStepWithText("("+taskId+") it's a step name with taskid delcared")))
            .isEqualTo(taskId);
    }

    @Test
    public void should_take_taskid_as_text_when_no_parenthesis() {
        String taskIdAsText = "success";
        when(taskTemplateRegistry.getByIdentifier(taskIdAsText)).thenReturn(Optional.of(mock(TaskTemplate.class)));
        assertThat(sut.parseTaskType(buildSimpleStepWithText(taskIdAsText)))
            .isEqualTo(taskIdAsText);
    }

    private Step buildSimpleStepWithText(String stepText) {
        return new Step(new Position(0, 0), stepText, emptyList(), empty(), empty());
    }
}
