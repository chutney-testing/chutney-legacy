package test.integration;

import static org.assertj.core.api.Assertions.assertThat;

import com.chutneytesting.engine.api.glacio.GlacioAdapter;
import com.chutneytesting.engine.domain.execution.ExecutionEngine;
import com.chutneytesting.engine.domain.execution.ScenarioExecution;
import com.chutneytesting.engine.domain.execution.StepDefinition;
import com.chutneytesting.engine.domain.execution.report.Status;
import com.chutneytesting.engine.domain.execution.report.StepExecutionReport;
import com.chutneytesting.engine.domain.report.Reporter;
import com.google.common.io.Resources;
import java.io.File;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import org.assertj.core.util.Files;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@ContextConfiguration
public class EngineIntegrationTest {

    private static final String ENVIRONMENT = "ENV";

    @Configuration
    @ComponentScan("com.chutneytesting")
    public static class SpringConfig {
    }

    @Autowired
    private ExecutionEngine executionEngine;
    @Autowired
    private Reporter reporter;
    @Autowired
    private GlacioAdapter glacioAdapter;

    @Test
    public void should_execute_simple_feature() {
        List<StepExecutionReport> reports = executeFeature("integration/simple_parser.feature");

        // Then
        assertThat(reports.get(0).status).isEqualTo(Status.SUCCESS); // Success/Debug scenario
        assertThat(reports.get(1).status).isEqualTo(Status.FAILURE); // Fail scenario
    }

    @Test
    public void should_execute_sleep_feature() {
        List<StepExecutionReport> reports = executeFeature("integration/sleep_parser.feature");

        // Then
        assertThat(reports.get(0).status).isEqualTo(Status.SUCCESS);
    }

    @Test
    public void should_execute_context_put_feature() {
        List<StepExecutionReport> reports = executeFeature("integration/context-put_parser.feature");

        // Then
        assertThat(reports.get(0).status).isEqualTo(Status.SUCCESS);
    }

    @Test
    public void should_execute_context_put_feature_with_default_parser() {
        List<StepExecutionReport> reports = executeFeature("integration/context-tasks_default-parser.feature");

        // Then
        assertThat(reports.get(0).status).isEqualTo(Status.SUCCESS);
    }

    @Test
    @Ignore
    public void should_execute_http_feature() {
        List<StepExecutionReport> reports = executeFeature("integration/http_parser.feature");

        // Then
        assertThat(reports.get(0).status).isEqualTo(Status.SUCCESS);
    }

    @Test
    @Ignore
    public void should_execute_sql_feature() {
        List<StepExecutionReport> reports = executeFeature("integration/sql_parser.feature");

        // Then
        assertThat(reports.get(0).status).isEqualTo(Status.SUCCESS);
    }

    @Test
    @Ignore
    public void should_execute_blackbox_feature() {
        List<StepExecutionReport> reports = executeFeature("integration/blackbox.feature");

        // Then
        assertThat(reports.get(0).status).isEqualTo(Status.SUCCESS);
    }


    private List<StepExecutionReport> executeFeature(String s) {
        // Given
        String feature = fileContent(s);
        List<StepDefinition> stepDefinitions = glacioAdapter.toChutneyStepDefinition(feature, ENVIRONMENT);

        // When
        List<StepExecutionReport> reports = new ArrayList<>();
        stepDefinitions.forEach(stepDefinition -> reports.add(execute(stepDefinition)));
        return reports;
    }

    private StepExecutionReport execute(StepDefinition stepDefinition) {
        Long executionId = executionEngine.execute(stepDefinition, ScenarioExecution.createScenarioExecution());
        return reporter.subscribeOnExecution(executionId).blockingLast();
    }

    private String fileContent(String resourcePath) {
        return Files.contentOf(new File(Resources.getResource(resourcePath).getPath()), Charset.forName("UTF-8"));
    }
}
