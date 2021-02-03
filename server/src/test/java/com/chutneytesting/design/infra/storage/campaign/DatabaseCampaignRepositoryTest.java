package com.chutneytesting.design.infra.storage.campaign;

import static com.google.common.collect.Lists.newArrayList;
import static java.util.Collections.emptyMap;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import com.chutneytesting.design.domain.campaign.Campaign;
import com.chutneytesting.design.domain.campaign.CampaignExecutionReport;
import com.chutneytesting.design.domain.campaign.CampaignRepository;
import com.chutneytesting.tests.AbstractLocalDatabaseTest;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class DatabaseCampaignRepositoryTest extends AbstractLocalDatabaseTest {

    private CampaignRepository sut;
    private CampaignExecutionRepository mockCampaignExecutionRepository;
    private CampaignParameterRepository campaignParameterRepository;

    @BeforeEach
    public void setUp() {
        mockCampaignExecutionRepository = mock(CampaignExecutionRepository.class);
        campaignParameterRepository = new CampaignParameterRepository(namedParameterJdbcTemplate);
        sut = new DatabaseCampaignRepository(namedParameterJdbcTemplate, mockCampaignExecutionRepository, campaignParameterRepository);
    }

    @Test
    public void should_find_a_campaign_by_id() {
        HashMap<String, String> dataSet = new HashMap<>();
        dataSet.put("param1", "val1");
        dataSet.put("param2", "");
        Campaign campaign = new Campaign(1L, "test", "lol", newArrayList("1", "2"), dataSet, null, "env", false, false, null);
        campaign = sut.createOrUpdate(campaign);

        Campaign selected = sut.findById(campaign.id);
        assertThat(selected.scenarioIds).containsExactly("1", "2");
        assertThat(selected.executionParameters).containsAllEntriesOf(dataSet);
    }

    @Test
    public void should_remove_a_campaign_by_id_and_all_its_parameters() {
        HashMap<String, String> dataSet = new HashMap<>();
        dataSet.put("param1", "val1");
        dataSet.put("param2", "");
        Campaign campaign = new Campaign(1L, "test", "lol", newArrayList("1", "2"), dataSet, null, "env", false, false, null);
        campaign = sut.createOrUpdate(campaign);

        boolean result = sut.removeById(campaign.id);
        List<CampaignParameter> actualParameters = campaignParameterRepository.findCampaignParameters(campaign.id);

        assertThat(result).isTrue();
        assertThat(actualParameters).isEmpty();
    }

    @Test
    public void should_find_scenario_order_by_index() {
        Campaign campaign = new Campaign(1L, "test", "lol", newArrayList("4", "2", "3", "1"), emptyMap(), null, "env", false, false, null);
        campaign = sut.createOrUpdate(campaign);

        Campaign selected = sut.findById(campaign.id);
        assertThat(selected.scenarioIds).containsExactly("4", "2", "3", "1");

        List<Campaign> campaigns = sut.findByName("test");
        assertThat(campaigns).hasSize(1);
        assertThat(campaigns.get(0).scenarioIds).containsExactly("4", "2", "3", "1");
    }

    @Test
    public void should_find_a_campaign_by_name() {
        Campaign campaign = new Campaign(1L, "campaignName", "lol", newArrayList("3", "4"), emptyMap(), null, "env", false, false, null);
        campaign = sut.createOrUpdate(campaign);

        List<Campaign> selected = sut.findByName(campaign.title);
        assertThat(selected.get(0).scenarioIds).containsExactly("3", "4");
    }

    @Test
    public void should_create_a_campaign_without_executions() {
        // Given
        Campaign campaign = new Campaign(null, "campaignName", "lol", newArrayList("3", "4"), emptyMap(), null, "env", false, false, null);

        // When
        Campaign savedCampaign = sut.createOrUpdate(campaign);

        // Then
        assertThat(savedCampaign.id).isNotNull();

        // , And when
        Campaign selected = sut.findById(savedCampaign.id);

        // Then
        assertThat(selected.id).isEqualTo(savedCampaign.id);
    }

    @Test
    public void should_update_a_campaign() {

        // Given
        Campaign unsavedCampaign = new Campaign(null, "campaignName", "lol", newArrayList("3", "4"), emptyMap(), null, "env", false, false, null);
        Campaign savedCampaign = sut.createOrUpdate(unsavedCampaign);

        String new_title = "new title";
        String new_description = "new description";
        List<String> new_scenarios = newArrayList("42");
        Campaign updatedCampaign = new Campaign(savedCampaign.id, new_title, new_description, new_scenarios, emptyMap(), LocalTime.now(), "env", false, false, null);

        // When
        Campaign selected = sut.createOrUpdate(updatedCampaign);

        // Then
        assertThat(selected.id).isEqualTo(savedCampaign.id);
        assertThat(selected.title).isEqualTo(new_title);
        assertThat(selected.description).isEqualTo(new_description);
        assertThat(selected.scenarioIds).containsExactlyElementsOf(new_scenarios);
        assertThat(selected.getScheduleTime()).isAfter(LocalTime.now().minusMinutes(1));
    }

    @Test
    public void should_delegate_saving_of_a_campaign_execution_report() {
        // Given
        Campaign unsavedCampaign = new Campaign(null, "campaignName", "lol", newArrayList("3", "4"), emptyMap(), null, "env", false, false, null);
        Campaign savedCampaign = sut.createOrUpdate(unsavedCampaign);
        CampaignExecutionReport mockReport = mock(CampaignExecutionReport.class);

        // When
        sut.saveReport(savedCampaign.id, mockReport);

        // Then
        verify(mockCampaignExecutionRepository).saveCampaignReport(savedCampaign.id, mockReport);
    }

    @Test
    public void should_delegate_get_last_execution_report() {
        // When
        sut.findLastExecutions(10L);

        // Then
        verify(mockCampaignExecutionRepository).findLastExecutions(10L);
    }

    @Test
    public void should_find_campaigns_related_to_a_given_scenario() {
        // Given
        Campaign campaign1 = new Campaign(null, "campaignTestName1", "campaignDesc1", newArrayList("1", "2"), emptyMap(), null, "env", false, false, null);
        Campaign campaign2 = new Campaign(null, "campaignTestName2", "campaignDesc2", newArrayList("1", "2"), emptyMap(), null, "env", false, false, null);
        Campaign campaign3 = new Campaign(null, "campaignTestName3", "campaignDesc3", newArrayList("1", "3"), emptyMap(), null, "env", false, false, null);
        Campaign campaign4 = new Campaign(null, "campaignTestName4", "campaignDesc4", newArrayList("3", "4"), emptyMap(), null, "env", false, false, null);
        sut.createOrUpdate(campaign1);
        sut.createOrUpdate(campaign2);
        sut.createOrUpdate(campaign3);
        sut.createOrUpdate(campaign4);

        // When
        List<String> scenarioCampaignNames = sut.findCampaignsByScenarioId("1").stream()
            .map(sc -> sc.title)
            .collect(Collectors.toList());

        // Then
        Assertions.assertThat(scenarioCampaignNames).containsExactly(
            "campaignTestName1",
            "campaignTestName2",
            "campaignTestName3"
        );
    }

    @Test
    public void should_find_no_campaign_related_to_an_orphan_scenario() {

        // Given
        Campaign campaign1 = new Campaign(null, "campaignTestName1", "campaignDesc1", newArrayList("3", "4"), emptyMap(), null, "env", false, false, null);
        sut.createOrUpdate(campaign1);

        // When
        List<Campaign> scenarioCampaigns = sut.findCampaignsByScenarioId("1");

        // Then
        Assertions.assertThat(scenarioCampaigns).isEmpty();
    }
}
