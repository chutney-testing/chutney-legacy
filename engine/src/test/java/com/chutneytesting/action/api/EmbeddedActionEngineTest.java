package com.chutneytesting.action.api;

import static org.assertj.core.api.Assertions.assertThat;

import com.chutneytesting.action.domain.ActionTemplate;
import com.chutneytesting.action.domain.ActionTemplateParserV2;
import com.chutneytesting.action.domain.ActionTemplateRegistry;
import com.chutneytesting.action.spi.Action;
import com.chutneytesting.action.spi.ActionExecutionResult;
import com.google.common.collect.Lists;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

public class EmbeddedActionEngineTest {

    private EmbeddedActionEngine engine ;
    private final ActionTemplateParserV2 parser = new ActionTemplateParserV2();

    @BeforeEach
    public void setUp() {
        // G
        ActionTemplateRegistry registry = Mockito.mock(ActionTemplateRegistry.class);
        List<ActionTemplate> actions = Lists.newArrayList();
        actions.add(parser.parse(TestAction.class).result());
        actions.add(parser.parse(Test2Action.class).result());

        Mockito.when(registry.getAll()).thenReturn(actions);

        this.engine = new EmbeddedActionEngine(registry);
    }

    @Test
    public void getAllActions() {
        // W
        List<ActionDto> allActions = engine.getAllActions();

        // T
        assertThat(allActions).hasSize(2);
        assertThat(allActions.get(0).getIdentifier()).isEqualTo("test");
        assertThat(allActions.get(1).getIdentifier()).isEqualTo("test2");
    }

    @Test
    public void getAction() {
        // W
        Optional<ActionDto> action = engine.getAction("test");

        // T
        assertThat(action).isPresent();
        assertThat(action.get().getIdentifier()).isEqualTo("test");
    }


    private static class TestAction implements Action {
        public ActionExecutionResult execute() {
            return ActionExecutionResult.ok();
        }
    }

    private static class Test2Action implements Action {
        public ActionExecutionResult execute() {
            return ActionExecutionResult.ok();
        }
    }
}
