package com.chutneytesting.environment.api;

import com.chutneytesting.environment.api.dto.EnvironmentDto;
import com.chutneytesting.environment.api.dto.TargetDto;
import com.chutneytesting.environment.domain.EnvironmentService;
import com.chutneytesting.environment.domain.exception.AlreadyExistingEnvironmentException;
import com.chutneytesting.environment.domain.exception.AlreadyExistingTargetException;
import com.chutneytesting.environment.domain.exception.CannotDeleteEnvironmentException;
import com.chutneytesting.environment.domain.exception.EnvironmentNotFoundException;
import com.chutneytesting.environment.domain.exception.InvalidEnvironmentNameException;
import com.chutneytesting.environment.domain.exception.TargetNotFoundException;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

public class EnvironmentEmbeddedApplication {

    private final EnvironmentService environmentService;

    public EnvironmentEmbeddedApplication(EnvironmentService environmentService) {
        this.environmentService = environmentService;
    }

    public Set<EnvironmentDto> listEnvironments() {
        return environmentService.listEnvironments().stream()
            .map(EnvironmentDto::from)
            .sorted(Comparator.comparing(e -> e.name))
            .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    public EnvironmentDto createEnvironment(@RequestBody EnvironmentDto environmentMetadataDto) throws InvalidEnvironmentNameException, AlreadyExistingEnvironmentException {
        return EnvironmentDto.from(environmentService.createEnvironment(environmentMetadataDto.toEnvironment()));
    }

    public void deleteEnvironment(@PathVariable("environmentName") String environmentName) throws EnvironmentNotFoundException, CannotDeleteEnvironmentException {
        environmentService.deleteEnvironment(environmentName);
    }

    public void updateEnvironment(@PathVariable("environmentName") String environmentName, @RequestBody EnvironmentDto environmentMetadataDto) throws InvalidEnvironmentNameException, EnvironmentNotFoundException {
        environmentService.updateEnvironment(environmentName, environmentMetadataDto.toEnvironment());
    }

    public Set<TargetDto> listTargets(@PathVariable("environmentName") String environmentName) throws EnvironmentNotFoundException {
        return environmentService.listTargets(environmentName).stream()
            .map(TargetDto::from)
            .sorted(Comparator.comparing(t -> t.name))
            .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    public Set<TargetDto> listTargets() throws EnvironmentNotFoundException {
        return environmentService.listTargets().stream()
            .map(TargetDto::from)
            .sorted(Comparator.comparing(t -> t.name))
            .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    public EnvironmentDto getEnvironment(@PathVariable("environmentName") String environmentName) throws EnvironmentNotFoundException {
        return EnvironmentDto.from(environmentService.getEnvironment(environmentName));
    }

    public TargetDto getTarget(@PathVariable("environmentName") String environmentName, @PathVariable("targetName") String targetName) throws EnvironmentNotFoundException, TargetNotFoundException {
        return TargetDto.from(environmentService.getTarget(environmentName, targetName));
    }

    public void addTarget(@PathVariable("environmentName") String environmentName, @RequestBody TargetDto targetMetadataDto) throws EnvironmentNotFoundException, AlreadyExistingTargetException {
        environmentService.addTarget(environmentName, targetMetadataDto.toTarget(environmentName));
    }

    public void deleteTarget(@PathVariable("environmentName") String environmentName, @PathVariable("targetName") String targetName) throws EnvironmentNotFoundException, TargetNotFoundException {
        environmentService.deleteTarget(environmentName, targetName);
    }

    public void updateTarget(@PathVariable("environmentName") String environmentName, @PathVariable("targetName") String targetName, @RequestBody TargetDto targetMetadataDto) throws EnvironmentNotFoundException, TargetNotFoundException {
        environmentService.updateTarget(environmentName, targetName, targetMetadataDto.toTarget(environmentName));
    }

}
