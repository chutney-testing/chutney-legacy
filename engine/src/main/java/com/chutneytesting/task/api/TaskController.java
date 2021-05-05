package com.chutneytesting.task.api;

import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(TaskController.BASE_URL)
@CrossOrigin(origins = "*")
public class TaskController {

    static final String BASE_URL = "/api/task/v1";

    private final EmbeddedTaskEngine embeddedTaskEngine;

    public TaskController(EmbeddedTaskEngine embeddedTaskEngine) {
        this.embeddedTaskEngine = embeddedTaskEngine;
    }

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public List<TaskDto> allTasks() {
        return embeddedTaskEngine.getAllTasks();
    }

    @GetMapping(path = "/{taskId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public TaskDto byTaskId(@PathVariable String taskId) {
        return embeddedTaskEngine.getTask(taskId).orElseThrow(TaskNotFoundException::new);
    }

    @ResponseStatus(value = HttpStatus.NOT_FOUND, reason = "The task id could not be found")
    @ExceptionHandler(TaskNotFoundException.class)
    @SuppressWarnings("unused")
    void notFoundTask() {
    }
}
