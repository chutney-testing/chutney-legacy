package com.chutneytesting.campaign.api;

import static java.lang.Integer.MAX_VALUE;
import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;
import static java.util.Collections.singletonList;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static util.WaitUtils.awaitDuring;

import com.chutneytesting.RestExceptionHandler;
import com.chutneytesting.WebConfiguration;
import com.chutneytesting.campaign.api.dto.CampaignDto;
import com.chutneytesting.campaign.api.dto.CampaignExecutionReportDto;
import com.chutneytesting.campaign.infra.FakeCampaignRepository;
import com.chutneytesting.execution.domain.campaign.CampaignExecutionEngine;
import com.chutneytesting.scenario.api.raw.dto.ImmutableTestCaseIndexDto;
import com.chutneytesting.scenario.api.raw.dto.TestCaseIndexDto;
import com.chutneytesting.scenario.infra.TestCaseRepositoryAggregator;
import com.chutneytesting.server.core.domain.execution.history.ExecutionHistory;
import com.chutneytesting.server.core.domain.instrument.ChutneyMetrics;
import com.chutneytesting.server.core.domain.scenario.TestCaseMetadataImpl;
import com.chutneytesting.server.core.domain.scenario.campaign.CampaignExecutionReport;
import com.chutneytesting.server.core.domain.scenario.campaign.ScenarioExecutionReportCampaign;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.assertj.core.api.Assertions;
import org.assertj.core.util.Lists;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

public class CampaignControllerTest {

    private static final CampaignDto SAMPLE_CAMPAIGN = new CampaignDto(null, "test", "desc", Lists.newArrayList("1", "2", "3"),
        emptyMap(), emptyList(), "env", false, false, null, emptyList());
    private static final String urlTemplate = "/api/ui/campaign/v1/";

    private final FakeCampaignRepository repository = new FakeCampaignRepository();
    private TestCaseRepositoryAggregator repositoryAggregator;

    private final CampaignExecutionEngine campaignExecutionEngine = mock(CampaignExecutionEngine.class);
    private MockMvc mockMvc;
    private ResultExtractor resultExtractor;
    private CampaignDto existingCampaign;
    private final ObjectMapper om = new WebConfiguration().objectMapper();

    @BeforeEach
    public void setUp() throws Exception {
        resultExtractor = new ResultExtractor();

        repositoryAggregator = mock(TestCaseRepositoryAggregator.class);
        CampaignController campaignController = new CampaignController(repositoryAggregator, repository, campaignExecutionEngine);
        mockMvc = MockMvcBuilders.standaloneSetup(campaignController)
            .setControllerAdvice(new RestExceptionHandler(Mockito.mock(ChutneyMetrics.class)))
            .build();

        createExistingCampaign();
    }

    @Test
    public void should_create_a_new_campaign() throws Exception {
        // When
        CampaignDto actual = insertCampaign(SAMPLE_CAMPAIGN);

        // Then
        assertThat(actual).usingRecursiveComparison().ignoringFields("id").isEqualTo(SAMPLE_CAMPAIGN);
        assertThat(actual.getId()).isNotNull();
    }

    @Test
    public void should_not_found_campaign_when_it_does_not_exist() throws Exception {
        execute(MockMvcRequestBuilders.get(urlTemplate + MAX_VALUE))
            .andExpect(MockMvcResultMatchers.status().isNotFound());
    }

    @Test
    public void should_found_campaign_when_it_exists() throws Exception {
        execute(MockMvcRequestBuilders.get(urlTemplate + existingCampaign.getId()))
            .andExpect(MockMvcResultMatchers.status().isOk());
        CampaignDto receivedCampaign = resultExtractor.campaign();
        assertThat(receivedCampaign).usingRecursiveComparison().isEqualTo(existingCampaign);
    }

    @Test
    public void should_update_campaign() throws Exception {
        // Given
        String updatedTitle = "MODIFIED TITLE";

        CampaignDto modifiedCampaign = new CampaignDto(existingCampaign.getId(),
            updatedTitle,
            existingCampaign.getDescription(),
            existingCampaign.getScenarioIds(),
            new HashMap<>(),
            existingCampaign.getCampaignExecutionReports(),
            existingCampaign.getEnvironment(), false, false, null, emptyList());

        // When
        CampaignDto receivedCampaign = insertCampaign(modifiedCampaign);

        // Then
        assertThat(receivedCampaign.getTitle()).isEqualTo(updatedTitle);
        assertThat(receivedCampaign.getDatasetId()).isNull();
        assertThat(receivedCampaign).usingRecursiveComparison().ignoringFields("title", "datasetId").isEqualTo(existingCampaign);
    }

    @Test
    public void should_update_campaign_scenarios() throws Exception {
        // Given
        List<String> updatedScenarioIds = new ArrayList<>();
        CampaignDto modifiedCampaign = new CampaignDto(existingCampaign.getId(),
            existingCampaign.getTitle(),
            existingCampaign.getDescription(),
            updatedScenarioIds,
            emptyMap(),
            existingCampaign.getCampaignExecutionReports(),
            existingCampaign.getEnvironment(), false, false, null, emptyList());

        // When
        CampaignDto receivedCampaign = insertCampaign(modifiedCampaign);

        // Then
        assertThat(receivedCampaign.getScenarioIds()).isEqualTo(updatedScenarioIds);
        assertThat(receivedCampaign.getDatasetId()).isNull();
        assertThat(receivedCampaign).usingRecursiveComparison().ignoringFields("scenarioIds", "datasetId").isEqualTo(existingCampaign);
    }

    @Test
    public void should_update_campaign_tags() throws Exception {
        // Given
        CampaignDto modifiedCampaign = new CampaignDto(existingCampaign.getId(),
            existingCampaign.getTitle(),
            existingCampaign.getDescription(),
            existingCampaign.getScenarioIds(),
            new HashMap<>(),
            existingCampaign.getCampaignExecutionReports(),
            existingCampaign.getEnvironment(), false, false, null, Arrays.asList("Tag"));

        // When
        CampaignDto receivedCampaign = insertCampaign(modifiedCampaign);

        // Then
        assertThat(receivedCampaign.getTags()).containsExactly("TAG");
        assertThat(receivedCampaign.getDatasetId()).isNull();
        assertThat(receivedCampaign).usingRecursiveComparison().ignoringFields("tags", "datasetId").isEqualTo(existingCampaign);
    }

    @Test
    public void should_return_true_when_deleting_existing_campaign() throws Exception {
        execute(delete(urlTemplate + existingCampaign.getId()))
            .andExpect(MockMvcResultMatchers.status().isOk());
        assertThat(resultExtractor.content()).isEqualTo("true");
    }

    @Test
    public void should_return_false_when_trying_to_delete_non_existing_campaign() throws Exception {
        execute(delete(urlTemplate + MAX_VALUE))
            .andExpect(MockMvcResultMatchers.status().isOk());
        assertThat(resultExtractor.content()).isEqualTo("false");
    }


    @Test
    public void should_not_found_deleted_campaign() throws Exception {
        // Given
        execute(delete(urlTemplate + existingCampaign.getId()));

        // When
        execute(MockMvcRequestBuilders.get(urlTemplate + existingCampaign.getId()))
            /*Then*/.andExpect(MockMvcResultMatchers.status().isNotFound());
    }

    @Test
    public void should_find_all_existing_campaign() throws Exception {
        // Given
        CampaignDto anotherExistingCampaign = insertCampaign(new CampaignDto(42L, "title", "description", emptyList(), emptyMap(), emptyList(), "env", false, false, null, null));

        // When
        execute(MockMvcRequestBuilders.get(urlTemplate))
            .andExpect(MockMvcResultMatchers.status().isOk());
        CampaignDto[] campaigns = resultExtractor.campaigns();

        // Then
        assertThat(campaigns)
            .usingFieldByFieldElementComparator()
            .containsExactlyInAnyOrder(anotherExistingCampaign, existingCampaign);
    }

    @Test
    public void should_find_campaigns_related_to_certain_scenario() throws Exception {
        // Given
        removeCampaign(existingCampaign.getId());
        CampaignDto campaignToCreate = new CampaignDto(42L, "CAMPAIGN_LINKED_TO_SCENARIO", "description", Lists.newArrayList("4", "5", "6"),
            emptyMap(), emptyList(), "env", false, false, null, null);
        insertCampaign(campaignToCreate);

        // When
        execute(MockMvcRequestBuilders.get(urlTemplate + "/scenario/4"))
            .andExpect(MockMvcResultMatchers.status().isOk());
        CampaignDto[] campaigns = resultExtractor.campaigns();

        List<String> campaignNames = Arrays.stream(campaigns).map(CampaignDto::getTitle).collect(Collectors.toList());

        // Then
        Assertions.assertThat(campaignNames).containsExactly(
            "CAMPAIGN_LINKED_TO_SCENARIO"
        );

    }

    @Test
    public void should_find_no_campaign_related_to_an_orphan_scenario() throws Exception {
        // Given
        removeCampaign(existingCampaign.getId());
        CampaignDto campaignToCreate = new CampaignDto(42L, "CAMPAIGN_WITHOUT_SCENARIO", "description", emptyList(),
            emptyMap(), emptyList(), "env", false, false, null, null);
        insertCampaign(campaignToCreate);

        // When
        execute(MockMvcRequestBuilders.get(urlTemplate + "/scenario/1"))
            .andExpect(MockMvcResultMatchers.status().isOk());
        CampaignDto[] campaigns = resultExtractor.campaigns();

        List<String> campaignNames = Arrays.stream(campaigns).map(CampaignDto::getTitle).collect(Collectors.toList());

        // Then
        Assertions.assertThat(campaignNames).isEmpty();

    }

    @Test
    public void should_retrieve_executions_and_current_execution_when_found_campaign() throws Exception {
        // Given
        CampaignExecutionReport currentExecution = new CampaignExecutionReport(42L, existingCampaign.getTitle(), false, "", null, null, "");
        when(campaignExecutionEngine.currentExecution(existingCampaign.getId()))
            .thenReturn(Optional.of(currentExecution));

        CampaignExecutionReport report1 = new CampaignExecutionReport(1L, existingCampaign.getId(), emptyList(), "...", false, "", null, null, "");
        CampaignExecutionReport report2 = new CampaignExecutionReport(2L, existingCampaign.getId(), emptyList(), "...", false, "", null, null, "");
        CampaignExecutionReport report3 = new CampaignExecutionReport(3L, existingCampaign.getId(), emptyList(), "...", false, "", null, null, "");
        CampaignExecutionReport report4 = new CampaignExecutionReport(4L, existingCampaign.getId(), emptyList(), "...", false, "", null, null, "");

        repository.saveReport(existingCampaign.getId(), report1);
        repository.saveReport(existingCampaign.getId(), report2);
        repository.saveReport(existingCampaign.getId(), report3);
        repository.saveReport(existingCampaign.getId(), report4);

        // When
        execute(MockMvcRequestBuilders.get(urlTemplate + existingCampaign.getId()))
            .andExpect(MockMvcResultMatchers.status().isOk());
        CampaignDto receivedCampaign = resultExtractor.campaign();

        // Then
        // get campaign with all executions
        verify(campaignExecutionEngine).currentExecution(existingCampaign.getId());
        assertThat(receivedCampaign.getCampaignExecutionReports()).hasSize(5);
        assertThat(receivedCampaign.getCampaignExecutionReports().get(0).getExecutionId()).isEqualTo(42L);
    }

    @Test
    public void should_retrieve_user_execution_when_found_campaign() throws Exception {
        // Given
        CampaignExecutionReport currentExecution = new CampaignExecutionReport(42L, existingCampaign.getTitle(), false, "", null, null, "user_1");
        when(campaignExecutionEngine.currentExecution(existingCampaign.getId()))
            .thenReturn(Optional.of(currentExecution));

        CampaignExecutionReport report1 = new CampaignExecutionReport(1L, existingCampaign.getId(), emptyList(), existingCampaign.getTitle(), false, "", null, null, "user_2");

        repository.saveReport(existingCampaign.getId(), report1);

        // When
        execute(MockMvcRequestBuilders.get(urlTemplate + existingCampaign.getId()))
            .andExpect(MockMvcResultMatchers.status().isOk());
        CampaignDto receivedCampaign = resultExtractor.campaign();

        // Then
        // get campaign with all executions
        verify(campaignExecutionEngine).currentExecution(existingCampaign.getId());
        assertThat(receivedCampaign.getCampaignExecutionReports()).hasSize(2);
        assertThat(receivedCampaign.getCampaignExecutionReports().get(0).getExecutionId()).isEqualTo(42L);

        assertThat(receivedCampaign.getCampaignExecutionReports().get(0).getUserId()).isEqualTo("user_1");
        assertThat(receivedCampaign.getCampaignExecutionReports().get(1).getUserId()).isEqualTo("user_2");

    }

    @Test
    public void should_add_current_executions_when_last_executions_asked_for() throws Exception {
        // Given
        // one persisted execution and two current campaigns executions
        ExecutionHistory.ExecutionSummary execution0 = mock(ExecutionHistory.ExecutionSummary.class);
        when(execution0.time()).thenReturn(LocalDateTime.now().minusDays(1));
        CampaignExecutionReport campaignExecutionReport0 = new CampaignExecutionReport(1L, 1L, singletonList(new ScenarioExecutionReportCampaign("20", "...", execution0)), "title", false, "", null, null, "");
        CampaignDto anotherExistingCampaign = new CampaignDto(null, "title", "description", emptyList(),
            emptyMap(), emptyList(), "env", false, false, null, null);
        anotherExistingCampaign = insertCampaign(anotherExistingCampaign);
        repository.saveReport(anotherExistingCampaign.getId(), campaignExecutionReport0);

        CampaignExecutionReport campaignExecutionReport1 = new CampaignExecutionReport(10L, 1L, emptyList(), existingCampaign.getTitle(), false, "", null, null, "");
        awaitDuring(100, MILLISECONDS); // Avoid reports with same startDate...
        CampaignExecutionReport campaignExecutionReport2 = new CampaignExecutionReport(5L, 2L, emptyList(), anotherExistingCampaign.getTitle(), false, "", null, null, "");

        when(campaignExecutionEngine.currentExecutions())
            .thenReturn(Lists.list(campaignExecutionReport1, campaignExecutionReport2));

        // When
        // last executions asked for
        execute(MockMvcRequestBuilders.get(urlTemplate + "/lastexecutions/5"))
            .andExpect(MockMvcResultMatchers.status().isOk());
        CampaignExecutionReportDto[] lastExecutions = resultExtractor.reports();

        // Then
        assertThat(lastExecutions).hasSize(3);
        assertThat(lastExecutions[0].getExecutionId()).isEqualTo(campaignExecutionReport0.executionId);
        assertThat(lastExecutions[1].getExecutionId()).isEqualTo(campaignExecutionReport1.executionId);
        assertThat(lastExecutions[2].getExecutionId()).isEqualTo(campaignExecutionReport2.executionId);
    }

    @Test
    public void should_retrieve_scenarios_of_a_campaign() throws Exception {
        // Given
        removeCampaign(existingCampaign.getId());
        CampaignDto campaignToCreate = new CampaignDto(42L, "CAMPAIGN_LINKED_TO_SCENARIO", "description", Lists.newArrayList("55", "44-44"),
            emptyMap(), emptyList(), "env", false, false, null, null);
        insertCampaign(campaignToCreate);

        when(repositoryAggregator.findMetadataById("44-44"))
            .thenReturn(Optional.of(
                TestCaseMetadataImpl.builder().withId("44-44").build()
            ));
        when(repositoryAggregator.findMetadataById("55")).thenReturn(Optional.of(TestCaseMetadataImpl.builder().withId("55").build()));

        // When
        execute(MockMvcRequestBuilders.get(urlTemplate + "2" + "/scenarios"))
            .andExpect(MockMvcResultMatchers.status().isOk());
        TestCaseIndexDto[] scenarios = resultExtractor.scenarios();

        List<String> ids = Arrays.stream(scenarios).map(c -> c.metadata().id().get()).collect(Collectors.toList());

        // Then
        Assertions.assertThat(ids).containsExactly(
            "55", "44-44"
        );

    }

    private void createExistingCampaign() throws Exception {
        existingCampaign = insertCampaign(SAMPLE_CAMPAIGN);
    }

    /**
     * Insert a campaign into the data base and returns insertion result.
     *
     * @param campaign The campaign to insert.
     * @return The database inserted campaign result.
     */
    private CampaignDto insertCampaign(CampaignDto campaign) throws Exception {
        execute(post(urlTemplate)
            .content(om.writeValueAsString(campaign))
            .contentType(APPLICATION_JSON_VALUE))
            .andExpect(MockMvcResultMatchers.status().isOk());
        return resultExtractor.campaign();
    }

    private void removeCampaign(Long idCampaign) throws Exception {
        execute(delete(urlTemplate + idCampaign));
    }


    /**
     * Execute the request and fill {@link #resultExtractor} with the result.
     */
    private ResultActions execute(MockHttpServletRequestBuilder builder) throws Exception {
        return mockMvc.perform(builder).andDo(resultExtractor::fill);
    }

    // ----------------------------------------------------------------------
    // ----------------------------------------------------------------------

    /**
     * Wrapper used to extract HTTP request response.
     */
    private class ResultExtractor {
        private MvcResult result;

        private void fill(MvcResult result) {
            this.result = result;
        }

        private String content() throws UnsupportedEncodingException {
            return result.getResponse().getContentAsString();
        }

        private CampaignDto campaign() throws IOException {
            return om.readValue(content(), CampaignDto.class);
        }

        private CampaignDto[] campaigns() throws IOException {
            return om.readValue(content(), CampaignDto[].class);
        }

        private TestCaseIndexDto[] scenarios() throws IOException {
            return om.readValue(content(), ImmutableTestCaseIndexDto[].class);
        }

        private CampaignExecutionReportDto[] reports() throws IOException {
            return om.readValue(content(), CampaignExecutionReportDto[].class);
        }
    }
}
