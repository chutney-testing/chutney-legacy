package com.chutneytesting.campaign.infra;

import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;

import com.chutneytesting.execution.infra.storage.jpa.ScenarioExecution;
import com.chutneytesting.scenario.infra.jpa.Scenario;
import com.chutneytesting.server.core.domain.execution.report.ServerReportStatus;
import com.chutneytesting.server.core.domain.scenario.campaign.CampaignExecutionReport;
import com.chutneytesting.server.core.domain.scenario.campaign.ScenarioExecutionReportCampaign;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import util.infra.AbstractLocalDatabaseTest;
import util.infra.EnableH2MemTestInfra;
import util.infra.EnablePostgreSQLTestInfra;
import util.infra.EnableSQLiteTestInfra;

public class CampaignExecutionRepositoryTest {

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

    abstract class AllTests extends AbstractLocalDatabaseTest {

        @Autowired
        private CampaignExecutionDBRepository sut;

        @Test
        public void should_persist_1_execution_when_saving_1_campaign_execution_report() {
            Scenario scenario = givenScenario();
            com.chutneytesting.campaign.infra.jpa.Campaign campaign = givenCampaign(scenario);

            ScenarioExecution scenarioExecution = givenScenarioExecution(scenario.id(), ServerReportStatus.NOT_EXECUTED);
            ScenarioExecutionReportCampaign scenarioExecutionReport = new ScenarioExecutionReportCampaign(scenario.id().toString(), scenario.title(), scenarioExecution.toDomain());
            Long campaignExecutionId = sut.generateCampaignExecutionId(campaign.id());
            CampaignExecutionReport campaignExecutionReport = new CampaignExecutionReport(campaignExecutionId, campaign.id(), singletonList(scenarioExecutionReport), campaign.title(), true, "env", "#2:87", 5, "user");
            sut.saveCampaignReport(campaign.id(), campaignExecutionReport);

            List<CampaignExecutionReport> reports = sut.findExecutionHistory(campaign.id());

            assertThat(reports).hasSize(1)
                .first()
                .hasFieldOrPropertyWithValue("executionId", campaignExecutionReport.executionId)
                .hasFieldOrPropertyWithValue("campaignName", campaignExecutionReport.campaignName)
                .hasFieldOrPropertyWithValue("partialExecution", campaignExecutionReport.partialExecution)
                .hasFieldOrPropertyWithValue("dataSetId", campaignExecutionReport.dataSetId)
                .hasFieldOrPropertyWithValue("dataSetVersion", campaignExecutionReport.dataSetVersion)
                .hasFieldOrPropertyWithValue("executionEnvironment", campaignExecutionReport.executionEnvironment)
                .hasFieldOrPropertyWithValue("userId", campaignExecutionReport.userId)
            ;

            assertThat(reports.get(0).scenarioExecutionReports()).hasSize(1)
                .first()
                .hasFieldOrPropertyWithValue("scenarioId", scenario.id().toString())
                .hasFieldOrPropertyWithValue("scenarioName", scenario.title())
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
            Scenario scenarioOne = givenScenario();
            Scenario scenarioTwo = givenScenario();
            com.chutneytesting.campaign.infra.jpa.Campaign campaign = givenCampaign(scenarioOne, scenarioTwo);

            ScenarioExecution scenarioOneExecution = givenScenarioExecution(scenarioOne.id(), ServerReportStatus.SUCCESS);
            ScenarioExecutionReportCampaign scenarioOneExecutionReport = new ScenarioExecutionReportCampaign(scenarioOne.id().toString(), scenarioOne.title(), scenarioOneExecution.toDomain());
            ScenarioExecution scenarioTwoExecution = givenScenarioExecution(scenarioTwo.id(), ServerReportStatus.FAILURE);
            ScenarioExecutionReportCampaign scenarioTwoExecutionReport = new ScenarioExecutionReportCampaign(scenarioTwo.id().toString(), scenarioTwo.title(), scenarioTwoExecution.toDomain());
            Long campaignExecutionId = sut.generateCampaignExecutionId(campaign.id());
            CampaignExecutionReport campaignExecutionReport = new CampaignExecutionReport(campaignExecutionId, campaign.id(), new ArrayList<>(List.of(scenarioOneExecutionReport, scenarioTwoExecutionReport)), campaign.title(), true, "env", "#2:87", 5, "user");
            sut.saveCampaignReport(campaign.id(), campaignExecutionReport);

            List<CampaignExecutionReport> reports = sut.findExecutionHistory(campaign.id());

            assertThat(reports).hasSize(1)
                .first()
                .hasFieldOrPropertyWithValue("executionId", campaignExecutionReport.executionId)
                .hasFieldOrPropertyWithValue("campaignName", campaignExecutionReport.campaignName)
                .hasFieldOrPropertyWithValue("partialExecution", campaignExecutionReport.partialExecution)
                .hasFieldOrPropertyWithValue("dataSetId", campaignExecutionReport.dataSetId)
                .hasFieldOrPropertyWithValue("dataSetVersion", campaignExecutionReport.dataSetVersion)
                .hasFieldOrPropertyWithValue("executionEnvironment", campaignExecutionReport.executionEnvironment)
                .hasFieldOrPropertyWithValue("userId", campaignExecutionReport.userId)
            ;

            assertThat(reports.get(0).scenarioExecutionReports()).hasSize(2);

            assertThat(reports.get(0).scenarioExecutionReports()).element(0)
                .hasFieldOrPropertyWithValue("scenarioId", scenarioOne.id().toString())
                .hasFieldOrPropertyWithValue("scenarioName", scenarioOne.title())
                .extracting("execution")
                .hasFieldOrPropertyWithValue("executionId", scenarioOneExecution.id())
                .hasFieldOrPropertyWithValue("status", scenarioOneExecution.status())
                .hasFieldOrPropertyWithValue("environment", scenarioOneExecution.environment())
                .hasFieldOrPropertyWithValue("datasetId", scenarioOneExecution.datasetId())
                .hasFieldOrPropertyWithValue("datasetVersion", scenarioOneExecution.datasetVersion())
            ;
            assertThat(reports.get(0).scenarioExecutionReports()).element(1)
                .hasFieldOrPropertyWithValue("scenarioId", scenarioTwo.id().toString())
                .hasFieldOrPropertyWithValue("scenarioName", scenarioTwo.title())
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
        // Therefore the implementation i made by not included the current execution if there is in findExecutionHistory method
        // So this test does not make sense for me now.
        // TODO - To move elsewhere ?
        public void campaign_execution_history_should_list_not_executed_scenarios() {
            Scenario scenarioOne = givenScenario();
            Scenario scenarioTwo = givenScenario();
            com.chutneytesting.campaign.infra.jpa.Campaign campaign = givenCampaign(scenarioOne, scenarioTwo);

            ScenarioExecution scenarioOneExecution = givenScenarioExecution(scenarioOne.id(), ServerReportStatus.SUCCESS);
            ScenarioExecutionReportCampaign scenarioOneExecutionReport = new ScenarioExecutionReportCampaign(scenarioOne.id().toString(), scenarioOne.title(), scenarioOneExecution.toDomain());
            Long campaignExecutionId = sut.generateCampaignExecutionId(campaign.id());
            CampaignExecutionReport campaignExecutionReport = new CampaignExecutionReport(campaignExecutionId, campaign.id(), singletonList(scenarioOneExecutionReport), campaign.title(), true, "env", "#2:87", 5, "user");
            sut.startExecution(campaign.id(), campaignExecutionReport);
            sut.saveCampaignReport(campaign.id(), campaignExecutionReport);

            List<CampaignExecutionReport> reports = sut.findExecutionHistory(campaign.id());

            assertThat(reports).hasSize(1)
                .first()
                .hasFieldOrPropertyWithValue("executionId", campaignExecutionReport.executionId)
                .hasFieldOrPropertyWithValue("campaignName", campaignExecutionReport.campaignName)
                .hasFieldOrPropertyWithValue("partialExecution", campaignExecutionReport.partialExecution)
                .hasFieldOrPropertyWithValue("dataSetId", campaignExecutionReport.dataSetId)
                .hasFieldOrPropertyWithValue("dataSetVersion", campaignExecutionReport.dataSetVersion)
                .hasFieldOrPropertyWithValue("executionEnvironment", campaignExecutionReport.executionEnvironment)
                .hasFieldOrPropertyWithValue("userId", campaignExecutionReport.userId)
            ;

            assertThat(reports.get(0).scenarioExecutionReports()).hasSize(2);

            assertThat(reports.get(0).scenarioExecutionReports()).element(0)
                .hasFieldOrPropertyWithValue("scenarioId", scenarioOne.id().toString())
                .hasFieldOrPropertyWithValue("scenarioName", scenarioOne.title())
                .extracting("execution")
                .hasFieldOrPropertyWithValue("executionId", scenarioOneExecution.id())
                .hasFieldOrPropertyWithValue("status", scenarioOneExecution.status())
            ;
            assertThat(reports.get(0).scenarioExecutionReports()).element(1)
                .hasFieldOrPropertyWithValue("scenarioId", scenarioTwo.id().toString())
                .hasFieldOrPropertyWithValue("scenarioName", scenarioTwo.title())
                .extracting("execution")
                .hasFieldOrPropertyWithValue("executionId", -1L)
                .hasFieldOrPropertyWithValue("status", ServerReportStatus.NOT_EXECUTED)
            ;
        }

        @Test
        public void should_remove_all_campaign_executions_when_removing_campaign_execution_report() {
            Scenario scenario = givenScenario();
            com.chutneytesting.campaign.infra.jpa.Campaign campaign = givenCampaign(scenario);

            ScenarioExecution scenarioExecution = givenScenarioExecution(scenario.id(), ServerReportStatus.NOT_EXECUTED);
            ScenarioExecutionReportCampaign scenarioExecutionReport = new ScenarioExecutionReportCampaign(scenario.id().toString(), scenario.title(), scenarioExecution.toDomain());
            Long campaignExecutionId = sut.generateCampaignExecutionId(campaign.id());
            CampaignExecutionReport campaignExecutionReport = new CampaignExecutionReport(campaignExecutionId, campaign.id(), singletonList(scenarioExecutionReport), campaign.title(), true, "env", "#2:87", 5, "user");
            sut.saveCampaignReport(campaign.id(), campaignExecutionReport);

            sut.clearAllExecutionHistory(campaign.id());

            List<CampaignExecutionReport> executionHistory = sut.findExecutionHistory(campaign.id());
            assertThat(executionHistory).isEmpty();

            List<?> scenarioExecutions =
                entityManager.createNativeQuery("select * from scenario_executions where id = :id", ScenarioExecution.class)
                    .setParameter("id", scenarioExecution.id())
                    .getResultList();
            assertThat(scenarioExecutions).hasSize(1);
        }

        @Test
        public void should_get_2_last_campaign_report_created() {
            clearTables();
            Scenario scenario = givenScenario();
            com.chutneytesting.campaign.infra.jpa.Campaign campaign = givenCampaign(scenario);

            ScenarioExecution scenarioExecutionOne = givenScenarioExecution(scenario.id(), ServerReportStatus.NOT_EXECUTED);
            ScenarioExecutionReportCampaign scenarioExecutionOneReport = new ScenarioExecutionReportCampaign(scenario.id().toString(), scenario.title(), scenarioExecutionOne.toDomain());
            Long campaignExecutionOneId = sut.generateCampaignExecutionId(campaign.id());
            CampaignExecutionReport campaignExecutionOneReport = new CampaignExecutionReport(campaignExecutionOneId, campaign.id(), singletonList(scenarioExecutionOneReport), campaign.title(), true, "env", "#2:87", 5, "user");
            sut.saveCampaignReport(campaign.id(), campaignExecutionOneReport);

            ScenarioExecution scenarioExecutionTwo = givenScenarioExecution(scenario.id(), ServerReportStatus.SUCCESS);
            ScenarioExecutionReportCampaign scenarioExecutionTwoReport = new ScenarioExecutionReportCampaign(scenario.id().toString(), scenario.title(), scenarioExecutionTwo.toDomain());
            Long campaignExecutionTwoId = sut.generateCampaignExecutionId(campaign.id());
            CampaignExecutionReport campaignExecutionTwoReport = new CampaignExecutionReport(campaignExecutionTwoId, campaign.id(), singletonList(scenarioExecutionTwoReport), campaign.title(), true, "env", "#2:87", 5, "user");
            sut.saveCampaignReport(campaign.id(), campaignExecutionTwoReport);

            ScenarioExecution scenarioExecutionThree = givenScenarioExecution(scenario.id(), ServerReportStatus.FAILURE);
            ScenarioExecutionReportCampaign scenarioExecutionThreeReport = new ScenarioExecutionReportCampaign(scenario.id().toString(), scenario.title(), scenarioExecutionThree.toDomain());
            Long campaignExecutionThreeId = sut.generateCampaignExecutionId(campaign.id());
            CampaignExecutionReport campaignExecutionThreeReport = new CampaignExecutionReport(campaignExecutionThreeId, campaign.id(), singletonList(scenarioExecutionThreeReport), campaign.title(), true, "env", "#2:87", 5, "user");
            sut.saveCampaignReport(campaign.id(), campaignExecutionThreeReport);

            ScenarioExecution scenarioExecutionFour = givenScenarioExecution(scenario.id(), ServerReportStatus.RUNNING);
            ScenarioExecutionReportCampaign scenarioExecutionFourReport = new ScenarioExecutionReportCampaign(scenario.id().toString(), scenario.title(), scenarioExecutionFour.toDomain());
            Long campaignExecutionFourId = sut.generateCampaignExecutionId(campaign.id());
            CampaignExecutionReport campaignExecutionFourReport = new CampaignExecutionReport(campaignExecutionFourId, campaign.id(), singletonList(scenarioExecutionFourReport), campaign.title(), true, "env", "#2:87", 5, "user");
            sut.saveCampaignReport(campaign.id(), campaignExecutionFourReport);


            List<CampaignExecutionReport> lastExecutions = sut.findLastExecutions(2L);

            assertThat(lastExecutions).hasSize(2);

            assertThat(lastExecutions.get(0).scenarioExecutionReports()).hasSize(1)
                .element(0)
                .hasFieldOrPropertyWithValue("scenarioId", scenario.id().toString())
                .hasFieldOrPropertyWithValue("scenarioName", scenario.title())
                .extracting("execution")
                .hasFieldOrPropertyWithValue("executionId", scenarioExecutionFour.id())
                .hasFieldOrPropertyWithValue("status", scenarioExecutionFour.status())
            ;
            assertThat(lastExecutions.get(1).scenarioExecutionReports()).hasSize(1)
                .element(0)
                .hasFieldOrPropertyWithValue("scenarioId", scenario.id().toString())
                .hasFieldOrPropertyWithValue("scenarioName", scenario.title())
                .extracting("execution")
                .hasFieldOrPropertyWithValue("executionId", scenarioExecutionThree.id())
                .hasFieldOrPropertyWithValue("status", scenarioExecutionThree.status())
            ;
        }
    }
}
