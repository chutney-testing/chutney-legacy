package com.chutneytesting.design.graphql.scenario;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.chutneytesting.design.api.scenario.v2_0.dto.RawTestCaseRequestDto;
import com.chutneytesting.design.infra.storage.scenario.TestCaseRepositoryAggregator;
import com.chutneytesting.security.api.UserDto;
import com.chutneytesting.security.infra.SpringUserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.graphql.spring.boot.test.GraphQLTestTemplate;
import java.io.IOException;
import java.util.Arrays;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ScenarioMutationResolverIntegrationTest {

    public static final String GRAPHQL_SAVE_SCENARIO_RESOURCE = "/graphql/save-scenario.graphql";
    @Autowired
    private GraphQLTestTemplate graphQLTestTemplate;
    @Autowired
    private ObjectMapper objectMapper;
    @MockBean
    private TestCaseRepositoryAggregator scenarioRepository;
    @MockBean
    private SpringUserService userService;

    private static final String SCENARIO_CONTENT = "{\n" +
        "  \"givens\": [\n" +
        "    {\n" +
        "      \"description\": \"step description\",\n" +
        "      \"implementation\": {\n" +
        "        \"type\": \"success\",\n" +
        "        \"inputs\": {},\n" +
        "        \"outputs\": {},\n" +
        "        \"validations\": {}\n" +
        "      }\n" +
        "    }\n" +
        "  ],\n" +
        "  \"when\": {},\n" +
        "  \"thens\": []\n" +
        "}";

    @Test
    void should_create_scenario() throws IOException {
        // GIVEN
        when(scenarioRepository.save(any())).thenReturn("1");
        when(userService.currentUser()).thenReturn(new UserDto());


        RawTestCaseRequestDto scenario = new RawTestCaseRequestDto(null,
            SCENARIO_CONTENT,
            "scenario title",
            "scenario description",
            Arrays.asList("DRAFT"));
        final ObjectNode params = objectMapper.createObjectNode();
        params.set("saveScenarioInput", objectMapper.valueToTree(scenario));

        // WHEN - THEN
        graphQLTestTemplate
            .perform(GRAPHQL_SAVE_SCENARIO_RESOURCE, params)
            .assertThatNoErrorsArePresent()
            .assertThatField("$.data.save")
            .asString()
            .isEqualTo("1");
        // THEN
        verify(scenarioRepository, times(1)).save(any());
    }
}
