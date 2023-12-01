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

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import org.junit.jupiter.api.Test;

public class EnvironmentRepositoryTest {

    @Test
    public void jackson_deserialize_of_environment_ok() throws IOException {

        // GIVEN
        ObjectMapper mapper = new ObjectMapper();

        final String TARGET_NAME = "MGN";
        final String TARGET_URL = "http://localhost:9060";
        final String target = "{\"name\":\"" + TARGET_NAME + "\",\"url\":\"" + TARGET_URL + "\",\"properties\":{},\"security\":{}}";

        final String ENV_NAME = "GLOBAL";
        final String ENV_DESCRIPTION = "Environment global";
        final String env = "{\"name\":\"" + ENV_NAME + "\",\"description\":\"" + ENV_DESCRIPTION + "\",\"targets\":[{\"name\":\"" + TARGET_NAME + "\",\"url\":\"" + TARGET_URL + "\",\"properties\":{},\"security\":{}}]}";

        // WHEN
        mapper.readValue(target, JsonTarget.class);
        JsonEnvironment environmentDto = mapper.readValue(env, JsonEnvironment.class);

        // THEN
        assertThat(environmentDto.name).isEqualTo(ENV_NAME);
        assertThat(environmentDto.description).isEqualTo(ENV_DESCRIPTION);
        JsonTarget jsonTarget = (JsonTarget) environmentDto.targets.toArray()[0];
        assertThat(jsonTarget.name).isEqualTo(TARGET_NAME);
        assertThat(jsonTarget.url).isEqualTo(TARGET_URL);
        assertThat(jsonTarget.properties).isEmpty();
    }
}
