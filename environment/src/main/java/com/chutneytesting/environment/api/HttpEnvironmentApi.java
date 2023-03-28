package com.chutneytesting.environment.api;

import com.chutneytesting.environment.api.dto.EnvironmentDto;
import com.chutneytesting.environment.api.dto.TargetDto;
import com.chutneytesting.environment.domain.TargetFilter;
import com.chutneytesting.environment.domain.exception.AlreadyExistingEnvironmentException;
import com.chutneytesting.environment.domain.exception.AlreadyExistingTargetException;
import com.chutneytesting.environment.domain.exception.CannotDeleteEnvironmentException;
import com.chutneytesting.environment.domain.exception.EnvironmentNotFoundException;
import com.chutneytesting.environment.domain.exception.InvalidEnvironmentNameException;
import com.chutneytesting.environment.domain.exception.TargetNotFoundException;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import java.io.IOException;
import java.util.List;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@CrossOrigin(origins = "*")
public class HttpEnvironmentApi implements EnvironmentApi {

    private final String ENVIRONMENT_BASE_URI = "/api/v2/environments";
    private final String TARGET_BASE_URI = "/api/v2/targets";
    private final EnvironmentApi delegate;

    private final ObjectMapper objectMapper = new ObjectMapper()
        .findAndRegisterModules()
        .enable(SerializationFeature.INDENT_OUTPUT)
        .setSerializationInclusion(JsonInclude.Include.NON_EMPTY);

    HttpEnvironmentApi(EnvironmentApi delegate) {
        this.delegate = delegate;
    }

    @Override
    @PreAuthorize("hasAuthority('ENVIRONMENT_ACCESS')")
    @GetMapping(path = ENVIRONMENT_BASE_URI, produces = MediaType.APPLICATION_JSON_VALUE)
    public Set<EnvironmentDto> listEnvironments() {
        return delegate.listEnvironments();
    }

    @Override
    @PreAuthorize("hasAuthority('SCENARIO_EXECUTE') or hasAuthority('CAMPAIGN_WRITE') or hasAuthority('CAMPAIGN_EXECUTE') or hasAuthority('COMPONENT_WRITE')")
    @GetMapping(path = ENVIRONMENT_BASE_URI + "/names", produces = MediaType.APPLICATION_JSON_VALUE)
    public Set<String> listEnvironmentsNames() {
        return delegate.listEnvironmentsNames();
    }

    @Override
    @PreAuthorize("hasAuthority('ENVIRONMENT_ACCESS')")
    @PostMapping(value = ENVIRONMENT_BASE_URI, consumes = MediaType.APPLICATION_JSON_VALUE)
    public EnvironmentDto createEnvironment(@RequestBody EnvironmentDto environmentDto) throws InvalidEnvironmentNameException, AlreadyExistingEnvironmentException {
        return delegate.createEnvironment(environmentDto, false);
    }

    @PreAuthorize("hasAuthority('ENVIRONMENT_ACCESS')")
    @PostMapping(value = ENVIRONMENT_BASE_URI, consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public EnvironmentDto importEnvironment(@RequestParam("file") MultipartFile file) {
        try {
            return importEnvironment(
                objectMapper.readValue(file.getBytes(), EnvironmentDto.class)
            );
        } catch (IOException e) {
            throw new UnsupportedOperationException("Cannot deserialize file: " + file.getName(), e);
        }
    }

    @Override
    public EnvironmentDto importEnvironment(EnvironmentDto environmentDto) {
        return delegate.importEnvironment(environmentDto);
    }

    @Override
    public EnvironmentDto createEnvironment(@RequestBody EnvironmentDto environmentDto, boolean force) {
        throw new UnsupportedOperationException();
    }

    @Override
    @PreAuthorize("hasAuthority('ENVIRONMENT_ACCESS')")
    @DeleteMapping(ENVIRONMENT_BASE_URI + "/{environmentName}")
    public void deleteEnvironment(@PathVariable("environmentName") String environmentName) throws EnvironmentNotFoundException, CannotDeleteEnvironmentException {
        delegate.deleteEnvironment(environmentName);
    }

    @Override
    @PreAuthorize("hasAuthority('ENVIRONMENT_ACCESS')")
    @PutMapping(ENVIRONMENT_BASE_URI + "/{environmentName}")
    public void updateEnvironment(@PathVariable("environmentName") String environmentName, @RequestBody EnvironmentDto environmentDto) throws InvalidEnvironmentNameException, EnvironmentNotFoundException {
        delegate.updateEnvironment(environmentName, environmentDto);
    }

    @Override
    @PreAuthorize("hasAuthority('ENVIRONMENT_ACCESS')")
    @GetMapping(ENVIRONMENT_BASE_URI + "/{environmentName}")
    public EnvironmentDto getEnvironment(@PathVariable("environmentName") String environmentName) throws EnvironmentNotFoundException {
        return delegate.getEnvironment(environmentName);
    }

    @PreAuthorize("hasAuthority('ENVIRONMENT_ACCESS')")
    @PostMapping(value = ENVIRONMENT_BASE_URI + "/{environmentName}/targets", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public TargetDto importTarget(@PathVariable("environmentName") String environmentName, @RequestParam("file") MultipartFile file) {
        try {
            return importTarget(
                environmentName,
                objectMapper.readValue(file.getBytes(), TargetDto.class)
            );
        } catch (IOException e) {
            throw new UnsupportedOperationException("Cannot deserialize file: " + file.getName(), e);
        }
    }

    @Override
    public TargetDto importTarget(String environmentName, TargetDto targetDto) {
        return delegate.importTarget(environmentName, targetDto);
    }

    @Override
    @PreAuthorize("hasAuthority('ENVIRONMENT_ACCESS')")
    @GetMapping(path = TARGET_BASE_URI + "/names", produces = MediaType.APPLICATION_JSON_VALUE)
    public Set<String> listTargetsNames() throws EnvironmentNotFoundException {
        return delegate.listTargetsNames();
    }

    @Override
    @PreAuthorize("hasAuthority('ENVIRONMENT_ACCESS')")
    @GetMapping(path = TARGET_BASE_URI, produces = MediaType.APPLICATION_JSON_VALUE)
    public List<TargetDto> listTargets(TargetFilter filters) throws EnvironmentNotFoundException {
        return delegate.listTargets(filters);
    }

    @Override
    @PreAuthorize("hasAuthority('ENVIRONMENT_ACCESS')")
    @GetMapping(ENVIRONMENT_BASE_URI +"/{environmentName}/targets/{targetName}")
    public TargetDto getTarget(@PathVariable("environmentName") String environmentName, @PathVariable("targetName") String targetName) throws EnvironmentNotFoundException, TargetNotFoundException {
        return delegate.getTarget(environmentName, targetName);
    }

    @Override
    @PreAuthorize("hasAuthority('ENVIRONMENT_ACCESS')")
    @DeleteMapping(ENVIRONMENT_BASE_URI+ "/{environmentName}/targets/{targetName}")
    public void deleteTarget(@PathVariable("environmentName") String environmentName, @PathVariable("targetName") String targetName) throws EnvironmentNotFoundException, TargetNotFoundException {
        delegate.deleteTarget(environmentName, targetName);
    }

    @Override
    @PreAuthorize("hasAuthority('ENVIRONMENT_ACCESS')")
    @DeleteMapping(TARGET_BASE_URI+ "/{targetName}")
    public void deleteTarget(@PathVariable("targetName") String targetName) throws EnvironmentNotFoundException, TargetNotFoundException {
        delegate.deleteTarget(targetName);
    }


    @Override
    @PreAuthorize("hasAuthority('ENVIRONMENT_ACCESS')")
    @PostMapping(TARGET_BASE_URI)
    public void addTarget(@RequestBody TargetDto targetDto) throws EnvironmentNotFoundException, AlreadyExistingTargetException {
        delegate.addTarget(targetDto);
    }

    @Override
    @PreAuthorize("hasAuthority('ENVIRONMENT_ACCESS')")
    @PutMapping(TARGET_BASE_URI + "/{targetName}")
    public void updateTarget(@PathVariable("targetName") String targetName, @RequestBody TargetDto targetDto) throws EnvironmentNotFoundException, TargetNotFoundException {
        delegate.updateTarget(targetName, targetDto);
    }
}
