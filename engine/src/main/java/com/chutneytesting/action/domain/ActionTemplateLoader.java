package com.chutneytesting.action.domain;

import java.util.List;

/**
 * Simple loader of {@link ActionTemplate}.
 *
 * @see DefaultActionTemplateRegistry
 */
@FunctionalInterface
public interface ActionTemplateLoader {

    List<ActionTemplate> load();
}
