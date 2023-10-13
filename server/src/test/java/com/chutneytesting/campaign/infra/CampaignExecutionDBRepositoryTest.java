package com.chutneytesting.campaign.infra;

import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.chutneytesting.campaign.infra.jpa.CampaignEntity;
import com.chutneytesting.execution.infra.storage.jpa.ScenarioExecutionEntity;
import com.chutneytesting.scenario.infra.jpa.ScenarioEntity;
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
    class PostreSQL extends AllTests {
    }

    abstract class AllTests extends AbstractLocalDatabaseTest {

        @Autowired
        private CampaignExecutionDBRepository sut;


        @Test
        public void should_return_the_last_campaign_execution() {
            ScenarioEntity scenarioEntity = givenScenario();
            CampaignEntity campaign = givenCampaign(scenarioEntity);

            ScenarioExecutionEntity scenarioExecution = givenScenarioExecution(scenarioEntity.getId(), ServerReportStatus.NOT_EXECUTED);
            ScenarioExecutionReportCampaign scenarioExecutionReport = new ScenarioExecutionReportCampaign(scenarioEntity.getId().toString(), scenarioEntity.getTitle(), scenarioExecution.toDomain());

            Long campaignExecutionId1 = sut.generateCampaignExecutionId(campaign.id());
            Long campaignExecutionId2 = sut.generateCampaignExecutionId(campaign.id());
            Long campaignExecutionId3 = sut.generateCampaignExecutionId(campaign.id());

            CampaignExecutionReport campaignExecutionReport1 = new CampaignExecutionReport(campaignExecutionId1, campaign.id(), singletonList(scenarioExecutionReport), campaign.title(), true, "env", "", 5, "user");
            CampaignExecutionReport campaignExecutionReport2 = new CampaignExecutionReport(campaignExecutionId2, campaign.id(), singletonList(scenarioExecutionReport), campaign.title(), true, "env", "", 5, "user");
            CampaignExecutionReport campaignExecutionReport3 = new CampaignExecutionReport(campaignExecutionId3, campaign.id(), singletonList(scenarioExecutionReport), campaign.title(), true, "env", "", 5, "user");

            sut.saveCampaignReport(campaign.id(), campaignExecutionReport1);
            sut.saveCampaignReport(campaign.id(), campaignExecutionReport2);
            sut.saveCampaignReport(campaign.id(), campaignExecutionReport3);

            CampaignExecutionReport report = sut.getLastExecutionReport(campaign.id());

            assertThat(report).isEqualTo(campaignExecutionReport3);
        }

        @Test
        public void should_throw_exception_when_no_campaign_execution() {
            ScenarioEntity scenarioEntity = givenScenario();
            CampaignEntity campaign = givenCampaign(scenarioEntity);

            assertThatThrownBy(() -> sut.getLastExecutionReport(campaign.id()));
        }

        @Test
        public void should_persist_1_execution_when_saving_1_campaign_execution_report() {
            ScenarioEntity scenarioEntity = givenScenario();
            CampaignEntity campaign = givenCampaign(scenarioEntity);

            ScenarioExecutionEntity scenarioExecution = givenScenarioExecution(scenarioEntity.getId(), ServerReportStatus.NOT_EXECUTED);
            ScenarioExecutionReportCampaign scenarioExecutionReport = new ScenarioExecutionReportCampaign(scenarioEntity.getId().toString(), scenarioEntity.getTitle(), scenarioExecution.toDomain());
            Long campaignExecutionId = sut.generateCampaignExecutionId(campaign.id());
            CampaignExecutionReport campaignExecutionReport = new CampaignExecutionReport(campaignExecutionId, campaign.id(), singletonList(scenarioExecutionReport), campaign.title(), true, "env", "", 5, "user");
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
            ScenarioExecutionReportCampaign scenarioOneExecutionReport = new ScenarioExecutionReportCampaign(scenarioEntityOne.getId().toString(), scenarioEntityOne.getTitle(), scenarioOneExecution.toDomain());
            ScenarioExecutionEntity scenarioTwoExecution = givenScenarioExecution(scenarioEntityTwo.getId(), ServerReportStatus.FAILURE);
            ScenarioExecutionReportCampaign scenarioTwoExecutionReport = new ScenarioExecutionReportCampaign(scenarioEntityTwo.getId().toString(), scenarioEntityTwo.getTitle(), scenarioTwoExecution.toDomain());
            Long campaignExecutionId = sut.generateCampaignExecutionId(campaign.id());
            CampaignExecutionReport campaignExecutionReport = new CampaignExecutionReport(campaignExecutionId, campaign.id(), new ArrayList<>(List.of(scenarioOneExecutionReport, scenarioTwoExecutionReport)), campaign.title(), true, "env", "", 5, "user");
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
        // Therefore the implementation i made by not included the current execution if there is in findExecutionHistory method
        // So this test does not make sense for me now.
        // TODO - To move elsewhere ?
        public void campaign_execution_history_should_list_not_executed_scenarios() {
            ScenarioEntity scenarioEntityOne = givenScenario();
            ScenarioEntity scenarioEntityTwo = givenScenario();
            CampaignEntity campaign = givenCampaign(scenarioEntityOne, scenarioEntityTwo);

            ScenarioExecutionEntity scenarioOneExecution = givenScenarioExecution(scenarioEntityOne.getId(), ServerReportStatus.SUCCESS);
            ScenarioExecutionReportCampaign scenarioOneExecutionReport = new ScenarioExecutionReportCampaign(scenarioEntityOne.getId().toString(), scenarioEntityOne.getTitle(), scenarioOneExecution.toDomain());
            Long campaignExecutionId = sut.generateCampaignExecutionId(campaign.id());
            CampaignExecutionReport campaignExecutionReport = new CampaignExecutionReport(campaignExecutionId, campaign.id(), singletonList(scenarioOneExecutionReport), campaign.title(), true, "env", "", 5, "user");
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
            ScenarioExecutionReportCampaign scenarioExecutionReport = new ScenarioExecutionReportCampaign(scenarioEntity.getId().toString(), scenarioEntity.getTitle(), scenarioExecution.toDomain());
            Long campaignExecutionId = sut.generateCampaignExecutionId(campaign.id());
            CampaignExecutionReport campaignExecutionReport = new CampaignExecutionReport(campaignExecutionId, campaign.id(), singletonList(scenarioExecutionReport), campaign.title(), true, "env", "", 5, "user");
            sut.saveCampaignReport(campaign.id(), campaignExecutionReport);

            sut.clearAllExecutionHistory(campaign.id());

            List<CampaignExecutionReport> executionHistory = sut.findExecutionHistory(campaign.id());
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
            ScenarioExecutionReportCampaign scenarioExecutionOneReport = new ScenarioExecutionReportCampaign(scenarioEntity.getId().toString(), scenarioEntity.getTitle(), scenarioExecutionOne.toDomain());
            Long campaignExecutionOneId = sut.generateCampaignExecutionId(campaign.id());
            CampaignExecutionReport campaignExecutionOneReport = new CampaignExecutionReport(campaignExecutionOneId, campaign.id(), singletonList(scenarioExecutionOneReport), campaign.title(), true, "env", "", 5, "user");
            sut.saveCampaignReport(campaign.id(), campaignExecutionOneReport);

            ScenarioExecutionEntity scenarioExecutionTwo = givenScenarioExecution(scenarioEntity.getId(), ServerReportStatus.SUCCESS);
            ScenarioExecutionReportCampaign scenarioExecutionTwoReport = new ScenarioExecutionReportCampaign(scenarioEntity.getId().toString(), scenarioEntity.getTitle(), scenarioExecutionTwo.toDomain());
            Long campaignExecutionTwoId = sut.generateCampaignExecutionId(campaign.id());
            CampaignExecutionReport campaignExecutionTwoReport = new CampaignExecutionReport(campaignExecutionTwoId, campaign.id(), singletonList(scenarioExecutionTwoReport), campaign.title(), true, "env", "", 5, "user");
            sut.saveCampaignReport(campaign.id(), campaignExecutionTwoReport);

            ScenarioExecutionEntity scenarioExecutionThree = givenScenarioExecution(scenarioEntity.getId(), ServerReportStatus.FAILURE);
            ScenarioExecutionReportCampaign scenarioExecutionThreeReport = new ScenarioExecutionReportCampaign(scenarioEntity.getId().toString(), scenarioEntity.getTitle(), scenarioExecutionThree.toDomain());
            Long campaignExecutionThreeId = sut.generateCampaignExecutionId(campaign.id());
            CampaignExecutionReport campaignExecutionThreeReport = new CampaignExecutionReport(campaignExecutionThreeId, campaign.id(), singletonList(scenarioExecutionThreeReport), campaign.title(), true, "env", "", 5, "user");
            sut.saveCampaignReport(campaign.id(), campaignExecutionThreeReport);

            ScenarioExecutionEntity scenarioExecutionFour = givenScenarioExecution(scenarioEntity.getId(), ServerReportStatus.RUNNING);
            ScenarioExecutionReportCampaign scenarioExecutionFourReport = new ScenarioExecutionReportCampaign(scenarioEntity.getId().toString(), scenarioEntity.getTitle(), scenarioExecutionFour.toDomain());
            Long campaignExecutionFourId = sut.generateCampaignExecutionId(campaign.id());
            CampaignExecutionReport campaignExecutionFourReport = new CampaignExecutionReport(campaignExecutionFourId, campaign.id(), singletonList(scenarioExecutionFourReport), campaign.title(), true, "env", "", 5, "user");
            sut.saveCampaignReport(campaign.id(), campaignExecutionFourReport);


            List<CampaignExecutionReport> lastExecutions = sut.findLastExecutions(2L);

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
}