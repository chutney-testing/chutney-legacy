package com.chutneytesting.agent.api.mapper;

import static com.chutneytesting.agent.domain.configure.ImmutableNetworkConfiguration.AgentNetworkConfiguration.builder;
import static java.util.Collections.emptyList;
import static java.util.Collections.singleton;
import static org.assertj.core.api.Assertions.assertThat;

import com.chutneytesting.agent.api.dto.NetworkConfigurationApiDto;
import com.chutneytesting.agent.api.dto.NetworkConfigurationApiDto.EnvironmentApiDto;
import com.chutneytesting.agent.api.dto.NetworkConfigurationApiDto.SecurityApiDto;
import com.chutneytesting.agent.api.dto.NetworkConfigurationApiDto.TargetsApiDto;
import com.chutneytesting.agent.domain.configure.ImmutableNetworkConfiguration;
import com.chutneytesting.agent.domain.configure.ImmutableNetworkConfiguration.AgentNetworkConfiguration;
import com.chutneytesting.agent.domain.configure.NetworkConfiguration;
import com.chutneytesting.design.domain.environment.Environment;
import com.chutneytesting.design.domain.environment.SecurityInfo;
import com.chutneytesting.design.domain.environment.Target;
import com.chutneytesting.engine.domain.delegation.NamedHostAndPort;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import org.assertj.core.api.Condition;
import org.junit.Test;

public class NetworkConfigurationApiMapperTest {

    private NetworkConfigurationApiMapper networkConfigurationApiMapper = new NetworkConfigurationApiMapper(new AgentInfoApiMapper(), new EnvironmentApiMapper());

    @Test
    public void fromdto() {
        NetworkConfigurationApiDto dto = new NetworkConfigurationApiDto();
        dto.creationDate = Instant.now();
        NetworkConfigurationApiDto.AgentInfoApiDto agentInfoApiDto = new NetworkConfigurationApiDto.AgentInfoApiDto();
        agentInfoApiDto.name = "name";
        agentInfoApiDto.host = "host";
        agentInfoApiDto.port = 1000;
        dto.agentNetworkConfiguration = new LinkedHashSet<>(Collections.singletonList(agentInfoApiDto));

        SecurityApiDto security = new SecurityApiDto("user1", null, null, null, null, null);
        TargetsApiDto targetsApiDto = new TargetsApiDto("s1", "proto://host:12/lol", null, security);

        LinkedHashSet<TargetsApiDto> targetSet = new LinkedHashSet<>();
        targetSet.add(targetsApiDto);
        EnvironmentApiDto envDto = new EnvironmentApiDto("name", targetSet);
        LinkedHashSet<EnvironmentApiDto> envSet = new LinkedHashSet<>();
        envSet.add(envDto);
        dto.environmentsConfiguration = envSet;
        new LinkedHashSet<>(Collections.singletonList(targetsApiDto));

        NetworkConfiguration networkConfiguration = networkConfigurationApiMapper.fromDto(dto);

        assertThat(networkConfiguration.creationDate()).isEqualTo(dto.creationDate);
        Set<NamedHostAndPort> agentInfos = networkConfiguration.agentNetworkConfiguration().agentInfos();
        assertThat(agentInfos).hasSize(1);
        assertThat(agentInfos).haveExactly(1, new Condition<>(agentInfo -> "name".equals(agentInfo.name()), "agent with name"));
        assertThat(agentInfos).haveExactly(1, new Condition<>(agentInfo -> "host".equals(agentInfo.host()), "agent with host"));
        assertThat(agentInfos).haveExactly(1, new Condition<>(agentInfo -> agentInfo.port() == 1000, "agent with port 1000"));

        Set<Environment> env = networkConfiguration.environmentConfiguration().environments();
        assertThat(env).hasSize(1);
        Environment environment = env.iterator().next();

        List<Target> targets = environment.targets;
        assertThat(targets).hasSize(1);

        Target singleValue = targets.iterator().next();
        assertThat(singleValue.name).as("target name").isEqualTo("s1");
        assertThat(singleValue.url).as("target url").isEqualTo("proto://host:12/lol");
        assertThat(singleValue.properties).as("target properties").isEmpty();
        assertThat(singleValue.security.credential).as("target security").isEqualTo(SecurityInfo.Credential.of("user1", ""));
    }

    @Test
    public void fromdtoAtNow_use_another_instant() throws InterruptedException {
        NetworkConfigurationApiDto dto = new NetworkConfigurationApiDto();
        dto.creationDate = Instant.now();
        NetworkConfigurationApiDto.AgentInfoApiDto agentInfoApiDto = new NetworkConfigurationApiDto.AgentInfoApiDto();
        agentInfoApiDto.name = "name";
        agentInfoApiDto.host = "host";
        agentInfoApiDto.port = 1000;
        dto.agentNetworkConfiguration = new LinkedHashSet<>(Collections.singletonList(agentInfoApiDto));

        SecurityApiDto securityApiDto = new SecurityApiDto("sa", "", null, null, null, null);
        TargetsApiDto targetsApiDto = new TargetsApiDto("s1", "proto://host:1/lol", new HashMap<>(), securityApiDto);
        EnvironmentApiDto envApiDto = new EnvironmentApiDto("envName", singleton(targetsApiDto));
        dto.environmentsConfiguration = new LinkedHashSet(Collections.singletonList(envApiDto));


        Thread.sleep(1);
        NetworkConfiguration networkConfiguration = networkConfigurationApiMapper.fromDtoAtNow(dto);

        assertThat(networkConfiguration.creationDate()).isNotEqualTo(dto.creationDate);
    }

    @Test
    public void toDTO_basic_test() {

        Set<Target> targets = new LinkedHashSet<>();
        targets.add(Target.builder()
            .withId(Target.TargetId.of("s2", "env"))
            .withUrl("pro://host2:45/lol")
            .build());

        Environment env = Environment.builder().withName("name").withTargets(targets).build();

        ImmutableNetworkConfiguration networkConfiguration = ImmutableNetworkConfiguration.builder()
            .creationDate(Instant.now())
            .agentNetworkConfiguration(builder()
                .addAgentInfos(new NamedHostAndPort("name", "host", 1000))
                .build())
            .environmentConfiguration(ImmutableNetworkConfiguration.EnvironmentConfiguration.builder()
            .addEnvironments(env).build())
            .build();


        NetworkConfigurationApiDto dto = networkConfigurationApiMapper.toDto(networkConfiguration);

        assertThat(dto.creationDate).isEqualTo(networkConfiguration.creationDate());
        assertThat(dto.agentNetworkConfiguration).hasSize(1);
        assertThat(dto.agentNetworkConfiguration).haveExactly(1, new Condition<>(agentInfoDto ->
            "name".equals(agentInfoDto.name) &&
                "host".equals(agentInfoDto.host) &&
                agentInfoDto.port == 1000,
            "right agent"));

        assertThat(dto.environmentsConfiguration).hasSize(1);
        EnvironmentApiDto envToAssert = dto.environmentsConfiguration.iterator().next();
        assertThat(envToAssert.targetsConfiguration).hasSize(1);
        TargetsApiDto targetsApiDto = envToAssert.targetsConfiguration.iterator().next();
        assertThat(targetsApiDto.name).as("DTO target name").isEqualTo("s2");
        assertThat(targetsApiDto.url).as("DTO target url").isEqualTo("pro://host2:45/lol");
        assertThat(targetsApiDto.properties).as("DTO target properties").isEmpty();
        assertThat(targetsApiDto.security).as("DTO target security").isNotNull();
    }

    @Test
    public void enhanceWithEnvironment_basic_test() {

        Environment env = Environment.builder().withName("env_name").build();

        NetworkConfiguration networkConfiguration = ImmutableNetworkConfiguration.builder()
            .creationDate(Instant.now())
            .agentNetworkConfiguration(AgentNetworkConfiguration.builder()
                .addAgentInfos(new NamedHostAndPort("name", "host", 1000))
                .build())
            .environmentConfiguration(ImmutableNetworkConfiguration.EnvironmentConfiguration.builder()
                .addEnvironments(env).build())
            .build();

        Set<Target> targets = new LinkedHashSet<>();
        targets.add(Target.builder()
            .withId(Target.TargetId.of("APP_1", "env_name"))
            .withUrl("https://host_of_app:443/api")
            .withAgents(emptyList())
            .build());
        Environment newEnv = Environment.builder().withName("env_name").withTargets(targets).build();
        List<Environment> newEnvs = new ArrayList<>();
        newEnvs.add(newEnv);
        NetworkConfiguration enhancedNetworkConfiguration = networkConfigurationApiMapper.enhanceWithEnvironment(networkConfiguration, newEnvs);

        assertThat(enhancedNetworkConfiguration.agentNetworkConfiguration().agentInfos())
            .as("Agent infos")
            .hasSize(1);

        assertThat(enhancedNetworkConfiguration.environmentConfiguration().environments()).hasSize(1);
        Environment environment = enhancedNetworkConfiguration.environmentConfiguration().environments().iterator().next();
        assertThat(environment.targets)
            .as("Target infos")
            .hasSize(1)
            .extracting(targetInfo -> targetInfo.name + " -> " + targetInfo.url)
            .containsExactlyInAnyOrder("APP_1 -> https://host_of_app:443/api");
    }
}
