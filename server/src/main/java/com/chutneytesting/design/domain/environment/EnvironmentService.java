package com.chutneytesting.design.domain.environment;

import static com.chutneytesting.design.domain.environment.NoTarget.NO_TARGET;

import com.chutneytesting.agent.domain.explore.CurrentNetworkDescription;
import com.chutneytesting.agent.domain.network.Agent;
import com.chutneytesting.agent.domain.network.NetworkDescription;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EnvironmentService {

    private final Logger logger = LoggerFactory.getLogger(EnvironmentService.class);

    private final EnvironmentRepository environmentRepository;
    private final CurrentNetworkDescription currentNetworkDescription;

    public EnvironmentService(EnvironmentRepository environmentRepository, CurrentNetworkDescription currentNetworkDescription) {
        this.environmentRepository = environmentRepository;
        this.currentNetworkDescription = currentNetworkDescription;
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
        environmentRepository.save(environment);
        return environment;
    }

    private boolean envAlreadyExist(Environment environment) {
        return environmentRepository.listNames().stream().map(String::toUpperCase)
            .collect(Collectors.toList()).contains(environment.name.toUpperCase());
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
        environmentRepository.save(newEnvironment);
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
        environmentRepository.save(newEnvironment);
    }

    public void deleteTarget(String environmentName, String targetName) throws EnvironmentNotFoundException, TargetNotFoundException {
        Environment environment = environmentRepository.findByName(environmentName);
        Environment newEnvironment = environment.deleteTarget(targetName);
        environmentRepository.save(newEnvironment);
    }

    public void updateTarget(String environmentName, String previousTargetName, Target targetToUpdate) throws EnvironmentNotFoundException, TargetNotFoundException {
        Environment environment = environmentRepository.findByName(environmentName);
        Environment newEnvironment = environment.updateTarget(previousTargetName, targetToUpdate);
        environmentRepository.save(newEnvironment);
        logger.debug("Updated target " + previousTargetName + " as " + targetToUpdate.name);
    }

    public Target getTargetForExecution(String environmentName, String targetName) {
        if (targetName == null || targetName.isEmpty()) {
            return NO_TARGET;
        }

        Target target = environmentRepository.getAndValidateServer(targetName, environmentName);

        Optional<NetworkDescription> networkDescription = currentNetworkDescription.findCurrent();
        if (networkDescription.isPresent() && networkDescription.get().localAgent().isPresent()) {
            final Agent localAgent = networkDescription.get().localAgent().get();
            List<Agent> agents = localAgent.findFellowAgentForReaching(target.id);
            return Target.builder()
                .copyOf(target)
                .withAgents(agents.stream().map(a -> a.agentInfo).collect(Collectors.toList()))
                .build();
        }
        return target;
    }
}
