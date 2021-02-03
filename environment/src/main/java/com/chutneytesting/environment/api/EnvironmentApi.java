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

public interface EnvironmentApi {
    Set<EnvironmentDto> listEnvironments();

    EnvironmentDto createEnvironment(EnvironmentDto environmentMetadataDto) throws InvalidEnvironmentNameException, AlreadyExistingEnvironmentException;

    void deleteEnvironment(String environmentName) throws EnvironmentNotFoundException, CannotDeleteEnvironmentException;

    void updateEnvironment(String environmentName, EnvironmentDto environmentMetadataDto) throws InvalidEnvironmentNameException, EnvironmentNotFoundException;

    Set<TargetDto> listTargets(String environmentName) throws EnvironmentNotFoundException;

    Set<TargetDto> listTargets() throws EnvironmentNotFoundException;

    EnvironmentDto getEnvironment(String environmentName) throws EnvironmentNotFoundException;

    TargetDto getTarget(String environmentName, String targetName) throws EnvironmentNotFoundException, TargetNotFoundException;

    void addTarget(String environmentName, TargetDto targetMetadataDto) throws EnvironmentNotFoundException, AlreadyExistingTargetException;

    void deleteTarget(String environmentName, String targetName) throws EnvironmentNotFoundException, TargetNotFoundException;

    void updateTarget(String environmentName, String targetName, TargetDto targetMetadataDto) throws EnvironmentNotFoundException, TargetNotFoundException;
}
