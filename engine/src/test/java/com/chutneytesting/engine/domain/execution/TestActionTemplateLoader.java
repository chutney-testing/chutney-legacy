package com.chutneytesting.engine.domain.execution;

import com.chutneytesting.action.TestActionTemplateFactory.FailAction;
import com.chutneytesting.action.TestActionTemplateFactory.SuccessAction;
import com.chutneytesting.action.domain.DefaultActionTemplateRegistry;
import com.chutneytesting.action.domain.ActionTemplate;
import com.chutneytesting.action.domain.ActionTemplateLoader;
import com.chutneytesting.action.domain.ActionTemplateLoaders;
import com.chutneytesting.action.domain.ActionTemplateParserV2;
import com.chutneytesting.action.domain.ActionTemplateRegistry;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Minimal {@link ActionTemplateLoader} with simple actions:
 * <ul>
 * <li>{@link SuccessAction}</li>
 * <li>{@link FailAction}</li>
 * </ul>
 */
public class TestActionTemplateLoader implements ActionTemplateLoader {

    private final List<ActionTemplate> actionTemplates = new ArrayList<>();

    public TestActionTemplateLoader() {
        this.actionTemplates.add(new ActionTemplateParserV2().parse(SuccessAction.class).result());
        this.actionTemplates.add(new ActionTemplateParserV2().parse(FailAction.class).result());
    }

    @Override
    public List<ActionTemplate> load() {
        return actionTemplates;
    }

    public static ActionTemplateRegistry buildRegistry() {
        return new DefaultActionTemplateRegistry(new ActionTemplateLoaders(Collections.singletonList(new TestActionTemplateLoader())));
    }
}
