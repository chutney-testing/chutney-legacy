/*
 * Copyright 2017-2023 Enedis
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.chutneytesting.environment.domain;

import com.chutneytesting.environment.domain.exception.AlreadyExistingEnvironmentException;
import com.chutneytesting.environment.domain.exception.AlreadyExistingTargetException;
import com.chutneytesting.environment.domain.exception.CannotDeleteEnvironmentException;
import com.chutneytesting.environment.domain.exception.EnvironmentNotFoundException;
import com.chutneytesting.environment.domain.exception.InvalidEnvironmentNameException;
import com.chutneytesting.environment.domain.exception.TargetNotFoundException;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;
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
        return createEnvironment(environment, false);
    }

    public Environment createEnvironment(Environment environment, boolean force) throws InvalidEnvironmentNameException, AlreadyExistingEnvironmentException {
        if (!force && envAlreadyExist(environment)) {
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

    public List<Target> listTargets(TargetFilter filters) {
        Set<Target> targets;
        if (filters != null && StringUtils.isNotBlank(filters.environment())) {
            targets = environmentRepository.findByName(filters.environment()).targets;
        } else {
            targets = listEnvironments()
                .stream()
                .flatMap(environment -> environment.targets.stream()).collect(Collectors.toSet());
        }
        return targets
            .stream()
            .filter(target -> match(target, filters))
            .collect(Collectors.toList());
    }


    public Set<String> listTargetsNames() {
        return listEnvironments().stream()
            .flatMap(environment -> environment.targets.stream().map(target -> target.name))
            .collect(Collectors.toSet());

    }

    public Target getTarget(String environmentName, String targetName) {
        Environment environment = environmentRepository.findByName(environmentName);
        return environment.getTarget(targetName);
    }

    public void addTarget(Target target) throws EnvironmentNotFoundException, AlreadyExistingTargetException {
        Environment environment = environmentRepository.findByName(target.environment);
        Environment newEnvironment = environment.addTarget(target);
        createOrUpdate(newEnvironment);
    }

    public void deleteTarget(String environmentName, String targetName) throws EnvironmentNotFoundException, TargetNotFoundException {
        Environment environment = environmentRepository.findByName(environmentName);
        Environment newEnvironment = environment.deleteTarget(targetName);
        createOrUpdate(newEnvironment);
    }

    public void deleteTarget(String targetName) throws EnvironmentNotFoundException, TargetNotFoundException {
        environmentRepository.getEnvironments()
            .stream()
            .filter(env -> env.targets.stream().map(target -> target.name).toList().contains(targetName))
            .forEach(env -> {
                Environment newEnvironment = env.deleteTarget(targetName);
                createOrUpdate(newEnvironment);
            });
    }

    public void updateTarget(String previousTargetName, Target targetToUpdate) throws EnvironmentNotFoundException, TargetNotFoundException {
        Environment environment = environmentRepository.findByName(targetToUpdate.environment);
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
            .toList().contains(environment.name.toUpperCase());
    }

    private boolean match(Target target, TargetFilter filters) {
        if (filters == null) {
            return true;
        }

        boolean matchName = true;
        if (StringUtils.isNotBlank(filters.name())) {
            matchName = filters.name().equals(target.name);
        }

        return matchName;
    }
}
