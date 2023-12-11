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

package com.chutneytesting.campaign.infra;

import static org.assertj.core.api.Assertions.assertThat;

import com.chutneytesting.campaign.domain.CampaignRepository;
import com.chutneytesting.scenario.infra.jpa.ScenarioEntity;
import com.chutneytesting.server.core.domain.scenario.campaign.Campaign;
import java.util.List;
import java.util.stream.Collectors;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.ResourceLock;
import org.springframework.beans.factory.annotation.Autowired;
import util.infra.AbstractLocalDatabaseTest;
import util.infra.EnableH2MemTestInfra;
import util.infra.EnablePostgreSQLTestInfra;
import util.infra.EnableSQLiteTestInfra;

public class DatabaseCampaignRepositoryTest {

    @Nested
    @EnableH2MemTestInfra
    class H2 extends AllTests {
    }

    @Nested
    @EnableSQLiteTestInfra
    class SQLite extends AllTests {
    }

    @Nested
    @EnablePostgreSQLTestInfra
    class PostreSQL extends AllTests {
    }

    @ResourceLock("changelog")
    abstract class AllTests extends AbstractLocalDatabaseTest {
        @Autowired
        private CampaignRepository sut;

        @AfterEach
        void afterEach() {
            clearTables();
        }

        @Test
        public void should_find_a_campaign_by_id() {
            ScenarioEntity s1 = givenScenario();
            ScenarioEntity s2 = givenScenario();

            List<String> scenarioIds = scenariosIds(s1, s2);
            Campaign campaign = new Campaign(null, "test", "lol", scenarioIds, "env", false, false, null, null);
            campaign = sut.createOrUpdate(campaign);

            Campaign selected = sut.findById(campaign.id);
            assertThat(selected.scenarioIds).containsExactlyElementsOf(scenarioIds);
        }

        @Test
        public void should_remove_a_campaign_by_id_and_all_its_parameters() {
            ScenarioEntity s1 = givenScenario();
            ScenarioEntity s2 = givenScenario();

            Campaign campaign = new Campaign(null, "test", "lol", scenariosIds(s1, s2), "env", false, false, null, null);
            campaign = sut.createOrUpdate(campaign);

            boolean result = sut.removeById(campaign.id);
            assertThat(result).isTrue();
        }

        @Test
        public void should_find_scenario_order_by_index() {
            ScenarioEntity s1 = givenScenario();
            ScenarioEntity s2 = givenScenario();
            ScenarioEntity s3 = givenScenario();
            ScenarioEntity s4 = givenScenario();

            List<String> scenarioIds = scenariosIds(s4, s2, s3, s1);
            Campaign campaign = new Campaign(null, "test", "lol", scenarioIds, "env", false, false, null, null);
            campaign = sut.createOrUpdate(campaign);

            Campaign selected = sut.findById(campaign.id);
            assertThat(selected.scenarioIds).containsExactlyElementsOf(scenarioIds);

            List<Campaign> campaigns = sut.findByName("test");
            assertThat(campaigns).hasSize(1);
            assertThat(campaigns.get(0).scenarioIds).containsExactlyElementsOf(scenarioIds);
        }

        @Test
        public void should_find_a_campaign_by_name() {
            ScenarioEntity s1 = givenScenario();
            ScenarioEntity s2 = givenScenario();

            List<String> scenarioIds = scenariosIds(s1, s2);
            Campaign campaign = new Campaign(null, "campaignName", "lol", scenarioIds, "env", false, false, null, null);
            campaign = sut.createOrUpdate(campaign);

            List<Campaign> selected = sut.findByName(campaign.title);
            assertThat(selected).hasSize(1);
            assertThat(selected.get(0).scenarioIds).containsExactlyElementsOf(scenarioIds);
        }

        @Test
        public void should_update_a_campaign() {
            // Given
            ScenarioEntity s1 = givenScenario();
            ScenarioEntity s2 = givenScenario();
            List<String> scenarioIds = scenariosIds(s1, s2);
            Campaign unsavedCampaign = new Campaign(null, "campaignName", "lol", scenarioIds, "env", false, false, null, null);
            Campaign savedCampaign = sut.createOrUpdate(unsavedCampaign);
            assertThat(savedCampaign.title).isEqualTo(unsavedCampaign.title);
            assertThat(savedCampaign.description).isEqualTo(unsavedCampaign.description);
            assertThat(savedCampaign.executionEnvironment()).isEqualTo(unsavedCampaign.executionEnvironment());
            assertThat(savedCampaign.parallelRun).isFalse();
            assertThat(savedCampaign.retryAuto).isFalse();
            assertThat(savedCampaign.scenarioIds).containsExactlyElementsOf(scenarioIds);

            String newTitle = "new title";
            String newDescription = "new description";
            ScenarioEntity s3 = givenScenario();
            List<String> newScenarios = scenariosIds(s3);
            String newEnvironment = "newEnv";
            Campaign updatedCampaign = new Campaign(savedCampaign.id, newTitle, newDescription, newScenarios, newEnvironment, true, true, null, null);

            // When
            Campaign selected = sut.createOrUpdate(updatedCampaign);

            // Then
            assertThat(selected.id).isEqualTo(savedCampaign.id);
            assertThat(selected.title).isEqualTo(newTitle);
            assertThat(selected.description).isEqualTo(newDescription);
            assertThat(selected.executionEnvironment()).isEqualTo(newEnvironment);
            assertThat(selected.parallelRun).isTrue();
            assertThat(selected.retryAuto).isTrue();
            assertThat(selected.scenarioIds).containsExactlyElementsOf(newScenarios);
        }

        @Test
        public void should_find_campaigns_related_to_a_given_scenario() {
            // Given
            ScenarioEntity s1 = givenScenario();
            ScenarioEntity s2 = givenScenario();
            ScenarioEntity s3 = givenScenario();
            ScenarioEntity s4 = givenScenario();
            Campaign campaign1 = new Campaign(null, "campaignTestName1", "campaignDesc1", scenariosIds(s1, s2), "env", false, false, null, null);
            Campaign campaign2 = new Campaign(null, "campaignTestName2", "campaignDesc2", scenariosIds(s2, s1), "env", false, false, null, null);
            Campaign campaign3 = new Campaign(null, "campaignTestName3", "campaignDesc3", scenariosIds(s1, s3), "env", false, false, null, null);
            Campaign campaign4 = new Campaign(null, "campaignTestName4", "campaignDesc4", scenariosIds(s3, s4), "env", false, false, null, null);
            sut.createOrUpdate(campaign1);
            sut.createOrUpdate(campaign2);
            sut.createOrUpdate(campaign3);
            sut.createOrUpdate(campaign4);

            // When
            List<String> scenarioCampaignNames = sut.findCampaignsByScenarioId(s1.getId().toString()).stream()
                .map(sc -> sc.title)
                .collect(Collectors.toList());

            // Then
            Assertions.assertThat(scenarioCampaignNames).containsExactlyInAnyOrder(
                campaign1.title,
                campaign2.title,
                campaign3.title
            );
        }

        @Test
        public void should_find_no_campaign_related_to_an_orphan_scenario() {
            // Given
            ScenarioEntity s1 = givenScenario();
            Campaign campaign1 = new Campaign(null, "campaignTestName1", "campaignDesc1", scenariosIds(s1), "env", false, false, null, null);
            sut.createOrUpdate(campaign1);

            // When
            List<Campaign> scenarioCampaigns = sut.findCampaignsByScenarioId(String.valueOf(s1.getId() + 666));

            // Then
            Assertions.assertThat(scenarioCampaigns).isEmpty();
        }
    }
}
