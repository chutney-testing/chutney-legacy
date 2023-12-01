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

package com.chutneytesting.environment.infra;

import static java.util.stream.Collectors.toList;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.List;
import java.util.function.Predicate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MigrateTargetSecurityExecutor {

    private static final Logger LOGGER = LoggerFactory.getLogger(MigrateTargetSecurityExecutor.class);

    private final JsonFilesEnvironmentRepository environmentRepository;
    private final ObjectMapper om;

    public MigrateTargetSecurityExecutor(JsonFilesEnvironmentRepository environmentRepository) {
        this.environmentRepository = environmentRepository;
        this.om = new ObjectMapper();
    }

    public void execute() {
        LOGGER.info("Migrate targets security...");
        try {
            List<String> environmentsToMigrate = environmentRepository.listNames().stream()
                .filter(this.hasTargetWithSecurity())
                .collect(toList());

            LOGGER.info("Environments to migrate: {}", environmentsToMigrate);
            for (String envName : environmentsToMigrate) {
                try {
                    environmentRepository.save(environmentRepository.findByName(envName));
                } catch (Exception e) {
                    LOGGER.warn("Cannot migrate targets securities of environment {}", envName, e);
                }
            }
        } catch (Exception e) {
            LOGGER.warn("Cannot migrate targets securities of environment", e);
        }
        LOGGER.info("Migrate targets security");
    }

    private Predicate<String> hasTargetWithSecurity() {
        return envName -> {
            Path environmentPath = environmentRepository.getEnvironmentPath(envName);
            try {
                byte[] bytes = Files.readAllBytes(environmentPath);
                JsonNode envRootNode = om.readTree(bytes);
                if (envRootNode.hasNonNull("targets")) {
                    Iterator<JsonNode> targetsIter = envRootNode.get("targets").elements();
                    while (targetsIter.hasNext()) {
                        if (targetsIter.next().hasNonNull("security")) {
                            return true;
                        }
                    }
                }
            } catch (IOException e) {
                LOGGER.warn("Cannot read configuration file: " + environmentPath, e);
            }
            return false;
        };
    }
}
