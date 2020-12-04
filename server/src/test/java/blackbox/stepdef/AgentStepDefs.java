package blackbox.stepdef;

import static java.util.Collections.singleton;
import static org.assertj.core.api.Assertions.assertThat;

import blackbox.restclient.RestClient;
import com.chutneytesting.agent.api.dto.AgentApiDto;
import com.chutneytesting.agent.api.dto.NetworkConfigurationApiDto;
import com.chutneytesting.agent.api.dto.NetworkConfigurationApiDto.EnvironmentApiDto;
import com.chutneytesting.agent.api.dto.NetworkConfigurationApiDto.TargetsApiDto;
import com.chutneytesting.agent.api.dto.NetworkDescriptionApiDto;
import com.chutneytesting.environment.api.dto.EnvironmentMetadataDto;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import java.io.IOException;
import java.net.InetAddress;
import java.util.Collections;
import java.util.Set;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;

public class AgentStepDefs {

    private final RestClient restClient;
    private ObjectMapper objectMapper;

    @Value("${server.port}")
    private int port;

    public AgentStepDefs(RestClient restClient) {
        this.restClient = restClient;
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new Jdk8Module());
        objectMapper.registerModule(new JavaTimeModule());
    }

    @When("^network configuration with target (.*) with url (.*) is received$")
    public void test_propagation(String name, String url) throws IOException {
        final ResponseEntity<String> responseDescription = restClient.defaultRequest()
            .withUrl("/api/v1/description")
            .get(String.class);

        NetworkDescriptionApiDto networkDescription = objectMapper.readValue(responseDescription.getBody(), NetworkDescriptionApiDto.class);
        TargetsApiDto fake_target = new TargetsApiDto(name, url, null, null);
        EnvironmentApiDto environment = new EnvironmentApiDto("GLOBAL", singleton(fake_target));
        networkDescription.networkConfiguration.environmentsConfiguration.add(environment);
        AgentApiDto localAgent = new AgentApiDto();
        localAgent.info = new NetworkConfigurationApiDto.AgentInfoApiDto();
        localAgent.info.name = "Agent local";
        localAgent.info.host = InetAddress.getLocalHost().getCanonicalHostName();
        localAgent.info.port = port;
        localAgent.reachableAgents = Collections.emptyList();
        localAgent.reachableTargets = Collections.emptyList();
        networkDescription.agentsGraph.agents.add(localAgent);

        restClient.defaultRequest()
            .withUrl("/api/v1/agentnetwork/wrapup")
            .withBody(networkDescription)
            .post(String.class);
    }

    @Then("^target (.*) is saved locally$")
    public void verify_target_exist(String name) throws IOException {
        final ResponseEntity<String> responseDescription = restClient.defaultRequest()
            .withUrl("/api/v2/environment")
            .get(String.class);

        Set<EnvironmentMetadataDto> environments = objectMapper.readValue(responseDescription.getBody(), new TypeReference<Set<EnvironmentMetadataDto>>() {
        });
        assertThat(environments).hasSize(1);
        assertThat(environments.iterator().next().targets).hasSize(1);
        assertThat(environments.iterator().next().targets.get(0).name).isEqualTo(name);
    }


}
