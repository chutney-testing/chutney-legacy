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

package integrationtest;

import static com.chutneytesting.engine.api.execution.StatusDto.FAILURE;
import static com.chutneytesting.engine.api.execution.StatusDto.SUCCESS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;

import com.chutneytesting.engine.api.execution.EnvironmentDto;
import com.chutneytesting.engine.api.execution.StepDefinitionDto;
import com.chutneytesting.engine.api.execution.StepExecutionReportDto;
import com.chutneytesting.glacio.GlacioAdapterConfiguration;
import com.chutneytesting.glacio.api.ExecutionRequestMapper;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.assertj.core.util.Files;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

public class EngineIntegrationTest {

    private static final String ENVIRONMENT = "ENV";

    private static GlacioAdapterConfiguration glacioAdapterConfiguration;

    @BeforeAll
    public static void setUp() throws IOException {
        String envFolderPath = "src/test/resources/conf";
        glacioAdapterConfiguration = new GlacioAdapterConfiguration(envFolderPath);
    }

    @Test
    public void should_execute_simple_feature() {
        List<StepExecutionReportDto> reports = executeFeature("/integration/simple_parser.feature");

        // Then
        StepExecutionReportDto report = reports.get(0); // Success/Debug scenario
        assertThat(report.status).isEqualTo(SUCCESS);
        assertThat(report.steps).hasSize(6);
        assertThat(report.steps.get(0).type).isEqualTo("debug");
        assertThat(report.steps.get(1).type).isEqualTo("success");
        assertThat(report.steps.get(2).steps.get(0).type).isEqualTo("debug");
        assertThat(report.steps.get(3).steps.get(0).type).isEqualTo("success");
        assertThat(report.steps.get(4).steps.get(0).steps.get(0).type).isEqualTo("debug");
        assertThat(report.steps.get(5).steps.get(0).steps.get(0).type).isEqualTo("success");

        report = reports.get(1); // Fail scenario
        assertThat(report.status).isEqualTo(FAILURE);
        assertThat(report.steps).hasSize(1);
        assertThat(report.steps.get(0).steps.get(0).steps.get(0).steps.get(0).type).isEqualTo("fail");
    }

    @Test
    public void should_execute_sleep_feature() {
        List<StepExecutionReportDto> reports = executeFeature("/integration/sleep_parser.feature");

        // Then
        assertThat(reports).hasSize(1);
        StepExecutionReportDto report = reports.get(0); // Specific parser
        assertThat(report.status).isEqualTo(SUCCESS);
        assertThat(report.steps).hasSize(3);
        assertThat(report.steps.get(0).steps.get(0).type).isEqualTo("success");
        assertThat(report.steps.get(1).steps.get(0).type).isEqualTo("sleep");
        assertThat(report.steps.get(1).steps.get(0).context.evaluatedInputs).containsExactly(entry("duration", "1 sec"));
        assertThat(report.steps.get(1).steps.get(1).type).isEqualTo("success");
        assertThat(report.steps.get(1).steps.get(2).type).isEqualTo("sleep");
        assertThat(report.steps.get(1).steps.get(2).context.evaluatedInputs).containsExactly(entry("duration", "200 ms"));
        assertThat(report.steps.get(2).steps.get(0).type).isEqualTo("sleep");
        assertThat(report.steps.get(2).steps.get(0).context.evaluatedInputs).containsExactly(entry("duration", "300 ms"));
        assertThat(report.steps.get(2).steps.get(1).type).isEqualTo("success");
        assertThat(report.steps.get(2).steps.get(2).type).isEqualTo("sleep");
        assertThat(report.steps.get(2).steps.get(2).context.evaluatedInputs).containsExactly(entry("duration", "120 ms"));
        assertThat(report.steps.get(2).steps.get(3).type).isEqualTo("success");
        assertThat(report.steps.get(2).steps.get(4).type).isEqualTo("sleep");
        assertThat(report.steps.get(2).steps.get(4).context.evaluatedInputs).containsExactly(entry("duration", "390 ms"));
    }

    @Test
    public void should_execute_context_put_feature() {
        List<StepExecutionReportDto> reports = executeFeature("/integration/context-put_parser.feature");

        // Then
        assertThat(reports).hasSize(2);
        StepExecutionReportDto report = reports.get(0); // Specific parser
        assertThat(report.status).isEqualTo(SUCCESS);
        assertThat(report.steps).hasSize(3);
        assertThat(report.steps.get(0).steps.get(0).type).isEqualTo("success");
        assertThat(report.steps.get(1).steps.get(0).type).isEqualTo("context-put");
        assertThat(report.steps.get(1).steps.get(0).context.evaluatedInputs).containsExactly(entry("entries", Map.of("var1", "value1 split", "var 2", "value2")));
        assertThat(report.steps.get(2).steps.get(0).type).isEqualTo("context-put");
        assertThat(report.steps.get(2).steps.get(0).context.evaluatedInputs).containsExactly(entry("entries", Map.of("var1", "value1 split", "var 2", "value2")));

        report = reports.get(1); // Default parser
        assertThat(report.status).isEqualTo(SUCCESS);
        assertThat(report.steps).hasSize(2);
        assertThat(report.steps.get(0).steps.get(0).type).isEqualTo("success");
        assertThat(report.steps.get(1).steps.get(0).type).isEqualTo("context-put");
        assertThat(report.steps.get(1).steps.get(0).information.get(2)).isEqualTo("Validation [assertion : ${'value1 split'.equals(#var1)}] : OK");
        assertThat(report.steps.get(1).steps.get(0).context.evaluatedInputs).containsExactly(entry("entries", Map.of("var1", "value1 split", "var 2", "value2")));
    }

    @Test
    public void should_execute_http_feature() {
        List<StepExecutionReportDto> reports = executeFeature("/integration/http_parser.feature");

        // Then
        assertThat(reports).hasSize(1);
        StepExecutionReportDto report = reports.get(0);
        assertThat(report.status).isEqualTo(SUCCESS);
        assertThat(report.steps).hasSize(2);
        assertThat(report.steps.get(0).type).isEqualTo("http-get");
        assertThat(report.steps.get(0).targetName).isEqualTo("GITHUB_API");
        assertThat(report.steps.get(0).targetUrl).isEqualTo("https://api.github.com");
        assertThat(report.steps.get(0).context.evaluatedInputs)
            .containsOnly(
                entry("uri", "/orgs/chutney-testing"),
                entry("timeout", "2000 s"),
                entry("headers", Map.of("X-Extra-Header", "An extra header"))
            );
        assertThat(report.steps.get(0).context.stepResults.get("statusOk")).isEqualTo(Boolean.TRUE);
        assertThat(report.steps.get(0).context.stepResults.get("jsonBody")).asString().isNotBlank();
        assertThat(report.steps.get(0).context.stepResults.get("headersString")).asString().isNotBlank();
        assertThat(report.steps.get(1).type).isEqualTo("debug");
    }

    @Test
    @Disabled
    public void should_execute_sql_feature() {
        List<StepExecutionReportDto> reports = executeFeature("/integration/sql_parser.feature");

        // Then
        assertThat(reports.get(0).status).isEqualTo(SUCCESS);
    }

    @Test
    @Disabled
    public void should_execute_blackbox_feature() {
        List<StepExecutionReportDto> reports = executeFeature("/integration/blackbox.feature");

        // Then
        assertThat(reports.get(0).status).isEqualTo(SUCCESS);
    }

    @Test
    public void should_execute_strategy_feature() {
        List<StepExecutionReportDto> reports = executeFeature("/integration/strategy_parser.feature");

        /* Then */
        it_should_continue_on_softly_failed_steps(reports.get(0));
        it_should_continue_on_softly_failed_steps(reports.get(1));
        it_should_continue_on_softly_failed_steps(reports.get(2));
        it_should_continue_on_softly_failed_steps(reports.get(3));

        /* And  */
        it_should_gracefully_fallback_on_unknown_strategy(reports.get(4));
        /* And  */
        it_should_not_affect_parsing_action_parameters_of_specific_parsers(reports.get(5));
        /* And  */
        it_should_work_with_default_parser(reports.get(6));

    }

    private void it_should_continue_on_softly_failed_steps(StepExecutionReportDto report) {
        assertThat(report.status).isEqualTo(FAILURE);
        assertThat(report.steps.get(0).status).isEqualTo(FAILURE);
        assertThat(report.steps.get(1).status).isEqualTo(SUCCESS);
    }

    private void it_should_gracefully_fallback_on_unknown_strategy(StepExecutionReportDto report) {
        assertThat(report.status).isEqualTo(SUCCESS);
        assertThat(report.steps.get(0).name).isEqualTo("a step succeeds");
        assertThat(report.steps.get(0).strategy).isEmpty();
    }

    private void it_should_not_affect_parsing_action_parameters_of_specific_parsers(StepExecutionReportDto report) {
        assertThat(report.steps.get(0).steps.get(0).strategy).isEqualTo("soft-assert");
        assertThat(report.status).isEqualTo(SUCCESS);
    }

    private void it_should_work_with_default_parser(StepExecutionReportDto report) {
        assertThat(report.steps.get(0).strategy).isEqualTo("retry-with-timeout");
        assertThat(report.status).isEqualTo(FAILURE);
    }

    @Test
    public void should_execute_strategy_feature_with_i18n_params() {
        List<StepExecutionReportDto> reports = executeFeature("/integration/strategy_parser_fr.feature");

        /* Then */
        it_should_continue_on_softly_failed_steps(reports.get(0));
        /* And there is no other error than the step failing as intended */
        assertThat(reports.get(0).steps.get(0).steps.get(0).errors.get(0)).startsWith("Failed at");
    }

    private List<StepExecutionReportDto> executeFeature(String filePath) {
        // Given
        String feature = fileContent(filePath);
        List<StepDefinitionDto> stepDefinitions = glacioAdapterConfiguration.glacioAdapter().toChutneyStepDefinition(feature, ENVIRONMENT);

        // When
        List<StepExecutionReportDto> reports = new ArrayList<>();
        stepDefinitions.forEach(stepDefinition -> reports.add(execute(stepDefinition)));
        return reports;
    }

    private StepExecutionReportDto execute(StepDefinitionDto stepDefinitionDto) {
        EnvironmentDto environment = new EnvironmentDto(ENVIRONMENT, Collections.emptyMap());
        return glacioAdapterConfiguration.executionConfiguration().embeddedTestEngine().execute(ExecutionRequestMapper.toDto(stepDefinitionDto, environment));
    }

    private String fileContent(String resourcePath) {
        return Files.contentOf(new File(EngineIntegrationTest.class.getResource(resourcePath).getPath()), StandardCharsets.UTF_8);
    }
}
