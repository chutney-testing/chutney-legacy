package com.chutneytesting.design.api.environment;

import static java.util.Arrays.stream;
import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toCollection;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
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

import com.chutneytesting.RestExceptionHandler;
import com.chutneytesting.design.domain.environment.Environment;
import com.chutneytesting.design.domain.environment.EnvironmentNotFoundException;
import com.chutneytesting.design.domain.environment.EnvironmentRepository;
import com.chutneytesting.design.domain.environment.EnvironmentService;
import com.chutneytesting.design.domain.environment.InvalidEnvironmentNameException;
import com.chutneytesting.design.domain.environment.Target;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import org.assertj.core.util.Lists;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

@RunWith(JUnitParamsRunner.class)
public class EnvironmentControllerV2Test {

    private final String basePath = "/api/v2/environment";

    private final EnvironmentRepository environmentRepository = mock(EnvironmentRepository.class);
    private final EnvironmentService environmentService = new EnvironmentService(environmentRepository);
    private final EnvironmentControllerV2 environmentControllerV2 = new EnvironmentControllerV2(environmentService);
    private final MockMvc mockMvc = MockMvcBuilders.standaloneSetup(environmentControllerV2)
        .setControllerAdvice(new RestExceptionHandler())
        .build();

    @Test
    @Parameters({
        "",
        "env1, env2",
        "c, b, a"
    })
    public void listEnvironments_returns_all_available(String[] environmentNames) throws Exception {
        // Given existing env and targets
        stream(environmentNames).forEach(this::addAvailableEnvironment);

        ResultActions resultActions = mockMvc.perform(get(basePath))
            .andDo(MockMvcResultHandlers.log())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()", equalTo(environmentNames.length)));

        List<String> expectedEnvNames = stream(environmentNames)
            .sorted(Comparator.naturalOrder())
            .collect(Collectors.toList());

        for (int i = 0; i < expectedEnvNames.size(); i++) {
            resultActions.andExpect(jsonPath("$.[" + i + "].description", equalTo(expectedEnvNames.get(i) + " description")));
        }
    }

    @Test
    public void createEnvironment_adds_it_to_repository() throws Exception {
        mockMvc.perform(
            post(basePath)
                .content("{\"name\": \"env test\", \"description\": \"test description\"}")
                .contentType(MediaType.APPLICATION_JSON_UTF8)
        )
            .andDo(MockMvcResultHandlers.log())
            .andExpect(status().isOk());

        ArgumentCaptor<Environment> environmentArgumentCaptor = ArgumentCaptor.forClass(Environment.class);
        verify(environmentRepository, times(1)).save(environmentArgumentCaptor.capture());

        Environment savedEnvironment = environmentArgumentCaptor.getValue();
        assertThat(savedEnvironment).isNotNull();
        assertThat(savedEnvironment.name).isEqualTo("env test");
        assertThat(savedEnvironment.description).isEqualTo("test description");
    }

    @Test
    public void createEnvironment_returns_400_when_name_is_invalid() throws Exception {
        doThrow(new InvalidEnvironmentNameException("message")).when(environmentRepository).save(any());
        mockMvc.perform(
            post(basePath)
                .content("{\"name\": \"env test\", \"description\": \"test description\"}")
                .contentType(MediaType.APPLICATION_JSON_UTF8)
        )
            .andDo(MockMvcResultHandlers.log())
            .andExpect(status().isBadRequest());
    }

    @Test
    public void createEnvironment_returns_409_when_already_existing() throws Exception {
        addAvailableEnvironment("env test");

        mockMvc.perform(
            post(basePath)
                .content("{\"name\": \"env test\", \"description\": \"test description\"}")
                .contentType(MediaType.APPLICATION_JSON_UTF8)
        )
            .andDo(MockMvcResultHandlers.log())
            .andExpect(status().isConflict());
    }

    @Test
    public void deleteEnvironment_deletes_it_from_repo() throws Exception {
        mockMvc.perform(delete(basePath + "/env test"))
            .andDo(MockMvcResultHandlers.log())
            .andExpect(status().isOk());

        verify(environmentRepository, times(1)).delete(eq("env test"));
    }

    @Test
    public void deleteEnvironment_returns_404_when_not_found() throws Exception {
        doThrow(new EnvironmentNotFoundException("message")).when(environmentRepository).delete(any());

        mockMvc.perform(delete(basePath + "/env test"))
            .andDo(MockMvcResultHandlers.log())
            .andExpect(status().isNotFound());
    }

    @Test
    public void updateEnvironment_returns_404_when_not_found() throws Exception {
        when(environmentRepository.findByName(any())).thenThrow(new EnvironmentNotFoundException("message"));

        mockMvc.perform(
            put(basePath + "/env test")
                .content("{\"name\": \"env test\", \"description\": \"test description\"}")
                .contentType(MediaType.APPLICATION_JSON_UTF8)
        )
            .andDo(MockMvcResultHandlers.log())
            .andExpect(status().isNotFound());
    }

    @Test
    public void updateEnvironment_saves_it() throws Exception {
        addAvailableEnvironment("env test");

        mockMvc.perform(
            put(basePath + "/env test")
                .content("{\"name\": \"env test\", \"description\": \"test description\"}")
                .contentType(MediaType.APPLICATION_JSON_UTF8)
        )
            .andDo(MockMvcResultHandlers.log())
            .andExpect(status().isOk());

        ArgumentCaptor<Environment> environmentArgumentCaptor = ArgumentCaptor.forClass(Environment.class);
        verify(environmentRepository, times(1)).save(environmentArgumentCaptor.capture());
        verify(environmentRepository, times(0)).delete(any());

        Environment savedEnvironment = environmentArgumentCaptor.getValue();
        assertThat(savedEnvironment).isNotNull();
        assertThat(savedEnvironment.name).isEqualTo("env test");
        assertThat(savedEnvironment.description).isEqualTo("test description");
    }

    @Test
    public void updateEnvironment_with_different_a_name_deletes_previous_one() throws Exception {
        addAvailableEnvironment("env test");

        mockMvc.perform(
            put(basePath + "/env test")
                .content("{\"description\": \"test2 description\", \"name\": \"env test 2\"}")
                .contentType(MediaType.APPLICATION_JSON_UTF8)
        )
            .andDo(MockMvcResultHandlers.log())
            .andExpect(status().isOk());

        ArgumentCaptor<Environment> environmentArgumentCaptor = ArgumentCaptor.forClass(Environment.class);
        verify(environmentRepository, times(1)).save(environmentArgumentCaptor.capture());
        verify(environmentRepository, times(1)).delete(eq("env test"));

        Environment savedEnvironment = environmentArgumentCaptor.getValue();
        assertThat(savedEnvironment).isNotNull();
        assertThat(savedEnvironment.name).isEqualTo("env test 2");
        assertThat(savedEnvironment.description).isEqualTo("test2 description");
    }

    @Test
    @Parameters({
        "",
        "target1, target2"
    })
    public void listTargets_returns_all_available(String[] targetNames) throws Exception {
        addAvailableEnvironment("env test", targetNames);

        ResultActions resultActions = mockMvc.perform(get(basePath + "/env test/target"))
            .andDo(MockMvcResultHandlers.log())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()", equalTo(targetNames.length)));

        for (int i = 0; i < targetNames.length; i++) {
            resultActions.andExpect(jsonPath("$.[" + i + "].name", equalTo(targetNames[i])))
                .andExpect(jsonPath("$.[" + i + "].url", equalTo("http://" + targetNames[i] + ":43")));
        }
    }

    @Test
    public void should_list_distinct_targets_in_any_environment() throws Exception {
        List<String> targetsNames = Lists.list("t1", "t2", "t3");
        addAvailableEnvironment("env1", targetsNames.get(0), targetsNames.get(2));
        addAvailableEnvironment("env2", targetsNames.get(0), targetsNames.get(1));

        ResultActions resultActions = mockMvc.perform(get(basePath + "/target"))
            .andDo(MockMvcResultHandlers.log())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(3)));

        for (String targetName : targetsNames) {
            resultActions.andExpect(jsonPath("$[?(@.name=='"+targetName+"')]", hasSize(1)));
        }
    }

    @Test
    public void addTarget_saves_an_environment_with_the_new_target() throws Exception {
        addAvailableEnvironment("env test", "server 1");

        mockMvc.perform(
            post(basePath + "/env test/target")
                .content("{\"name\": \"server 2\", \"url\": \"ssh://somehost:42\"}")
                .contentType(MediaType.APPLICATION_JSON_UTF8)
        )
            .andDo(MockMvcResultHandlers.log())
            .andExpect(status().isOk());

        ArgumentCaptor<Environment> environmentArgumentCaptor = ArgumentCaptor.forClass(Environment.class);
        verify(environmentRepository, times(1)).save(environmentArgumentCaptor.capture());

        Environment savedEnvironment = environmentArgumentCaptor.getValue();
        assertThat(savedEnvironment).isNotNull();
        assertThat(savedEnvironment.targets).hasSize(2);
        assertThat(savedEnvironment.targets.get(1).name).isEqualTo("server 2");
        assertThat(savedEnvironment.targets.get(1).url).isEqualTo("ssh://somehost:42");
    }

    @Test
    public void addTarget_returns_409_when_already_existing() throws Exception {
        addAvailableEnvironment("env test", "server 1");

        mockMvc.perform(
            post(basePath + "/env test/target")
                .content("{\"name\": \"server 1\", \"url\": \"ssh://somehost:42\"}")
                .contentType(MediaType.APPLICATION_JSON_UTF8)
        )
            .andDo(MockMvcResultHandlers.log())
            .andExpect(status().isConflict());
    }

    @Test
    public void deleteTarget_deletes_it_from_repo() throws Exception {
        addAvailableEnvironment("env test", "server 1");

        mockMvc.perform(delete(basePath + "/env test/target/server 1"))
            .andDo(MockMvcResultHandlers.log())
            .andExpect(status().isOk());

        verify(environmentRepository, times(1)).save(eq(Environment.builder().withName("env test").withDescription("env test description").build()));
    }

    @Test
    public void deleteTarget_returns_404_when_not_found() throws Exception {
        addAvailableEnvironment("env test", "server 1");

        mockMvc.perform(delete(basePath + "/env test/target/server 2"))
            .andDo(MockMvcResultHandlers.log())
            .andExpect(status().isNotFound());
    }

    @Test
    public void updateTarget_returns_404_when_not_found() throws Exception {
        addAvailableEnvironment("env test", "server 1");

        mockMvc.perform(
            put(basePath + "/env test/target/server 2")
                .content("{\"name\": \"server 2\", \"url\": \"http://somehost2:42\"}")
                .contentType(MediaType.APPLICATION_JSON_UTF8)
        )
            .andDo(MockMvcResultHandlers.log())
            .andExpect(status().isNotFound());
    }

    @Test
    public void updateTarget_saves_it() throws Exception {
        addAvailableEnvironment("env test", "server 1");

        mockMvc.perform(
            put(basePath + "/env test/target/server 1")
                .content("{\"name\": \"server 1\", \"url\": \"http://somehost2:42\"}")
                .contentType(MediaType.APPLICATION_JSON_UTF8)
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
    public void updateTarget_with_different_a_name_deletes_previous_one() throws Exception {
        addAvailableEnvironment("env test", "server 1");

        mockMvc.perform(
            put(basePath + "/env test/target/server 1")
                .content("{\"name\": \"server 2\", \"url\": \"http://somehost2:42\"}")
                .contentType(MediaType.APPLICATION_JSON_UTF8)
        )
            .andDo(MockMvcResultHandlers.log())
            .andExpect(status().isOk());

        ArgumentCaptor<Environment> environmentArgumentCaptor = ArgumentCaptor.forClass(Environment.class);
        verify(environmentRepository, times(1)).save(environmentArgumentCaptor.capture());

        Environment savedEnvironment = environmentArgumentCaptor.getValue();
        assertThat(savedEnvironment).isNotNull();
        assertThat(savedEnvironment.targets).hasSize(1);
        assertThat(savedEnvironment.targets.iterator().next().name).isEqualTo("server 2");
    }

    Map<String, Environment> registeredEnvironments = new LinkedHashMap<>();

    private void addAvailableEnvironment(String envName, String... targetNames) {

        Set<Target> targets = stream(targetNames)
            .map(targetName -> Target.builder()
                .withId(Target.TargetId.of(targetName, envName))
                .withUrl("http://" + targetName.replace(' ', '_') + ":43")
                .withAgents(emptyList())
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
