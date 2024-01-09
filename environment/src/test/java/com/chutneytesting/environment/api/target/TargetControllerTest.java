/*
 *
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
 *
 */

package com.chutneytesting.environment.api.target;

import static java.util.Arrays.stream;
import static java.util.stream.Collectors.toCollection;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.chutneytesting.environment.api.EnvironmentRestExceptionHandler;
import com.chutneytesting.environment.domain.Environment;
import com.chutneytesting.environment.domain.EnvironmentRepository;
import com.chutneytesting.environment.domain.EnvironmentService;
import com.chutneytesting.environment.domain.Target;
import com.chutneytesting.environment.domain.exception.EnvironmentNotFoundException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.assertj.core.util.Lists;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

public class TargetControllerTest {

    private final String environmentBasePath = "/api/v2/environments";
    private final String targetBasePath = "/api/v2/targets";

    private final EnvironmentRepository environmentRepository = mock(EnvironmentRepository.class);
    private final EnvironmentService environmentService = new EnvironmentService(environmentRepository);
    private final EmbeddedTargetApi embeddedApplication = new EmbeddedTargetApi(environmentService);
    private final TargetController targetController = new TargetController(embeddedApplication);

    final Map<String, Environment> registeredEnvironments = new LinkedHashMap<>();

    private MockMvc mockMvc;

    @BeforeEach
    public void setUp() {
        MappingJackson2HttpMessageConverter mappingJackson2HttpMessageConverter = new MappingJackson2HttpMessageConverter();
        mappingJackson2HttpMessageConverter.setObjectMapper(new ObjectMapper().findAndRegisterModules());
        mockMvc = MockMvcBuilders.standaloneSetup(targetController)
            .setControllerAdvice(new EnvironmentRestExceptionHandler())
            .setMessageConverters(mappingJackson2HttpMessageConverter)
            .build();
    }

    private static Object[] params_listTargets_returns_all_available() {
        return new Object[]{
            new Object[]{new String[]{}},
            new Object[]{new String[]{"target1", "target2"}}
        };
    }

    @ParameterizedTest
    @MethodSource("params_listTargets_returns_all_available")
    public void listTargets_returns_all_available(String[] targetNames) throws Exception {
        addAvailableEnvironment("env test", targetNames);

        ResultActions resultActions = mockMvc.perform(get(targetBasePath + "?environment={env}", "env test"))
            .andDo(MockMvcResultHandlers.log())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()", equalTo(targetNames.length)));

        for (int i = 0; i < targetNames.length; i++) {
            resultActions.andExpect(jsonPath("$.[" + i + "].name", equalTo(targetNames[i])))
                .andExpect(jsonPath("$.[" + i + "].url", equalTo("http://" + targetNames[i] + ":43")));
        }
    }

    @Test
    public void should_list_distinct_targets_names_in_any_environment() throws Exception {
        List<String> targetsNames = Lists.list("t1", "t2", "t3");
        addAvailableEnvironment("env1", targetsNames.get(0), targetsNames.get(2));
        addAvailableEnvironment("env2", targetsNames.get(0), targetsNames.get(1));

        ResultActions resultActions = mockMvc.perform(get(targetBasePath + "/names"))
            .andDo(MockMvcResultHandlers.log())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(3)));

        for (String targetName : targetsNames) {
            resultActions.andExpect(jsonPath("$[?(@=='" + targetName + "')]", hasSize(1)));
        }
    }

    @Test
    public void addTarget_saves_an_environment_with_the_new_target() throws Exception {
        addAvailableEnvironment("env_test", "server 1");

        mockMvc.perform(
                post(targetBasePath )
                    .content("{\"name\": \"server 2\", \"url\": \"ssh://somehost:42\", \"environment\": \"env_test\"}")
                    .contentType(MediaType.APPLICATION_JSON))
            .andDo(MockMvcResultHandlers.log())
            .andExpect(status().isOk());

        ArgumentCaptor<Environment> environmentArgumentCaptor = ArgumentCaptor.forClass(Environment.class);
        verify(environmentRepository, times(1)).save(environmentArgumentCaptor.capture());

        Environment savedEnvironment = environmentArgumentCaptor.getValue();
        assertThat(savedEnvironment).isNotNull();
        assertThat(savedEnvironment.targets).hasSize(2);
        assertThat(savedEnvironment.targets.toArray()).contains(
            Target.builder().withName("server 2").withUrl("ssh://somehost:42").withEnvironment("env_test").build()
        );
    }

    @Test
    public void addTarget_returns_409_when_already_existing() throws Exception {
        addAvailableEnvironment("env test", "server 1");

        mockMvc.perform(
                post(targetBasePath)
                    .content("{\"name\": \"server 1\", \"url\": \"ssh://somehost:42\", \"environment\": \"env test\"}")
                    .contentType(MediaType.APPLICATION_JSON)
            )
            .andDo(MockMvcResultHandlers.log())
            .andExpect(status().isConflict());
    }

    @Test
    public void deleteTarget_deletes_it_from_repo() throws Exception {
        addAvailableEnvironment("env_test", "server 1");

        mockMvc.perform(delete(environmentBasePath + "/env_test/targets/server 1"))
            .andDo(MockMvcResultHandlers.log())
            .andExpect(status().isOk());

        verify(environmentRepository, times(1)).save(eq(Environment.builder().withName("env_test").withDescription("env_test description").build()));
    }

    @Test
    public void deleteTarget_returns_404_when_not_found() throws Exception {
        addAvailableEnvironment("env_test", "server 1");

        mockMvc.perform(delete(environmentBasePath + "/env_test/targets/server 2"))
            .andDo(MockMvcResultHandlers.log())
            .andExpect(status().isNotFound());
    }

    @Test
    public void updateTarget_returns_404_when_not_found() throws Exception {
        addAvailableEnvironment("env test", "server 1");

        mockMvc.perform(
                put(targetBasePath + "/server 2")
                    .content("{\"name\": \"server 2\", \"url\": \"http://somehost2:42\" , \"environment\": \"env test\"}")
                    .contentType(MediaType.APPLICATION_JSON))
            .andDo(MockMvcResultHandlers.log())
            .andExpect(status().isNotFound());
    }

    @Test
    public void updateTarget_saves_it() throws Exception {
        addAvailableEnvironment("env_test", "server 1");

        mockMvc.perform(
                put(targetBasePath + "/server 1")
                    .content("{\"name\": \"server 1\", \"url\": \"http://somehost2:42\", \"environment\": \"env_test\"}")
                    .contentType(MediaType.APPLICATION_JSON)
            )
            .andDo(MockMvcResultHandlers.log())
            .andExpect(status().isOk());

        ArgumentCaptor<Environment> environmentArgumentCaptor = ArgumentCaptor.forClass(Environment.class);
        verify(environmentRepository, times(1)).save(environmentArgumentCaptor.capture());

        Environment savedEnvironment = environmentArgumentCaptor.getValue();
        assertThat(savedEnvironment).isNotNull();
        assertThat(savedEnvironment.targets).hasSize(1);
        assertThat(savedEnvironment.targets.iterator().next().url).isEqualTo("http://somehost2:42");
    }

    @Test
    public void updateTarget_with_different_name_deletes_previous_one() throws Exception {
        addAvailableEnvironment("env_test", "server 1");

        mockMvc.perform(
                put(targetBasePath + "/server 1")
                    .content("{\"name\": \"server 2\", \"url\": \"http://somehost2:42\", \"environment\": \"env_test\"}")
                    .contentType(MediaType.APPLICATION_JSON))
            .andDo(MockMvcResultHandlers.log())
            .andExpect(status().isOk());

        ArgumentCaptor<Environment> environmentArgumentCaptor = ArgumentCaptor.forClass(Environment.class);
        verify(environmentRepository, times(1)).save(environmentArgumentCaptor.capture());

        Environment savedEnvironment = environmentArgumentCaptor.getValue();
        assertThat(savedEnvironment).isNotNull();
        assertThat(savedEnvironment.targets).hasSize(1);
        assertThat(savedEnvironment.targets.iterator().next().name).isEqualTo("server 2");
    }

    private void addAvailableEnvironment(String envName, String... targetNames) {

        Set<Target> targets = stream(targetNames)
            .map(targetName -> Target.builder()
                .withName(targetName)
                .withEnvironment(envName)
                .withUrl("http://" + targetName.replace(' ', '_') + ":43")
                .build())
            .collect(toCollection(LinkedHashSet::new));

        registeredEnvironments.put(
            envName,
            Environment.builder()
                .withName(envName)
                .withDescription(envName + " description")
                .withTargets(targets)
                .build()
        );

        when(environmentRepository.findByName(eq(envName)))
            .thenAnswer(iom -> {
                    String envNameParam = iom.getArgument(0);
                    if (!registeredEnvironments.containsKey(envNameParam)) {
                        throw new EnvironmentNotFoundException("test env not found");
                    }
                    return registeredEnvironments.get(envNameParam);
                }
            );

        when(environmentRepository.listNames())
            .thenReturn(new ArrayList<>(registeredEnvironments.keySet()));
    }
}
