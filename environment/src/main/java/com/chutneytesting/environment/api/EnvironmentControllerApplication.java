package com.chutneytesting.environment.api;

import com.chutneytesting.environment.api.dto.EnvironmentDto;
import com.chutneytesting.environment.api.dto.TargetDto;
import com.chutneytesting.environment.domain.exception.AlreadyExistingEnvironmentException;
import com.chutneytesting.environment.domain.exception.AlreadyExistingTargetException;
import com.chutneytesting.environment.domain.exception.CannotDeleteEnvironmentException;
import com.chutneytesting.environment.domain.exception.EnvironmentNotFoundException;
import com.chutneytesting.environment.domain.exception.InvalidEnvironmentNameException;
import com.chutneytesting.environment.domain.exception.TargetNotFoundException;
import java.util.Set;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v2/environment")
public class EnvironmentControllerApplication {

    private final EnvironmentEmbeddedApplication embeddedApplication;

    EnvironmentControllerApplication(EnvironmentEmbeddedApplication embeddedApplication) {
        this.embeddedApplication = embeddedApplication;
    }

    @CrossOrigin(origins = "*")
    @GetMapping(path = "", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public Set<EnvironmentDto> listEnvironments() {
        return embeddedApplication.listEnvironments();
    }

    @CrossOrigin(origins = "*")
    @PostMapping("")
    public EnvironmentDto createEnvironment(@RequestBody EnvironmentDto environmentDto) throws InvalidEnvironmentNameException, AlreadyExistingEnvironmentException {
        return embeddedApplication.createEnvironment(environmentDto);
    }

    @CrossOrigin(origins = "*")
    @DeleteMapping("/{environmentName}")
    public void deleteEnvironment(@PathVariable("environmentName") String environmentName) throws EnvironmentNotFoundException, CannotDeleteEnvironmentException {
        embeddedApplication.deleteEnvironment(environmentName);
    }

    @CrossOrigin(origins = "*")
    @PutMapping("/{environmentName}")
    public void updateEnvironment(@PathVariable("environmentName") String environmentName, @RequestBody EnvironmentDto environmentDto) throws InvalidEnvironmentNameException, EnvironmentNotFoundException {
        embeddedApplication.updateEnvironment(environmentName, environmentDto);
    }

    @CrossOrigin(origins = "*")
    @GetMapping(path = "/{environmentName}/target", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public Set<TargetDto> listTargets(@PathVariable("environmentName") String environmentName) throws EnvironmentNotFoundException {
        return embeddedApplication.listTargets(environmentName);
    }

    @CrossOrigin(origins = "*")
    @GetMapping(path = "/target", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public Set<TargetDto> listTargets() throws EnvironmentNotFoundException {
        return embeddedApplication.listTargets();
    }

    @CrossOrigin(origins = "*")
    @GetMapping("/{environmentName}")
    public EnvironmentDto getEnvironment(@PathVariable("environmentName") String environmentName) throws EnvironmentNotFoundException {
        return embeddedApplication.getEnvironment(environmentName);
    }

    @CrossOrigin(origins = "*")
    @GetMapping("/{environmentName}/target/{targetName}")
    public TargetDto getTarget(@PathVariable("environmentName") String environmentName, @PathVariable("targetName") String targetName) throws EnvironmentNotFoundException, TargetNotFoundException {
        return embeddedApplication.getTarget(environmentName, targetName);
    }

    @CrossOrigin(origins = "*")
    @PostMapping("/{environmentName}/target")
    public void addTarget(@PathVariable("environmentName") String environmentName, @RequestBody TargetDto targetDto) throws EnvironmentNotFoundException, AlreadyExistingTargetException {
        embeddedApplication.addTarget(environmentName, targetDto);
    }

    @CrossOrigin(origins = "*")
    @DeleteMapping("/{environmentName}/target/{targetName}")
    public void deleteTarget(@PathVariable("environmentName") String environmentName, @PathVariable("targetName") String targetName) throws EnvironmentNotFoundException, TargetNotFoundException {
        embeddedApplication.deleteTarget(environmentName, targetName);
    }

    @CrossOrigin(origins = "*")
    @PutMapping("/{environmentName}/target/{targetName}")
    public void updateTarget(@PathVariable("environmentName") String environmentName, @PathVariable("targetName") String targetName, @RequestBody TargetDto targetDto) throws EnvironmentNotFoundException, TargetNotFoundException {
        embeddedApplication.updateTarget(environmentName, targetName, targetDto);
    }

}
