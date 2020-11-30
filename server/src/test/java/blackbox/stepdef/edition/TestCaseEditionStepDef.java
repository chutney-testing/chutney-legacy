package blackbox.stepdef.edition;

import static java.time.Instant.now;
import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

import blackbox.restclient.RestClient;
import blackbox.stepdef.TestContext;
import com.chutneytesting.design.api.editionlock.TestCaseEditionDto;
import com.chutneytesting.design.api.scenario.v2_0.dto.GwtTestCaseDto;
import com.chutneytesting.design.api.scenario.v2_0.dto.ImmutableGwtScenarioDto;
import com.chutneytesting.design.api.scenario.v2_0.dto.ImmutableGwtStepDto;
import com.chutneytesting.design.api.scenario.v2_0.dto.ImmutableGwtTestCaseDto;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.cucumber.java.Before;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import java.io.IOException;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.assertj.core.util.Lists;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;

public class TestCaseEditionStepDef {

    private final TestContext context;
    private final RestClient secureRestClient;
    private final ObjectMapper om;
    private Instant startInstant;

    private static final String GWT_SCENARIO_URL = "/api/scenario/v2/";
    private static final String TESTCASE_EDITION_URL = "/api/v1/editions/testcases/";

    public TestCaseEditionStepDef(TestContext context, RestClient secureRestClient) {
        this.context = context;
        this.secureRestClient = secureRestClient;
        om = new ObjectMapper();
        om.registerModule(new Jdk8Module())
            .registerModule(new JavaTimeModule());
    }

    @Before
    public void before() {
        startInstant = now();
    }

    @Given("^(.+) requests an edition on an existing testcase$")
    public void createTestCase(String user) {
        createGwtTestCase(user);
        requestEdition(user, context.getLastScenarioId());
    }

    @And("^(.+) requests an edition on the same testcase$")
    public void requestEditionOnCurrentTestCase(String user) {
        context.putTestCaseEdition(responseBodyAsEdition());
        requestEdition(user, context.getLastScenarioId());
    }

    @When("^(.+) consults the current editions of this testcase$")
    public void consultCurrentEditions(String user) {
        requestEditions(user, context.getLastScenarioId());
    }

    @Then("^(.+) are seen as current editors$")
    public void checkEditor(String users) {
        List<String> userList = Lists.list(users.split(","));
        List<TestCaseEditionDto> editions = responseBodyAsEditions();

        editions.forEach(e -> {
            assertThat(e.testCaseId()).isEqualTo(context.getLastScenarioId());
            assertThat(e.editionUser()).isIn(userList);
            assertThat(e.editionStartDate()).isAfter(startInstant);
        });
    }

    @Then("^the edition received is the first one$")
    public void checkEdition() {
        TestCaseEditionDto lastRequestedEdition = responseBodyAsEdition();
        assertThat(lastRequestedEdition).isEqualTo(context.getTestCaseEdition());
    }

    @When("^(.+) ends its edition$")
    public void endEdition(String user) {
        endEdition(user, context.getLastScenarioId());
    }

    @When("^edition lasts beyond defined ttl$")
    public void waitTTL() throws InterruptedException { // defined in application.yml with iceberg.editions.ttl values
        TimeUnit.MILLISECONDS.sleep(2500);
    }

    @And("^(.+) cannot be seen as current editor$")
    public void requestAndCheckEditor(String user) {
        requestEditions("admin", context.getLastScenarioId());
        List<TestCaseEditionDto> editions = responseBodyAsEditions();

        editions.forEach(e -> assertThat(e.editionUser()).isNotEqualTo(user));
    }

    private void endEdition(String user, String testCaseId) {
        secureRestClient.defaultRequestAs(user, user)
            .withUrl(TESTCASE_EDITION_URL + testCaseId)
            .delete();
    }

    private void requestEditions(String user, String testCaseId) {
        final ResponseEntity<String> response = secureRestClient.defaultRequestAs(user, user)
            .withUrl(TESTCASE_EDITION_URL + testCaseId)
            .get();

        context.putResponseBody(response.getBody());
    }

    private void requestEdition(String user, String testCaseId) {
        try {
            final ResponseEntity<String> response = secureRestClient.defaultRequestAs(user, user)
                .withUrl(TESTCASE_EDITION_URL + testCaseId)
                .withBody("")
                .post();

            context.putResponseBody(response.getBody());

            TestCaseEditionDto dto = responseBodyAsEdition();
            assert dto != null;
            assertThat(dto.testCaseId()).isEqualTo(testCaseId);
            assertThat(dto.editionUser()).isEqualTo(user);
            assertThat(dto.editionStartDate()).isAfter(startInstant);

        } catch (HttpClientErrorException hcee) {
            context.putStatus(hcee.getRawStatusCode());
            context.putResponseBody(hcee.getResponseBodyAsString());
        }
    }

    private void createGwtTestCase(String user) {
        GwtTestCaseDto testCase = ImmutableGwtTestCaseDto.builder()
            .title("titre")
            .scenario(ImmutableGwtScenarioDto.builder()
                .when(ImmutableGwtStepDto.builder().build())
                .build())
            .build();

        final ResponseEntity<String> response = secureRestClient.defaultRequestAs(user, user)
            .withUrl(GWT_SCENARIO_URL)
            .withBody(testCase)
            .post();

        context.putScenarioId(response.getBody());
    }

    private List<TestCaseEditionDto> responseBodyAsEditions() {
        try {
            return om.readValue(context.getResponseBody(), new TypeReference<List<TestCaseEditionDto>>() {
            });
        } catch (IOException e) {
            fail("Cannot read response body as editions list", e);
        }
        return emptyList();
    }

    private TestCaseEditionDto responseBodyAsEdition() {
        try {
            return om.readValue(context.getResponseBody(), TestCaseEditionDto.class);
        } catch (IOException e) {
            fail("Cannot read response body as edition", e);
        }
        return null;
    }
}
