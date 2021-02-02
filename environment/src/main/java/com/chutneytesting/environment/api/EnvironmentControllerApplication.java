package com.chutneytesting.environment.api;

import com.chutneytesting.environment.api.dto.EnvironmentDto;
import com.chutneytesting.environment.api.dto.TargetDto;
import com.chutneytesting.environment.domain.exception.AlreadyExistingEnvironmentException;
import com.chutneytesting.environment.domain.exception.AlreadyExistingTargetException;
import com.chutneytesting.environment.domain.exception.CannotDeleteEnvironmentException;
import com.chutneytesting.environment.domain.exception.EnvironmentNotFoundException;
import com.chutneytesting.environment.domain.EnvironmentService;
import com.chutneytesting.environment.domain.exception.InvalidEnvironmentNameException;
import com.chutneytesting.environment.domain.exception.TargetNotFoundException;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;
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
public class EnvironmentControllerV2 {

    private final EnvironmentService environmentService;

    EnvironmentControllerV2(EnvironmentService environmentService) {
        this.environmentService = environmentService;
    }

    @CrossOrigin(origins = "*")
    @GetMapping(path = "", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public Set<EnvironmentDto> listEnvironments() {
        return environmentService.listEnvironments().stream()
            .map(EnvironmentDto::from)
            .sorted(Comparator.comparing(e -> e.name))
            .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    @CrossOrigin(origins = "*")
    @PostMapping("")
    public EnvironmentDto createEnvironment(@RequestBody EnvironmentDto environmentMetadataDto) throws InvalidEnvironmentNameException, AlreadyExistingEnvironmentException {
        return EnvironmentDto.from(environmentService.createEnvironment(environmentMetadataDto.toEnvironment()));
    }

    @CrossOrigin(origins = "*")
    @DeleteMapping("/{environmentName}")
    public void deleteEnvironment(@PathVariable("environmentName") String environmentName) throws EnvironmentNotFoundException, CannotDeleteEnvironmentException {
        environmentService.deleteEnvironment(environmentName);
    }

    @CrossOrigin(origins = "*")
    @PutMapping("/{environmentName}")
    public void updateEnvironment(@PathVariable("environmentName") String environmentName, @RequestBody EnvironmentDto environmentMetadataDto) throws InvalidEnvironmentNameException, EnvironmentNotFoundException {
        environmentService.updateEnvironment(environmentName, environmentMetadataDto.toEnvironment());
    }

    @CrossOrigin(origins = "*")
    @GetMapping(path = "/{environmentName}/target", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public Set<TargetDto> listTargets(@PathVariable("environmentName") String environmentName) throws EnvironmentNotFoundException {
        return environmentService.listTargets(environmentName).stream()
            .map(TargetDto::from)
            .sorted(Comparator.comparing(t -> t.name))
            .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    @CrossOrigin(origins = "*")
    @GetMapping(path = "/target", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public Set<TargetDto> listTargets() throws EnvironmentNotFoundException {
        return environmentService.listTargets().stream()
            .map(TargetDto::from)
            .sorted(Comparator.comparing(t -> t.name))
            .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    @CrossOrigin(origins = "*")
    @GetMapping("/{environmentName}")
    public EnvironmentDto getEnvironment(@PathVariable("environmentName") String environmentName) throws EnvironmentNotFoundException {
        return EnvironmentDto.from(environmentService.getEnvironment(environmentName));
    }

    @CrossOrigin(origins = "*")
    @GetMapping("/{environmentName}/target/{targetName}")
    public TargetDto getTarget(@PathVariable("environmentName") String environmentName, @PathVariable("targetName") String targetName) throws EnvironmentNotFoundException, TargetNotFoundException {
        return TargetDto.from(environmentService.getTarget(environmentName, targetName));
    }

    @CrossOrigin(origins = "*")
    @PostMapping("/{environmentName}/target")
    public void addTarget(@PathVariable("environmentName") String environmentName, @RequestBody TargetDto targetMetadataDto) throws EnvironmentNotFoundException, AlreadyExistingTargetException {
        environmentService.addTarget(environmentName, targetMetadataDto.toTarget(environmentName));
    }

    @CrossOrigin(origins = "*")
    @DeleteMapping("/{environmentName}/target/{targetName}")
    public void deleteTarget(@PathVariable("environmentName") String environmentName, @PathVariable("targetName") String targetName) throws EnvironmentNotFoundException, TargetNotFoundException {
        environmentService.deleteTarget(environmentName, targetName);
    }

    @CrossOrigin(origins = "*")
    @PutMapping("/{environmentName}/target/{targetName}")
    public void updateTarget(@PathVariable("environmentName") String environmentName, @PathVariable("targetName") String targetName, @RequestBody TargetDto targetMetadataDto) throws EnvironmentNotFoundException, TargetNotFoundException {
        environmentService.updateTarget(environmentName, targetName, targetMetadataDto.toTarget(environmentName));
    }

}
