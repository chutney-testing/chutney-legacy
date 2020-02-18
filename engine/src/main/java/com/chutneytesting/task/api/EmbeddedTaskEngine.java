package com.chutneytesting.task.api;

import com.chutneytesting.task.domain.TaskTemplateRegistry;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

@Component
public class EmbeddedTaskEngine {

    private List<TaskDto> allTasks;

    public EmbeddedTaskEngine(TaskTemplateRegistry taskTemplateRegistry) {
        this.allTasks = taskTemplateRegistry.getAll().parallelStream()
            .map(TaskTemplateMapper::toDto)
            .collect(Collectors.toList());
    }

    public List<TaskDto> getAllTasks() {
        return allTasks;
    }

    public Optional<TaskDto> getTask(String identifier) {
        return this.allTasks.stream()
            .filter(taskDto -> taskDto.getIdentifier().equals(identifier))
            .findFirst();
    }
}
