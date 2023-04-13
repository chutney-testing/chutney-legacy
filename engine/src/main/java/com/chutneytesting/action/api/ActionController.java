package com.chutneytesting.action.api;

import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(ActionController.BASE_URL)
@CrossOrigin(origins = "*")
public class ActionController {

    static final String BASE_URL = "/api/action/v1";

    private final EmbeddedActionEngine embeddedActionEngine;

    public ActionController(EmbeddedActionEngine embeddedActionEngine) {
        this.embeddedActionEngine = embeddedActionEngine;
    }

    @PreAuthorize("hasAuthority('SCENARIO_READ')")
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public List<ActionDto> allActions() {
        return embeddedActionEngine.getAllActions();
    }

    @PreAuthorize("hasAuthority('SCENARIO_READ')")
    @GetMapping(path = "/{actionId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ActionDto byActionId(@PathVariable String actionId) {
        return embeddedActionEngine.getAction(actionId).orElseThrow(ActionNotFoundException::new);
    }

    @ResponseStatus(value = HttpStatus.NOT_FOUND, reason = "The action id could not be found")
    @ExceptionHandler(ActionNotFoundException.class)
    @SuppressWarnings("unused")
    void notFoundAction() {
    }
}
