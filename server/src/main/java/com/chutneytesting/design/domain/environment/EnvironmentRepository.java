package com.chutneytesting.design.domain.environment;

import com.chutneytesting.admin.domain.Backupable;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Repository of {@link Environment Environments}.
 */
public interface EnvironmentRepository extends Backupable {

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

    /**
     * @return global configure, overridable by environments
     */
    Environment getEnvironment(String name);

    default List<Environment> getEnvironments() {
        return listNames().stream().map(s -> findByName(s)).collect(Collectors.toList());
    }

    default Target getAndValidateServer(String name, String environment) {
        return getOptionalServer(environment, name).orElseThrow(() -> new TargetNotFoundException("Target [" + name + "] not found in environment [" + environment + "]"));
    }

    default Optional<Target> getOptionalServer(String environment, String name) {
        return getEnvironment(environment).targets.stream()
            .filter(target -> target.name.equals(name))
            .findFirst();
    }
}
