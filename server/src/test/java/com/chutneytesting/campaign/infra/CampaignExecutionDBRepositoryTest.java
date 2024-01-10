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

import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.params.provider.Arguments.of;

import com.chutneytesting.campaign.domain.CampaignExecutionRepository;
import com.chutneytesting.campaign.infra.jpa.CampaignEntity;
import com.chutneytesting.execution.infra.storage.jpa.ScenarioExecutionEntity;
import com.chutneytesting.scenario.infra.jpa.ScenarioEntity;
import com.chutneytesting.server.core.domain.execution.report.ServerReportStatus;
import com.chutneytesting.server.core.domain.scenario.campaign.CampaignExecution;
import com.chutneytesting.server.core.domain.scenario.campaign.ScenarioExecutionCampaign;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import util.infra.AbstractLocalDatabaseTest;
import util.infra.EnableH2MemTestInfra;
import util.infra.EnablePostgreSQLTestInfra;
import util.infra.EnableSQLiteTestInfra;

public class CampaignExecutionDBRepositoryTest {

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
    class PostgreSQL extends AllTests {
    }

    abstract class AllTests extends AbstractLocalDatabaseTest {

        @Autowired
        private CampaignExecutionRepository sut;


        @Test
        public void should_return_the_last_campaign_execution() {
            ScenarioEntity scenarioEntity = givenScenario();
            CampaignEntity campaign = givenCampaign(scenarioEntity);

            ScenarioExecutionEntity scenarioExecution = givenScenarioExecution(scenarioEntity.getId(), ServerReportStatus.NOT_EXECUTED);
            ScenarioExecutionCampaign scenarioExecutionReport = new ScenarioExecutionCampaign(scenarioEntity.getId().toString(), scenarioEntity.getTitle(), scenarioExecution.toDomain());

            Long campaignExecutionId1 = sut.generateCampaignExecutionId(campaign.id(), "executionEnv");
            Long campaignExecutionId2 = sut.generateCampaignExecutionId(campaign.id(), "executionEnv");
            Long campaignExecutionId3 = sut.generateCampaignExecutionId(campaign.id(), "executionEnv");

            CampaignExecution campaignExecution1 = new CampaignExecution(campaignExecutionId1, campaign.id(), singletonList(scenarioExecutionReport), campaign.title(), true, "env", "", 5, "user");
            CampaignExecution campaignExecution2 = new CampaignExecution(campaignExecutionId2, campaign.id(), singletonList(scenarioExecutionReport), campaign.title(), true, "env", "", 5, "user");
            CampaignExecution campaignExecution3 = new CampaignExecution(campaignExecutionId3, campaign.id(), singletonList(scenarioExecutionReport), campaign.title(), true, "env", "", 5, "user");

            sut.saveCampaignExecution(campaign.id(), campaignExecution1);
            sut.saveCampaignExecution(campaign.id(), campaignExecution2);
            sut.saveCampaignExecution(campaign.id(), campaignExecution3);

            CampaignExecution report = sut.getLastExecution(campaign.id());

            assertThat(report).isEqualTo(campaignExecution3);
        }

        @Test
        public void should_throw_exception_when_no_campaign_execution() {
            ScenarioEntity scenarioEntity = givenScenario();
            CampaignEntity campaign = givenCampaign(scenarioEntity);

            assertThatThrownBy(() -> sut.getLastExecution(campaign.id()));
        }

        @Test
        public void should_persist_1_execution_when_saving_1_campaign_execution_report() {
            ScenarioEntity scenarioEntity = givenScenario();
            CampaignEntity campaign = givenCampaign(scenarioEntity);

            ScenarioExecutionEntity scenarioExecution = givenScenarioExecution(scenarioEntity.getId(), ServerReportStatus.NOT_EXECUTED);
            ScenarioExecutionCampaign scenarioExecutionReport = new ScenarioExecutionCampaign(scenarioEntity.getId().toString(), scenarioEntity.getTitle(), scenarioExecution.toDomain());
            Long campaignExecutionId = sut.generateCampaignExecutionId(campaign.id(), "executionEnv");
            CampaignExecution campaignExecution = new CampaignExecution(campaignExecutionId, campaign.id(), singletonList(scenarioExecutionReport), campaign.title(), true, "env", "", 5, "user");
            sut.saveCampaignExecution(campaign.id(), campaignExecution);

            List<CampaignExecution> reports = sut.getExecutionHistory(campaign.id());

            assertThat(reports).hasSize(1)
                .first()
                .hasFieldOrPropertyWithValue("executionId", campaignExecution.executionId)
                .hasFieldOrPropertyWithValue("campaignName", campaignExecution.campaignName)
                .hasFieldOrPropertyWithValue("partialExecution", campaignExecution.partialExecution)
                .hasFieldOrPropertyWithValue("dataSetId", campaignExecution.dataSetId)
                .hasFieldOrPropertyWithValue("dataSetVersion", campaignExecution.dataSetVersion)
                .hasFieldOrPropertyWithValue("executionEnvironment", campaignExecution.executionEnvironment)
                .hasFieldOrPropertyWithValue("userId", campaignExecution.userId)
            ;

            assertThat(reports.get(0).scenarioExecutionReports()).hasSize(1)
                .first()
                .hasFieldOrPropertyWithValue("scenarioId", scenarioEntity.getId().toString())
                .hasFieldOrPropertyWithValue("scenarioName", scenarioEntity.getTitle())
                .extracting("execution")
                .hasFieldOrPropertyWithValue("executionId", scenarioExecution.id())
                .hasFieldOrPropertyWithValue("status", scenarioExecution.status())
                .hasFieldOrPropertyWithValue("environment", scenarioExecution.environment())
                .hasFieldOrPropertyWithValue("datasetId", scenarioExecution.datasetId())
                .hasFieldOrPropertyWithValue("datasetVersion", scenarioExecution.datasetVersion())
            ;
        }

        @Test
        public void should_persist_2_executions_when_saving_2_campaign_execution_report() {
            ScenarioEntity scenarioEntityOne = givenScenario();
            ScenarioEntity scenarioEntityTwo = givenScenario();
            CampaignEntity campaign = givenCampaign(scenarioEntityOne, scenarioEntityTwo);

            ScenarioExecutionEntity scenarioOneExecution = givenScenarioExecution(scenarioEntityOne.getId(), ServerReportStatus.SUCCESS);
            ScenarioExecutionCampaign scenarioOneExecutionReport = new ScenarioExecutionCampaign(scenarioEntityOne.getId().toString(), scenarioEntityOne.getTitle(), scenarioOneExecution.toDomain());
            ScenarioExecutionEntity scenarioTwoExecution = givenScenarioExecution(scenarioEntityTwo.getId(), ServerReportStatus.FAILURE);
            ScenarioExecutionCampaign scenarioTwoExecutionReport = new ScenarioExecutionCampaign(scenarioEntityTwo.getId().toString(), scenarioEntityTwo.getTitle(), scenarioTwoExecution.toDomain());
            Long campaignExecutionId = sut.generateCampaignExecutionId(campaign.id(), "executionEnv");
            CampaignExecution campaignExecution = new CampaignExecution(campaignExecutionId, campaign.id(), new ArrayList<>(List.of(scenarioOneExecutionReport, scenarioTwoExecutionReport)), campaign.title(), true, "env", "", 5, "user");
            sut.saveCampaignExecution(campaign.id(), campaignExecution);

            List<CampaignExecution> reports = sut.getExecutionHistory(campaign.id());

            assertThat(reports).hasSize(1)
                .first()
                .hasFieldOrPropertyWithValue("executionId", campaignExecution.executionId)
                .hasFieldOrPropertyWithValue("campaignName", campaignExecution.campaignName)
                .hasFieldOrPropertyWithValue("partialExecution", campaignExecution.partialExecution)
                .hasFieldOrPropertyWithValue("dataSetId", campaignExecution.dataSetId)
                .hasFieldOrPropertyWithValue("dataSetVersion", campaignExecution.dataSetVersion)
                .hasFieldOrPropertyWithValue("executionEnvironment", campaignExecution.executionEnvironment)
                .hasFieldOrPropertyWithValue("userId", campaignExecution.userId)
            ;

            assertThat(reports.get(0).scenarioExecutionReports()).hasSize(2);

            assertThat(reports.get(0).scenarioExecutionReports()).element(0)
                .hasFieldOrPropertyWithValue("scenarioId", scenarioEntityOne.getId().toString())
                .hasFieldOrPropertyWithValue("scenarioName", scenarioEntityOne.getTitle())
                .extracting("execution")
                .hasFieldOrPropertyWithValue("executionId", scenarioOneExecution.id())
                .hasFieldOrPropertyWithValue("status", scenarioOneExecution.status())
                .hasFieldOrPropertyWithValue("environment", scenarioOneExecution.environment())
                .hasFieldOrPropertyWithValue("datasetId", scenarioOneExecution.datasetId())
                .hasFieldOrPropertyWithValue("datasetVersion", scenarioOneExecution.datasetVersion())
            ;
            assertThat(reports.get(0).scenarioExecutionReports()).element(1)
                .hasFieldOrPropertyWithValue("scenarioId", scenarioEntityTwo.getId().toString())
                .hasFieldOrPropertyWithValue("scenarioName", scenarioEntityTwo.getTitle())
                .extracting("execution")
                .hasFieldOrPropertyWithValue("executionId", scenarioTwoExecution.id())
                .hasFieldOrPropertyWithValue("status", scenarioTwoExecution.status())
                .hasFieldOrPropertyWithValue("environment", scenarioTwoExecution.environment())
                .hasFieldOrPropertyWithValue("datasetId", scenarioTwoExecution.datasetId())
                .hasFieldOrPropertyWithValue("datasetVersion", scenarioTwoExecution.datasetVersion())
            ;
        }

        @Test
        @Disabled
        // I don't know why is this here and how it was success before ?
        // When a campaign is running, as in this test, user interface add current executions by separate call (cf. campaignController)
        // Therefore the implementation i made by not included the current execution if there is in getExecutionHistory method
        // So this test does not make sense for me now.
        // TODO - To move elsewhere ?
        public void campaign_execution_history_should_list_not_executed_scenarios() {
            ScenarioEntity scenarioEntityOne = givenScenario();
            ScenarioEntity scenarioEntityTwo = givenScenario();
            CampaignEntity campaign = givenCampaign(scenarioEntityOne, scenarioEntityTwo);

            ScenarioExecutionEntity scenarioOneExecution = givenScenarioExecution(scenarioEntityOne.getId(), ServerReportStatus.SUCCESS);
            ScenarioExecutionCampaign scenarioOneExecutionReport = new ScenarioExecutionCampaign(scenarioEntityOne.getId().toString(), scenarioEntityOne.getTitle(), scenarioOneExecution.toDomain());
            Long campaignExecutionId = sut.generateCampaignExecutionId(campaign.id(), "executionEnv");
            CampaignExecution campaignExecution = new CampaignExecution(campaignExecutionId, campaign.id(), singletonList(scenarioOneExecutionReport), campaign.title(), true, "env", "", 5, "user");
            sut.startExecution(campaign.id(), campaignExecution);
            sut.saveCampaignExecution(campaign.id(), campaignExecution);

            List<CampaignExecution> reports = sut.getExecutionHistory(campaign.id());

            assertThat(reports).hasSize(1)
                .first()
                .hasFieldOrPropertyWithValue("executionId", campaignExecution.executionId)
                .hasFieldOrPropertyWithValue("campaignName", campaignExecution.campaignName)
                .hasFieldOrPropertyWithValue("partialExecution", campaignExecution.partialExecution)
                .hasFieldOrPropertyWithValue("dataSetId", campaignExecution.dataSetId)
                .hasFieldOrPropertyWithValue("dataSetVersion", campaignExecution.dataSetVersion)
                .hasFieldOrPropertyWithValue("executionEnvironment", campaignExecution.executionEnvironment)
                .hasFieldOrPropertyWithValue("userId", campaignExecution.userId)
            ;

            assertThat(reports.get(0).scenarioExecutionReports()).hasSize(2);

            assertThat(reports.get(0).scenarioExecutionReports()).element(0)
                .hasFieldOrPropertyWithValue("scenarioId", scenarioEntityOne.getId().toString())
                .hasFieldOrPropertyWithValue("scenarioName", scenarioEntityOne.getTitle())
                .extracting("execution")
                .hasFieldOrPropertyWithValue("executionId", scenarioOneExecution.id())
                .hasFieldOrPropertyWithValue("status", scenarioOneExecution.status())
            ;
            assertThat(reports.get(0).scenarioExecutionReports()).element(1)
                .hasFieldOrPropertyWithValue("scenarioId", scenarioEntityTwo.getId().toString())
                .hasFieldOrPropertyWithValue("scenarioName", scenarioEntityTwo.getTitle())
                .extracting("execution")
                .hasFieldOrPropertyWithValue("executionId", -1L)
                .hasFieldOrPropertyWithValue("status", ServerReportStatus.NOT_EXECUTED)
            ;
        }

        @Test
        public void should_remove_all_campaign_executions_when_removing_campaign_execution_report() {
            ScenarioEntity scenarioEntity = givenScenario();
            CampaignEntity campaign = givenCampaign(scenarioEntity);

            ScenarioExecutionEntity scenarioExecution = givenScenarioExecution(scenarioEntity.getId(), ServerReportStatus.NOT_EXECUTED);
            ScenarioExecutionCampaign scenarioExecutionReport = new ScenarioExecutionCampaign(scenarioEntity.getId().toString(), scenarioEntity.getTitle(), scenarioExecution.toDomain());
            Long campaignExecutionId = sut.generateCampaignExecutionId(campaign.id(), "executionEnv");
            CampaignExecution campaignExecution = new CampaignExecution(campaignExecutionId, campaign.id(), singletonList(scenarioExecutionReport), campaign.title(), true, "env", "", 5, "user");
            sut.saveCampaignExecution(campaign.id(), campaignExecution);

            sut.clearAllExecutionHistory(campaign.id());

            List<CampaignExecution> executionHistory = sut.getExecutionHistory(campaign.id());
            assertThat(executionHistory).isEmpty();

            List<?> scenarioExecutions =
                entityManager.createNativeQuery("select * from scenario_executions where id = :id", ScenarioExecutionEntity.class)
                    .setParameter("id", scenarioExecution.id())
                    .getResultList();
            assertThat(scenarioExecutions).hasSize(1);
        }

        @Test
        public void should_get_2_last_campaign_report_created() {
            clearTables();
            ScenarioEntity scenarioEntity = givenScenario();
            CampaignEntity campaign = givenCampaign(scenarioEntity);

            ScenarioExecutionEntity scenarioExecutionOne = givenScenarioExecution(scenarioEntity.getId(), ServerReportStatus.NOT_EXECUTED);
            ScenarioExecutionCampaign scenarioExecutionOneReport = new ScenarioExecutionCampaign(scenarioEntity.getId().toString(), scenarioEntity.getTitle(), scenarioExecutionOne.toDomain());
            Long campaignExecutionOneId = sut.generateCampaignExecutionId(campaign.id(), "executionEnv");
            CampaignExecution campaignExecutionOneReport = new CampaignExecution(campaignExecutionOneId, campaign.id(), singletonList(scenarioExecutionOneReport), campaign.title(), true, "env", "", 5, "user");
            sut.saveCampaignExecution(campaign.id(), campaignExecutionOneReport);

            ScenarioExecutionEntity scenarioExecutionTwo = givenScenarioExecution(scenarioEntity.getId(), ServerReportStatus.SUCCESS);
            ScenarioExecutionCampaign scenarioExecutionTwoReport = new ScenarioExecutionCampaign(scenarioEntity.getId().toString(), scenarioEntity.getTitle(), scenarioExecutionTwo.toDomain());
            Long campaignExecutionTwoId = sut.generateCampaignExecutionId(campaign.id(), "executionEnv");
            CampaignExecution campaignExecutionTwoReport = new CampaignExecution(campaignExecutionTwoId, campaign.id(), singletonList(scenarioExecutionTwoReport), campaign.title(), true, "env", "", 5, "user");
            sut.saveCampaignExecution(campaign.id(), campaignExecutionTwoReport);

            ScenarioExecutionEntity scenarioExecutionThree = givenScenarioExecution(scenarioEntity.getId(), ServerReportStatus.FAILURE);
            ScenarioExecutionCampaign scenarioExecutionThreeReport = new ScenarioExecutionCampaign(scenarioEntity.getId().toString(), scenarioEntity.getTitle(), scenarioExecutionThree.toDomain());
            Long campaignExecutionThreeId = sut.generateCampaignExecutionId(campaign.id(), "executionEnv");
            CampaignExecution campaignExecutionThreeReport = new CampaignExecution(campaignExecutionThreeId, campaign.id(), singletonList(scenarioExecutionThreeReport), campaign.title(), true, "env", "", 5, "user");
            sut.saveCampaignExecution(campaign.id(), campaignExecutionThreeReport);

            ScenarioExecutionEntity scenarioExecutionFour = givenScenarioExecution(scenarioEntity.getId(), ServerReportStatus.RUNNING);
            ScenarioExecutionCampaign scenarioExecutionFourReport = new ScenarioExecutionCampaign(scenarioEntity.getId().toString(), scenarioEntity.getTitle(), scenarioExecutionFour.toDomain());
            Long campaignExecutionFourId = sut.generateCampaignExecutionId(campaign.id(), "executionEnv");
            CampaignExecution campaignExecutionFourReport = new CampaignExecution(campaignExecutionFourId, campaign.id(), singletonList(scenarioExecutionFourReport), campaign.title(), true, "env", "", 5, "user");
            sut.saveCampaignExecution(campaign.id(), campaignExecutionFourReport);


            List<CampaignExecution> lastExecutions = sut.getLastExecutions(2L);

            assertThat(lastExecutions).hasSize(2);

            assertThat(lastExecutions.get(0).scenarioExecutionReports()).hasSize(1)
                .element(0)
                .hasFieldOrPropertyWithValue("scenarioId", scenarioEntity.getId().toString())
                .hasFieldOrPropertyWithValue("scenarioName", scenarioEntity.getTitle())
                .extracting("execution")
                .hasFieldOrPropertyWithValue("executionId", scenarioExecutionFour.id())
                .hasFieldOrPropertyWithValue("status", scenarioExecutionFour.status())
            ;
            assertThat(lastExecutions.get(1).scenarioExecutionReports()).hasSize(1)
                .element(0)
                .hasFieldOrPropertyWithValue("scenarioId", scenarioEntity.getId().toString())
                .hasFieldOrPropertyWithValue("scenarioName", scenarioEntity.getTitle())
                .extracting("execution")
                .hasFieldOrPropertyWithValue("executionId", scenarioExecutionThree.id())
                .hasFieldOrPropertyWithValue("status", scenarioExecutionThree.status())
            ;
        }
    }

    @ParameterizedTest
    @MethodSource("invalidInputProvider")
    void shouldThrowExceptionForInvalidInputs(Long campaignId, String environment, Class<?> exceptionExpected) {
        CampaignExecutionRepository sut = new CampaignExecutionDBRepository(null,null,null,null);
        assertThatThrownBy(() -> sut.generateCampaignExecutionId(campaignId, environment))
            .isInstanceOf(exceptionExpected);
    }

    private static Stream<Arguments> invalidInputProvider() {
        return Stream.of(
            of(null, "testEnvironment", NullPointerException.class),
            of(123L, null, NullPointerException.class),
            of(123L, "", IllegalArgumentException.class)
        );
    }
}
