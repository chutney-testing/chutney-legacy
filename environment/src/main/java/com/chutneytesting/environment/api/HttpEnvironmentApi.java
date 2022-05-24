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
import org.springframework.security.access.prepost.PreAuthorize;
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
@CrossOrigin(origins = "*")
@RequestMapping("/api/v2/environment")
public class HttpEnvironmentApi implements EnvironmentApi {

    private final EnvironmentApi delegate;

    HttpEnvironmentApi(EnvironmentApi delegate) {
        this.delegate = delegate;
    }

    @Override
    @PreAuthorize("hasAuthority('ENVIRONMENT_ACCESS')")
    @GetMapping(path = "", produces = MediaType.APPLICATION_JSON_VALUE)
    public Set<EnvironmentDto> listEnvironments() {
        return delegate.listEnvironments();
    }

    @Override
    @PreAuthorize("hasAuthority('SCENARIO_EXECUTE') or hasAuthority('CAMPAIGN_WRITE') or hasAuthority('CAMPAIGN_EXECUTE') or hasAuthority('COMPONENT_WRITE')")
    @GetMapping(path = "/names", produces = MediaType.APPLICATION_JSON_VALUE)
    public Set<String> listEnvironmentsNames() {
        return delegate.listEnvironmentsNames();
    }

    @Override
    @PreAuthorize("hasAuthority('ENVIRONMENT_ACCESS')")
    @PostMapping("")
    public EnvironmentDto createEnvironment(@RequestBody EnvironmentDto environmentDto) throws InvalidEnvironmentNameException, AlreadyExistingEnvironmentException {
        return delegate.createEnvironment(environmentDto, false);
    }

    @Override
    public EnvironmentDto createEnvironment(@RequestBody EnvironmentDto environmentDto, boolean force) {
        throw new UnsupportedOperationException();
    }

    @Override
    @PreAuthorize("hasAuthority('ENVIRONMENT_ACCESS')")
    @DeleteMapping("/{environmentName}")
    public void deleteEnvironment(@PathVariable("environmentName") String environmentName) throws EnvironmentNotFoundException, CannotDeleteEnvironmentException {
        delegate.deleteEnvironment(environmentName);
    }

    @Override
    @PreAuthorize("hasAuthority('ENVIRONMENT_ACCESS')")
    @PutMapping("/{environmentName}")
    public void updateEnvironment(@PathVariable("environmentName") String environmentName, @RequestBody EnvironmentDto environmentDto) throws InvalidEnvironmentNameException, EnvironmentNotFoundException {
        delegate.updateEnvironment(environmentName, environmentDto);
    }

    @Override
    @PreAuthorize("hasAuthority('ENVIRONMENT_ACCESS')")
    @GetMapping(path = "/{environmentName}/target", produces = MediaType.APPLICATION_JSON_VALUE)
    public Set<TargetDto> listTargets(@PathVariable("environmentName") String environmentName) throws EnvironmentNotFoundException {
        return delegate.listTargets(environmentName);
    }

    @Override
    @PreAuthorize("hasAuthority('ENVIRONMENT_ACCESS')")
    @GetMapping(path = "/target", produces = MediaType.APPLICATION_JSON_VALUE)
    public Set<TargetDto> listTargets() throws EnvironmentNotFoundException {
        return delegate.listTargets();
    }

    @Override
    @PreAuthorize("hasAuthority('COMPONENT_READ')")
    @GetMapping(path = "/target/names", produces = MediaType.APPLICATION_JSON_VALUE)
    public Set<String> listTargetsNames() throws EnvironmentNotFoundException {
        return delegate.listTargetsNames();
    }

    @Override
    @PreAuthorize("hasAuthority('ENVIRONMENT_ACCESS')")
    @GetMapping("/{environmentName}")
    public EnvironmentDto getEnvironment(@PathVariable("environmentName") String environmentName) throws EnvironmentNotFoundException {
        return delegate.getEnvironment(environmentName);
    }

    @Override
    @PreAuthorize("hasAuthority('ENVIRONMENT_ACCESS')")
    @GetMapping("/{environmentName}/target/{targetName}")
    public TargetDto getTarget(@PathVariable("environmentName") String environmentName, @PathVariable("targetName") String targetName) throws EnvironmentNotFoundException, TargetNotFoundException {
        return delegate.getTarget(environmentName, targetName);
    }

    @Override
    @PreAuthorize("hasAuthority('ENVIRONMENT_ACCESS')")
    @PostMapping("/{environmentName}/target")
    public void addTarget(@PathVariable("environmentName") String environmentName, @RequestBody TargetDto targetDto) throws EnvironmentNotFoundException, AlreadyExistingTargetException {
        delegate.addTarget(environmentName, targetDto);
    }

    @Override
    @PreAuthorize("hasAuthority('ENVIRONMENT_ACCESS')")
    @DeleteMapping("/{environmentName}/target/{targetName}")
    public void deleteTarget(@PathVariable("environmentName") String environmentName, @PathVariable("targetName") String targetName) throws EnvironmentNotFoundException, TargetNotFoundException {
        delegate.deleteTarget(environmentName, targetName);
    }

    @Override
    @PreAuthorize("hasAuthority('ENVIRONMENT_ACCESS')")
    @PutMapping("/{environmentName}/target/{targetName}")
    public void updateTarget(@PathVariable("environmentName") String environmentName, @PathVariable("targetName") String targetName, @RequestBody TargetDto targetDto) throws EnvironmentNotFoundException, TargetNotFoundException {
        delegate.updateTarget(environmentName, targetName, targetDto);
    }
}
