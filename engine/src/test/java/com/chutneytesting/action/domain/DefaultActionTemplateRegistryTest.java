package com.chutneytesting.action.domain;

import static com.chutneytesting.action.TestActionTemplateFactory.buildActionTemplate;
import static org.assertj.core.api.Assertions.assertThat;

import com.chutneytesting.action.TestActionTemplateFactory.TestAction1;
import com.chutneytesting.action.TestActionTemplateFactory.TestAction2;
import java.util.Arrays;
import java.util.Collections;
import org.junit.jupiter.api.Test;

public class DefaultActionTemplateRegistryTest {

    @Test
    public void getByType_returns_matching_actionTemplate() {
        String actionType = "test-type";
        DefaultActionTemplateRegistry actionTemplateRegistry = withActions(buildActionTemplate(actionType, TestAction1.class));

        assertThat(actionTemplateRegistry.getByIdentifier(actionType)).isPresent();
    }

    @Test
    public void getByType_returns_empty_when_no_actionTemplate_matches() {
        String actionType = "test-type";
        DefaultActionTemplateRegistry actionTemplateRegistry = withActions(buildActionTemplate(actionType, TestAction1.class));

        assertThat(actionTemplateRegistry.getByIdentifier("unknown")).isEmpty();
    }

    @Test
    public void registry_keep_the_first_actionTemplate_with_the_same_identifier() {
        String actionType = "test-type";
        ActionTemplate primaryActionTemplate = buildActionTemplate(actionType, TestAction1.class);
        DefaultActionTemplateRegistry actionTemplateRegistry = withActions(primaryActionTemplate, buildActionTemplate(actionType, TestAction2.class));

        assertThat(actionTemplateRegistry.getByIdentifier("test-type")).hasValue(primaryActionTemplate);
    }

    private DefaultActionTemplateRegistry withActions(ActionTemplate... actionTemplates) {
        ActionTemplateLoader actionTemplateLoader = () -> Arrays.asList(actionTemplates);
        return new DefaultActionTemplateRegistry(new ActionTemplateLoaders(Collections.singletonList(actionTemplateLoader)));
    }
}
