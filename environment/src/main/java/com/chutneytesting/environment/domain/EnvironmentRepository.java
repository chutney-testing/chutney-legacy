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

    default List<Environment> findByNames(List<String> names) {
        return names.stream().map(this::findByName).toList();
    }
}
