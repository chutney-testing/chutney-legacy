package com.chutneytesting.action.domain;

import static com.chutneytesting.action.TestActionTemplateFactory.buildActionTemplate;
import static org.assertj.core.api.Assertions.assertThat;

import com.chutneytesting.action.TestActionTemplateFactory.TestAction1;
import com.chutneytesting.action.TestActionTemplateFactory.TestAction2;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Test;

public class ActionTemplateLoadersTest {

    @Test
    public void load_actions_in_order() {
        List<ActionTemplateLoader> loaders = new ArrayList<>();
        loaders.add(() -> Collections.singletonList(buildActionTemplate("action1", TestAction1.class)));
        loaders.add(() -> Collections.singletonList(buildActionTemplate("action2", TestAction2.class)));
        ActionTemplateLoaders actionTemplateLoaders = new ActionTemplateLoaders(loaders);

        assertThat(actionTemplateLoaders.orderedTemplates())
            .as("ActionTemplates from ActionTemplateLoaders")
            .hasSize(2)
            .extracting(ActionTemplate::identifier)
            .containsExactly("action1", "action2");
    }
}
