package blackbox.stepdef;

import static org.assertj.core.api.Assertions.assertThat;

import blackbox.assertion.Assertions;
import blackbox.restclient.RestClient;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;

public class ScenarioExecutionRequestStepDefs {
    private static final Logger LOGGER = LoggerFactory.getLogger(ScenarioExecutionRequestStepDefs.class);

    private static final String EXECUTE_SCENARIO_URL = "/api/ui/scenario/execution/v1";

    private final TestContext context;
    private final RestClient secureRestClient;

    private ObjectMapper objectMapper;
    public static final String ENV = "GLOBAL";

    public ScenarioExecutionRequestStepDefs(TestContext context, RestClient secureRestClient) {
        this.context = context;
        this.secureRestClient = secureRestClient;
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new Jdk8Module());
    }

    @When("^last saved scenario is executed")
    public void executeScenarioById() {
        final ResponseEntity<String> response = executeScenario();
        context.putExecutionReport(response.getBody());
    }

    @When("^last saved scenario execution produces an error")
    public void executeIncorrectScenario() {
        try {
            executeScenario();
        } catch (HttpClientErrorException | HttpServerErrorException e) {
            context.putExecutionReport(e);
        }
    }

    private ResponseEntity<String> executeScenario() {
        return secureRestClient.defaultRequest()
            .withUrl(EXECUTE_SCENARIO_URL + "/" + context.getLastScenarioId() + "/" + ENV)
            .withBody("{}")
            .post(String.class);
    }

    @Then("^the scenario execution status is \"([^\"]*)\"$")
    public void checkTaskExecutionStatusIs(String expectedExecutionStatus) {
        assertJsonPathEqualValue("$.status", expectedExecutionStatus);
    }

    @Then("^the extracted value is '(.*)'$")
    public void checkExtractedValueIs(String expectedValue) {
        assertJsonPathEqualValue("$.report.steps[1].information[1]", expectedValue);
    }

    @Then("^the json resulting context is$")
    public void the_old_json_resulting_context_is(String expectedResult) {
        assertJsonPathEqualValue("$.report.steps[0].information[0]", expectedResult);
    }

    @Then("^the last record results is (.*)$")
    public void the_record_result(String expectedResult) {
        assertJsonPathEqualValue("$.report.steps[-1:].information[0]", expectedResult);
    }

    @Then("^the report status is (SUCCESS|FAILURE)$")
    public void the_report_step_is(String expectedResultStatus) {
        assertJsonPathEqualValue("$.report.status", expectedResultStatus);
    }

    @Then("^the resulting step is (SUCCESS|FAILURE)$")
    public void the_resulting_step_is(String expectedResultStatus) {
        assertJsonPathEqualValue("$.status", expectedResultStatus);
    }

    @Then("^the error status is (.*) with message (.*)$")
    public void anErrorOccursWithStatusStatusAndMessage(int status, String message) {
        Object executionReport = context.getExecutionReport();
        assertThat(executionReport).isInstanceOfAny(HttpClientErrorException.class, HttpServerErrorException.class);
        if(executionReport instanceof HttpClientErrorException) {
            assertThat(((HttpClientErrorException)executionReport).getRawStatusCode()).isEqualTo(status);
            assertThat(((HttpClientErrorException)executionReport).getResponseBodyAsString()).isEqualTo(message);
        } else {
            assertThat(((HttpServerErrorException)executionReport).getRawStatusCode()).isEqualTo(status);
            assertThat(((HttpServerErrorException)executionReport).getResponseBodyAsString()).isEqualTo(message);
        }
    }

    private void assertJsonPathEqualValue(String path, String value) {
        String lastExecutionReport = context.getExecutionReport();
        LOGGER.debug("Execution report : \n" + lastExecutionReport);
        Assertions.assertThatJson(lastExecutionReport)
            .hasPathEqualsTo(path, value);
    }
}
