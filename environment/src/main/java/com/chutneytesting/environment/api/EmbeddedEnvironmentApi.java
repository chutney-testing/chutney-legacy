package com.chutneytesting.environment.api;

import static java.util.stream.Collectors.toList;

import com.chutneytesting.environment.api.dto.EnvironmentDto;
import com.chutneytesting.environment.api.dto.TargetDto;
import com.chutneytesting.environment.domain.Environment;
import com.chutneytesting.environment.domain.EnvironmentService;
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
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.web.multipart.MultipartFile;

public class EmbeddedEnvironmentApi implements EnvironmentApi {

    private final EnvironmentService environmentService;

    private final ObjectMapper objectMapper = new ObjectMapper()
        .findAndRegisterModules()
        .enable(SerializationFeature.INDENT_OUTPUT)
        .setSerializationInclusion(JsonInclude.Include.NON_EMPTY);


    public EmbeddedEnvironmentApi(EnvironmentService environmentService) {
        this.environmentService = environmentService;
    }

    @Override
    public Set<EnvironmentDto> listEnvironments() {
        return environmentService.listEnvironments().stream()
            .map(EnvironmentDto::from)
            .sorted(Comparator.comparing(e -> e.name))
            .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    @Override
    public Set<String> listEnvironmentsNames() {
        return environmentService.listEnvironmentsNames();
    }

    @Override
    public EnvironmentDto getEnvironment(String environmentName) throws EnvironmentNotFoundException {
        return EnvironmentDto.from(environmentService.getEnvironment(environmentName));
    }

    @Override
    public EnvironmentDto createEnvironment(EnvironmentDto environmentMetadataDto, boolean force) throws InvalidEnvironmentNameException, AlreadyExistingEnvironmentException {
        return EnvironmentDto.from(environmentService.createEnvironment(environmentMetadataDto.toEnvironment(), force));
    }

    @Override
    public EnvironmentDto importEnvironment(MultipartFile file) throws UnsupportedOperationException {
        try {
            EnvironmentDto environmentDto = objectMapper.readValue(file.getBytes(), EnvironmentDto.class);
            environmentService.createEnvironment(environmentDto.toEnvironment());
            return environmentDto;
        } catch (IOException e) {
            throw new UnsupportedOperationException("Cannot deserialize file: " + file.getName(), e);
        }
    }

    @Override
    public void deleteEnvironment(String environmentName) throws EnvironmentNotFoundException, CannotDeleteEnvironmentException {
        environmentService.deleteEnvironment(environmentName);
    }

    @Override
    public void updateEnvironment(String environmentName, EnvironmentDto environmentMetadataDto) throws InvalidEnvironmentNameException, EnvironmentNotFoundException {
        environmentService.updateEnvironment(environmentName, environmentMetadataDto.toEnvironment());
    }

    @Override
    public List<TargetDto> listTargets(TargetFilter filters) throws EnvironmentNotFoundException {
        return environmentService.listTargets(filters).stream()
            .map(TargetDto::from)
            .sorted(Comparator.comparing(t -> t.name))
            .collect(toList());
    }

    @Override
    public Set<String> listTargetsNames() throws EnvironmentNotFoundException {
        return environmentService.listTargetsNames();
    }

    @Override
    public TargetDto getTarget(String environmentName, String targetName) throws EnvironmentNotFoundException, TargetNotFoundException {
        return TargetDto.from(environmentService.getTarget(environmentName, targetName));
    }

    @Override
    public void addTarget(TargetDto targetMetadataDto) throws EnvironmentNotFoundException, AlreadyExistingTargetException {
        environmentService.addTarget(targetMetadataDto.toTarget());
    }

    @Override
    public TargetDto importTarget(String environmentName, MultipartFile file) {
        try {
            TargetDto targetDto = objectMapper.readValue(file.getBytes(), TargetDto.class);
            environmentService.addTarget(targetDto.toTarget(environmentName));
            return targetDto;
        } catch (IOException e) {
            throw new UnsupportedOperationException("Cannot deserialize file: " + file.getName(), e);
        }

    }

    @Override
    public void updateTarget(String targetName, TargetDto targetMetadataDto) throws EnvironmentNotFoundException, TargetNotFoundException {
        environmentService.updateTarget(targetName, targetMetadataDto.toTarget());
    }

    @Override
    public void deleteTarget(String environmentName, String targetName) throws EnvironmentNotFoundException, TargetNotFoundException {
        environmentService.deleteTarget(environmentName, targetName);
    }

    @Override
    public void deleteTarget(String targetName) throws EnvironmentNotFoundException, TargetNotFoundException {
        environmentService.deleteTarget(targetName);
    }


}
