package test.unit.com.chutneytesting.engine.api.glacio.parse.default_;

import static java.util.Collections.emptyList;
import static java.util.Optional.empty;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.chutneytesting.design.domain.environment.EnvironmentService;
import com.chutneytesting.engine.api.glacio.parse.default_.DefaultGlacioParser;
import com.chutneytesting.engine.api.glacio.parse.default_.StrategyParser;
import com.chutneytesting.task.domain.TaskTemplate;
import com.chutneytesting.task.domain.TaskTemplateRegistry;
import com.github.fridujo.glacio.model.Step;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class DefaultGlacioParserTest {

    private DefaultGlacioParser sut;

    private TaskTemplateRegistry taskTemplateRegistry;
    private EnvironmentService environmentService;
    private StrategyParser strategyParser;

    @BeforeEach
    public void setUp() {
        taskTemplateRegistry = mock(TaskTemplateRegistry.class);
        environmentService = mock(EnvironmentService.class);
        strategyParser = mock(StrategyParser.class);
        sut = new DefaultGlacioParser(taskTemplateRegistry, environmentService);
    }

    @Test()
    public void should_throw_exception_when_taskid_not_found_in_registry() {
        assertThatThrownBy(() -> sut.parseTaskType(buildSimpleStepWithText("taskId")))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    public void should_take_taskid_as_first_word() {
        String taskId = "succces";
        when(taskTemplateRegistry.getByIdentifier(taskId)).thenReturn(Optional.of(mock(TaskTemplate.class)));
        assertThat(sut.parseTaskType(buildSimpleStepWithText(taskId + " it's a step name with taskid delcared")))
            .isEqualTo(taskId);
    }

    @Test
    public void should_take_taskid_as_text_when_only_one_word() {
        String taskIdAsText = "success";
        when(taskTemplateRegistry.getByIdentifier(taskIdAsText)).thenReturn(Optional.of(mock(TaskTemplate.class)));
        assertThat(sut.parseTaskType(buildSimpleStepWithText(taskIdAsText)))
            .isEqualTo(taskIdAsText);
    }

    public static Step buildSimpleStepWithText(String stepText) {
        return new Step(false, empty(), stepText, empty(), emptyList());
    }
}
