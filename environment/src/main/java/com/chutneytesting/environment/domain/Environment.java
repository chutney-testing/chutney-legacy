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

import static java.util.Collections.emptySet;
import static java.util.Optional.ofNullable;

import com.chutneytesting.environment.domain.exception.AlreadyExistingTargetException;
import com.chutneytesting.environment.domain.exception.TargetNotFoundException;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

public class Environment {

    public final String name;
    public final String description;
    public final Set<Target> targets;

    private Environment(String name, String description, Set<Target> targets) {
        this.name = name;
        this.description = description;
        this.targets = targets;
    }

    public static EnvironmentBuilder builder() {
        return new EnvironmentBuilder();
    }

    Environment addTarget(Target target) {
        if (this.containsTarget(target)) {
            throw new AlreadyExistingTargetException("Target [" + target.name + "] already exists in [" + this.name + "] environment");
        }

        return Environment.builder()
            .from(this)
            .addTarget(target)
            .build();
    }

    private boolean containsTarget(Target target) {
        return targets.stream().anyMatch(t -> t.name.equalsIgnoreCase(target.name));
    }

    Target getTarget(String targetName) {
        return targets.stream()
            .filter(t -> t.name.equals(targetName))
            .findFirst()
            .orElseThrow(() -> new TargetNotFoundException("Target [" + targetName + "] not found in environment [" + name + "]"));
    }

    Environment deleteTarget(String targetName) {
        Optional<Target> targetToRemove = targets.stream()
            .filter(t -> t.name.equals(targetName))
            .findFirst();

        return targetToRemove
            .map(t -> {
                Set<Target> updatedTargets = new HashSet<>(targets);
                updatedTargets.remove(t);

                return Environment.builder()
                    .from(this)
                    .withTargets(updatedTargets)
                    .build();
            })
            .orElseThrow(() -> new TargetNotFoundException("Target [" + targetName + "] not found in environment [" + name + "]"));
    }

    Environment updateTarget(String targetName, Target targetToUpdate) {
        Optional<Target> previousTarget = targets.stream()
            .filter(t -> t.name.equals(targetName))
            .findFirst();

        return previousTarget
            .map(t -> {
                    if (previousTarget.get().equals(targetToUpdate)) {
                        return this;
                    }
                    Set<Target> updatedTargets = new HashSet<>(targets);
                    updatedTargets.remove(t);
                    updatedTargets.add(targetToUpdate);

                    return Environment.builder()
                        .from(this)
                        .withTargets(updatedTargets)
                        .build();
                }
            )
            .orElseThrow(() -> new TargetNotFoundException("Target [" + targetName + "] not found in environment [" + name + "]"));
    }

    public static class EnvironmentBuilder {

        private String name;
        private String description;
        private Set<Target> targets = new HashSet<>();

        private EnvironmentBuilder() {
        }

        public Environment build() {
            return new Environment(
                ofNullable(name).orElse(""),
                ofNullable(description).orElse(""),
                ofNullable(targets).map(Collections::unmodifiableSet).orElse(emptySet())
            );
        }

        public EnvironmentBuilder withName(String name) {
            this.name = name;
            return this;
        }

        public EnvironmentBuilder withDescription(String description) {
            this.description = description;
            return this;
        }

        public EnvironmentBuilder withTargets(Set<Target> targetSet) {
            this.targets = new HashSet<>(targetSet);
            return this;
        }

        public EnvironmentBuilder addTarget(Target target) {
            this.targets.add(target);
            return this;
        }

        public EnvironmentBuilder addAllTargets(List<Target> targets) {
            this.targets.addAll(targets);
            return this;
        }

        public EnvironmentBuilder from(Environment environment) {
            this.name = environment.name;
            this.description = environment.description;
            this.targets = new HashSet<>(environment.targets);
            return this;
        }

    }

    @Override
    public String toString() {
        return "Environment{" +
            "name='" + name + '\'' +
            ", description='" + description + '\'' +
            ", targets=" + targets +
            '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Environment that = (Environment) o;
        return Objects.equals(name, that.name) &&
            Objects.equals(description, that.description) &&
            Objects.equals(targets, that.targets);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, description, targets);
    }
}
