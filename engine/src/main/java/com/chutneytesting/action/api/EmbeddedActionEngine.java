package com.chutneytesting.action.api;

import com.chutneytesting.action.domain.ActionTemplateRegistry;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class EmbeddedActionEngine {

    private final List<ActionDto> allActions;

    public EmbeddedActionEngine(ActionTemplateRegistry actionTemplateRegistry) {
        this.allActions = actionTemplateRegistry.getAll().parallelStream()
            .map(ActionTemplateMapper::toDto)
            .collect(Collectors.toList());
    }

    public List<ActionDto> getAllActions() {
        return allActions;
    }

    public Optional<ActionDto> getAction(String identifier) {
        return this.allActions.stream()
            .filter(actionDto -> actionDto.getIdentifier().equals(identifier))
            .findFirst();
    }
}
