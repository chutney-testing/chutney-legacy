package test.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;

import com.chutneytesting.ExecutionConfiguration;
import com.chutneytesting.engine.api.glacio.GlacioAdapter;
import com.chutneytesting.engine.api.glacio.GlacioAdapterConfiguration;
import com.chutneytesting.engine.domain.execution.ScenarioExecution;
import com.chutneytesting.engine.domain.execution.StepDefinition;
import com.chutneytesting.engine.domain.execution.report.Status;
import com.chutneytesting.engine.domain.execution.report.StepExecutionReport;
import com.google.common.io.Resources;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import org.apache.groovy.util.Maps;
import org.assertj.core.util.Files;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

public class EngineIntegrationTest {

    private static final String ENVIRONMENT = "ENV";
    private static ExecutionConfiguration executionConfiguration;

    private static GlacioAdapter glacioAdapter;

    @BeforeAll
    public static void setUp() throws IOException {
        String ENV_FOLDER_PATH = "src/test/resources/conf";

        executionConfiguration = new ExecutionConfiguration();
        GlacioAdapterConfiguration glacioAdapterConfiguration = new GlacioAdapterConfiguration(executionConfiguration, ENV_FOLDER_PATH, ENV_FOLDER_PATH + "/endpoints.json");

        glacioAdapter = glacioAdapterConfiguration.glacioAdapter();
    }

    @Test
    public void should_execute_simple_feature() {
        List<StepExecutionReport> reports = executeFeature("integration/simple_parser.feature");

        // Then
        StepExecutionReport report = reports.get(0); // Success/Debug scenario
        assertThat(report.status).isEqualTo(Status.SUCCESS);
        assertThat(report.steps).hasSize(6);
        assertThat(report.steps.get(0).type).isEqualTo("debug");
        assertThat(report.steps.get(1).type).isEqualTo("success");
        assertThat(report.steps.get(2).steps.get(0).type).isEqualTo("debug");
        assertThat(report.steps.get(3).steps.get(0).type).isEqualTo("success");
        assertThat(report.steps.get(4).steps.get(0).steps.get(0).type).isEqualTo("debug");
        assertThat(report.steps.get(5).steps.get(0).steps.get(0).type).isEqualTo("success");

        report = reports.get(1); // Fail scenario
        assertThat(report.status).isEqualTo(Status.FAILURE);
        assertThat(report.steps).hasSize(1);
        assertThat(report.steps.get(0).steps.get(0).steps.get(0).steps.get(0).type).isEqualTo("fail");
    }

    @Test
    public void should_execute_sleep_feature() {
        List<StepExecutionReport> reports = executeFeature("integration/sleep_parser.feature");

        // Then
        assertThat(reports).hasSize(1);
        StepExecutionReport report = reports.get(0); // Specific parser
        assertThat(report.status).isEqualTo(Status.SUCCESS);
        assertThat(report.steps).hasSize(3);
        assertThat(report.steps.get(0).steps.get(0).type).isEqualTo("success");
        assertThat(report.steps.get(1).steps.get(0).type).isEqualTo("sleep");
        assertThat(report.steps.get(1).steps.get(0).evaluatedInputs).containsExactly(entry("duration", "1 sec"));
        assertThat(report.steps.get(1).steps.get(1).type).isEqualTo("success");
        assertThat(report.steps.get(1).steps.get(2).type).isEqualTo("sleep");
        assertThat(report.steps.get(1).steps.get(2).evaluatedInputs).containsExactly(entry("duration", "200 ms"));
        assertThat(report.steps.get(2).steps.get(0).type).isEqualTo("sleep");
        assertThat(report.steps.get(2).steps.get(0).evaluatedInputs).containsExactly(entry("duration", "300 ms"));
        assertThat(report.steps.get(2).steps.get(1).type).isEqualTo("success");
        assertThat(report.steps.get(2).steps.get(2).type).isEqualTo("sleep");
        assertThat(report.steps.get(2).steps.get(2).evaluatedInputs).containsExactly(entry("duration", "120 ms"));
        assertThat(report.steps.get(2).steps.get(3).type).isEqualTo("success");
        assertThat(report.steps.get(2).steps.get(4).type).isEqualTo("sleep");
        assertThat(report.steps.get(2).steps.get(4).evaluatedInputs).containsExactly(entry("duration", "390 ms"));
    }

    @Test
    public void should_execute_context_put_feature() {
        List<StepExecutionReport> reports = executeFeature("integration/context-put_parser.feature");

        // Then
        assertThat(reports).hasSize(2);
        StepExecutionReport report = reports.get(0); // Specific parser
        assertThat(report.status).isEqualTo(Status.SUCCESS);
        assertThat(report.steps).hasSize(3);
        assertThat(report.steps.get(0).steps.get(0).type).isEqualTo("success");
        assertThat(report.steps.get(1).steps.get(0).type).isEqualTo("context-put");
        assertThat(report.steps.get(1).steps.get(0).evaluatedInputs).containsExactly(entry("entries", Maps.of("var1", "value1 splitted", "var 2", "value2")));
        assertThat(report.steps.get(2).steps.get(0).type).isEqualTo("context-put");
        assertThat(report.steps.get(2).steps.get(0).evaluatedInputs).containsExactly(entry("entries", Maps.of("var1", "value1 splitted", "var 2", "value2")));

        report = reports.get(1); // Default parser
        assertThat(report.status).isEqualTo(Status.SUCCESS);
        assertThat(report.steps).hasSize(2);
        assertThat(report.steps.get(0).steps.get(0).type).isEqualTo("success");
        assertThat(report.steps.get(1).steps.get(0).type).isEqualTo("context-put");
        assertThat(report.steps.get(1).steps.get(0).evaluatedInputs).containsExactly(entry("entries", Maps.of("var1", "value1 splitted", "var 2", "value2")));
    }

    @Test
    public void should_execute_http_feature() {
        List<StepExecutionReport> reports = executeFeature("integration/http_parser.feature");

        // Then
        assertThat(reports).hasSize(1);
        StepExecutionReport report = reports.get(0);
        assertThat(report.status).isEqualTo(Status.SUCCESS);
        assertThat(report.steps).hasSize(2);
        assertThat(report.steps.get(0).type).isEqualTo("http-get");
        assertThat(report.steps.get(0).targetName).isEqualTo("GITHUB_API");
        assertThat(report.steps.get(0).targetUrl).isEqualTo("https://api.github.com");
        assertThat(report.steps.get(0).evaluatedInputs)
            .containsOnly(
                entry("uri", "/orgs/chutney-testing"),
                entry("timeout", "2000 s"),
                entry("headers", Maps.of("X-Extra-Header", "An extra header"))
            );
        assertThat(report.steps.get(0).scenarioContext.get("statusOk")).isEqualTo(Boolean.TRUE);
        assertThat(report.steps.get(0).scenarioContext.get("jsonBody")).asString().isNotBlank();
        assertThat(report.steps.get(0).scenarioContext.get("headersString")).asString().isNotBlank();
        assertThat(report.steps.get(1).type).isEqualTo("debug");
    }

    @Test
    @Disabled
    public void should_execute_sql_feature() {
        List<StepExecutionReport> reports = executeFeature("integration/sql_parser.feature");

        // Then
        assertThat(reports.get(0).status).isEqualTo(Status.SUCCESS);
    }

    @Test
    @Disabled
    public void should_execute_blackbox_feature() {
        List<StepExecutionReport> reports = executeFeature("integration/blackbox.feature");

        // Then
        assertThat(reports.get(0).status).isEqualTo(Status.SUCCESS);
    }

    @Test
    public void should_execute_strategy_feature() {
        List<StepExecutionReport> reports = executeFeature("integration/strategy_parser.feature");

        /* Then */ it_should_continue_on_softly_failed_steps(reports.get(0));
                   it_should_continue_on_softly_failed_steps(reports.get(1));
                   it_should_continue_on_softly_failed_steps(reports.get(2));
                   it_should_continue_on_softly_failed_steps(reports.get(3));

        /* And  */ it_should_gracefully_fallback_on_unknown_strategy(reports.get(4));
        /* And  */ it_should_not_affect_parsing_action_parameters_of_specific_parsers(reports.get(5));
        /* And  */ it_should_work_with_default_parser(reports.get(6));

    }

    private void it_should_continue_on_softly_failed_steps(StepExecutionReport report) {
        assertThat(report.status).isEqualTo(Status.FAILURE);
        assertThat(report.steps.get(0).status).isEqualTo(Status.FAILURE);
        assertThat(report.steps.get(1).status).isEqualTo(Status.SUCCESS);
    }

    private void it_should_gracefully_fallback_on_unknown_strategy(StepExecutionReport report) {
        assertThat(report.status).isEqualTo(Status.FAILURE);
        assertThat(report.steps.get(0).status).isEqualTo(Status.FAILURE);
        assertThat(report.steps.get(1).status).isEqualTo(Status.NOT_EXECUTED);
    }

    private void it_should_not_affect_parsing_action_parameters_of_specific_parsers(StepExecutionReport report) {
        assertThat(report.steps.get(0).steps.get(0).strategy).isEqualTo("soft-assert");
        assertThat(report.status).isEqualTo(Status.SUCCESS);
    }

    private void it_should_work_with_default_parser(StepExecutionReport report) {
        assertThat(report.steps.get(0).strategy).isEqualTo("retry-with-timeout");
        assertThat(report.status).isEqualTo(Status.FAILURE);
    }

    private List<StepExecutionReport> executeFeature(String filePath) {
        // Given
        String feature = fileContent(filePath);
        List<StepDefinition> stepDefinitions = glacioAdapter.toChutneyStepDefinition(feature, ENVIRONMENT);

        // When
        List<StepExecutionReport> reports = new ArrayList<>();
        stepDefinitions.forEach(stepDefinition -> reports.add(execute(stepDefinition)));
        return reports;
    }

    private StepExecutionReport execute(StepDefinition stepDefinition) {
        Long executionId = executionConfiguration.executionEngine().execute(stepDefinition, ScenarioExecution.createScenarioExecution());
        return executionConfiguration.reporter().subscribeOnExecution(executionId).blockingLast();
    }

    private String fileContent(String resourcePath) {
        return Files.contentOf(new File(Resources.getResource(resourcePath).getPath()), StandardCharsets.UTF_8);
    }
}
