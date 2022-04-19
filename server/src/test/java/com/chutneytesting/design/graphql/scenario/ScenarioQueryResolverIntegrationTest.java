package com.chutneytesting.design.graphql.scenario;

import static java.time.Instant.now;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.chutneytesting.design.api.scenario.v2_0.dto.TestCaseIndexDto;
import com.chutneytesting.design.domain.scenario.TestCaseMetadataImpl;
import com.chutneytesting.design.infra.storage.scenario.TestCaseRepositoryAggregator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.graphql.spring.boot.test.GraphQLTestTemplate;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ScenarioQueryResolverIntegrationTest {

    @Autowired
    private GraphQLTestTemplate graphQLTestTemplate;
    @Autowired
    private ObjectMapper objectMapper;
    @MockBean
    private TestCaseRepositoryAggregator scenarioRepository;

    private static final String SEARCH_SCENARIO_GRAPHQL_RESOURCE = "/graphql/search-scenarios.graphql";

    @Test
    void should_list_all_scenarios() throws IOException {
        // GIVEN
        TestCaseMetadataImpl testCaseMetadata = TestCaseMetadataImpl.builder()
            .withId("1")
            .withTitle("scenario title")
            .withDescription("scenario description")
            .withCreationDate(now())
            .withUpdateDate(now())
            .withVersion(1)
            .build();
        when(scenarioRepository.findAll()).thenReturn(Arrays.asList(testCaseMetadata));

        // When
        final  List<TestCaseIndexDto> scenarios = graphQLTestTemplate.postForResource(SEARCH_SCENARIO_GRAPHQL_RESOURCE).getList("$.data.search", TestCaseIndexDto.class);

        // THEN
        assertThat(scenarios).hasSize(1);
        assertThat(scenarios.get(0).metadata().title()).isEqualTo("scenario title");
    }

    @Test
    void should_search_scenarios() throws IOException {
        // GIVEN
        TestCaseMetadataImpl F140TestCaseMetadata = TestCaseMetadataImpl.builder()
            .withId("2")
            .withTitle("F140")
            .withDescription("scenario description")
            .withCreationDate(now())
            .withUpdateDate(now())
            .withVersion(1)
            .build();
        when(scenarioRepository.search("F140")).thenReturn(Arrays.asList(F140TestCaseMetadata));

        final ObjectNode params = objectMapper.createObjectNode();
        params.put("keywordInput", "F140");

        // When
        final  List<TestCaseIndexDto> scenarios = graphQLTestTemplate.perform(SEARCH_SCENARIO_GRAPHQL_RESOURCE, params).getList("$.data.search", TestCaseIndexDto.class);

        // THEN
        assertThat(scenarios).hasSize(1);
        assertThat(scenarios.get(0).metadata().title()).isEqualTo("F140");
    }
}
