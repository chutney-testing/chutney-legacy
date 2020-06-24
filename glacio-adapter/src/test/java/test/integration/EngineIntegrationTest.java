package test.integration;

import static java.util.Arrays.asList;
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

    @Test
    public void should_adapt_scenario_with_feature_background() {
        String feature = fileContent("integration/with_background.feature");
        List<StepDefinition> stepDefinitions = glacioAdapter.toChutneyStepDefinition(feature, ENVIRONMENT);

        assertThat(stepDefinitions).hasSize(1);
        StepDefinition scenario = stepDefinitions.get(0);
        assertThat(scenario.steps).hasSize(4);

        StepDefinition stepDefinition = scenario.steps.get(0);
        assertSizeAndName(stepDefinition, "A background step", 0);

        stepDefinition = scenario.steps.get(1);
        assertSizeAndName(stepDefinition, "Another one with substeps", 2);
        assertSizeAndName(stepDefinition.steps.get(0), "First substep", 0);
        assertSizeAndName(stepDefinition.steps.get(1), "Second substep", 0);

        stepDefinition = scenario.steps.get(2);
        assertSizeAndName(stepDefinition, "Something happens", 0);

        stepDefinition = scenario.steps.get(3);
        assertSizeAndName(stepDefinition, "Background step should be taken into account", 0);
    }

    @Test
    public void should_adapt_scenario_with_scenario_outline() {
        String feature = fileContent("integration/with_scenario_outline.feature");
        List<List<String>> examples = asList(
            asList("first", "value11", "value12", "1"),
            asList("second", "value21", "value22", "2")
        );

        List<StepDefinition> stepDefinitions = glacioAdapter.toChutneyStepDefinition(feature, ENVIRONMENT);

        assertThat(stepDefinitions).hasSize(examples.size());

        for (int i = 0; i < examples.size(); i++) {
            List<String> example = examples.get(i);

            StepDefinition scenario = stepDefinitions.get(i);
            assertThat(scenario.steps).hasSize(3);

            StepDefinition stepDefinition = scenario.steps.get(0);
            assertSizeAndName(stepDefinition, "A step outline " + example.get(0), 0);

            stepDefinition = scenario.steps.get(1);
            assertSizeAndName(stepDefinition, "Parse this step " + example.get(0) + " with substeps", 2);
            assertSizeAndName(stepDefinition.steps.get(0), "First substep with param " + example.get(1), 0);
            assertSizeAndName(stepDefinition.steps.get(1), "Second substep with param " + example.get(2), 0);

            stepDefinition = scenario.steps.get(2);
            assertSizeAndName(stepDefinition, "Multiple scenarios " + example.get(3) + " should be parsed", 0);
        }
    }

    private void assertSizeAndName(StepDefinition step, String expectedName, Integer expectedSize) {
        assertThat(step.name).as("StepDefinition name").isEqualTo(expectedName);
        assertThat(step.steps).as("StepDefinition substeps size").hasSize(expectedSize);
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
