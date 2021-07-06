package com.chutneytesting.environment.domain;

import com.chutneytesting.environment.domain.exception.AlreadyExistingEnvironmentException;
import com.chutneytesting.environment.domain.exception.AlreadyExistingTargetException;
import com.chutneytesting.environment.domain.exception.CannotDeleteEnvironmentException;
import com.chutneytesting.environment.domain.exception.EnvironmentNotFoundException;
import com.chutneytesting.environment.domain.exception.InvalidEnvironmentNameException;
import com.chutneytesting.environment.domain.exception.TargetNotFoundException;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EnvironmentService {

    private static final String NAME_VALIDATION_REGEX = "[a-zA-Z0-9_\\-]{3,20}";
    private static final Pattern NAME_VALIDATION_PATTERN = Pattern.compile(NAME_VALIDATION_REGEX);

    private final Logger logger = LoggerFactory.getLogger(EnvironmentService.class);
    private final EnvironmentRepository environmentRepository;

    public EnvironmentService(EnvironmentRepository environmentRepository) {
        this.environmentRepository = environmentRepository;
    }

    public Set<String> listEnvironmentsNames() {
        return new LinkedHashSet<>(environmentRepository.listNames());
    }

    public Set<Environment> listEnvironments() {
        return environmentRepository
            .listNames()
            .stream()
            .map(environmentRepository::findByName)
            .collect(Collectors.toSet());
    }

    public Environment createEnvironment(Environment environment) throws InvalidEnvironmentNameException, AlreadyExistingEnvironmentException {
        if (envAlreadyExist(environment)) {
            throw new AlreadyExistingEnvironmentException("Environment [" + environment.name + "] already exists");
        }
        createOrUpdate(environment);
        return environment;
    }

    public Environment getEnvironment(String environmentName) throws EnvironmentNotFoundException {
        return environmentRepository.findByName(environmentName);
    }

    public void deleteEnvironment(String environmentName) throws EnvironmentNotFoundException, CannotDeleteEnvironmentException {
        environmentRepository.delete(environmentName);
    }

    public void updateEnvironment(String environmentName, Environment newVersion) throws InvalidEnvironmentNameException, EnvironmentNotFoundException {
        Environment previousEnvironment = environmentRepository.findByName(environmentName);
        Environment newEnvironment = Environment.builder()
            .from(previousEnvironment)
            .withName(newVersion.name)
            .withDescription(newVersion.description)
            .build();
        createOrUpdate(newEnvironment);
        if (!newEnvironment.name.equals(environmentName)) {
            environmentRepository.delete(environmentName);
        }
    }

    public Set<Target> listTargets(String environmentName) throws EnvironmentNotFoundException {
        Environment environment = environmentRepository.findByName(environmentName);
        return new LinkedHashSet<>(environment.targets);
    }

    public Set<Target> listTargets() {
        Set<String> targetsNames = new HashSet<>();
        Set<Target> distinctTargets = new HashSet<>();
        List<Target> targetsList = listEnvironments().stream()
            .flatMap(environment -> environment.targets.stream())
            .collect(Collectors.toList());
        for (Target target : targetsList) {
            if (targetsNames.add(target.name)) {
                distinctTargets.add(target);
            }
        }
        return distinctTargets;
    }

    public Target getTarget(String environmentName, String targetName) {
        Environment environment = environmentRepository.findByName(environmentName);
        return environment.getTarget(targetName);
    }

    public void addTarget(String environmentName, Target target) throws EnvironmentNotFoundException, AlreadyExistingTargetException {
        Environment environment = environmentRepository.findByName(environmentName);
        Environment newEnvironment = environment.addTarget(target);
        createOrUpdate(newEnvironment);
    }

    public void deleteTarget(String environmentName, String targetName) throws EnvironmentNotFoundException, TargetNotFoundException {
        Environment environment = environmentRepository.findByName(environmentName);
        Environment newEnvironment = environment.deleteTarget(targetName);
        createOrUpdate(newEnvironment);
    }

    public void updateTarget(String environmentName, String previousTargetName, Target targetToUpdate) throws EnvironmentNotFoundException, TargetNotFoundException {
        Environment environment = environmentRepository.findByName(environmentName);
        Environment newEnvironment = environment.updateTarget(previousTargetName, targetToUpdate);
        createOrUpdate(newEnvironment);
        logger.debug("Updated target " + previousTargetName + " as " + targetToUpdate.name);
    }

    private void createOrUpdate(Environment environment) {
        if (!NAME_VALIDATION_PATTERN.matcher(environment.name).matches()) {
            throw new InvalidEnvironmentNameException("Environment name must be of 3 to 20 letters, digits, underscore or hyphen");
        }
        environmentRepository.save(environment);
    }

    private boolean envAlreadyExist(Environment environment) {
        return environmentRepository.listNames().stream().map(String::toUpperCase)
            .collect(Collectors.toList()).contains(environment.name.toUpperCase());
    }
}
