package com.chutneytesting.environment.domain;

import com.chutneytesting.environment.domain.exception.CannotDeleteEnvironmentException;
import com.chutneytesting.environment.domain.exception.EnvironmentNotFoundException;
import com.chutneytesting.environment.domain.exception.InvalidEnvironmentNameException;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Repository of {@link Environment Environments}.
 */
public interface EnvironmentRepository {

    /**
     * @param environment to save, identified by its {@link Environment#name}
     */
    void save(Environment environment) throws InvalidEnvironmentNameException;

    Environment findByName(String name) throws EnvironmentNotFoundException;

    /**
     * @return all {@link Environment} identifiers
     */
    List<String> listNames();

    /**
     * @param name of the {@link Environment} to delete
     */
    void delete(String name) throws EnvironmentNotFoundException, CannotDeleteEnvironmentException;

    default List<Environment> getEnvironments() {
        return listNames().stream().map(this::findByName).collect(Collectors.toList());
    }
}
