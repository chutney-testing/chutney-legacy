package blackbox.stepdef.edition;

import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;
import static org.assertj.core.api.Assertions.assertThat;

import blackbox.restclient.RestClient;
import blackbox.stepdef.TestContext;
import com.chutneytesting.design.api.scenario.v2_0.dto.GwtScenarioDto;
import com.chutneytesting.design.api.scenario.v2_0.dto.GwtTestCaseDto;
import com.chutneytesting.design.api.scenario.v2_0.dto.ImmutableGwtTestCaseDto;
import com.chutneytesting.design.api.scenario.v2_0.dto.ImmutableRawTestCaseDto;
import com.chutneytesting.design.api.scenario.v2_0.dto.RawTestCaseDto;
import com.chutneytesting.design.domain.scenario.TestCase;
import com.chutneytesting.design.domain.scenario.TestCaseRepository;
import com.chutneytesting.design.domain.scenario.gwt.GwtTestCase;
import com.chutneytesting.design.infra.storage.scenario.jdbc.DatabaseTestCaseRepository;
import com.chutneytesting.design.infra.storage.scenario.jdbc.TestCaseData;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.google.common.io.Resources;
import cucumber.api.java.en.But;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.time.Instant;
import java.util.Optional;
import org.apache.commons.text.StringEscapeUtils;
import org.assertj.core.util.Files;
import org.hjson.JsonValue;
import org.springframework.http.ResponseEntity;

public class EditionStepDef {

    private final TestCaseRepository repository;
    private final DatabaseTestCaseRepository infraRepo;

    private final TestContext context;
    private final RestClient secureRestClient;
    private ObjectMapper objectMapper;

    private static final String RAW_SCENARIO_URL = "/api/scenario/v2/raw/";
    private static final String GWT_SCENARIO_URL = "/api/scenario/v2/";

    public EditionStepDef(TestCaseRepository repository, DatabaseTestCaseRepository infraRepo, TestContext context, RestClient secureRestClient) {
        this.repository = repository;
        this.infraRepo = infraRepo;
        this.context = context;
        this.secureRestClient = secureRestClient;
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new Jdk8Module())
            .registerModule(new JavaTimeModule());
    }

    @Given("^this scenario is saved")
    public void savedScenario(String scenarioJSonRequest) throws Throwable {
        String jsonContent = JsonValue.readHjson(scenarioJSonRequest).toString();
        jsonContent = context.replaceVariables(jsonContent);

        RawTestCaseDto rawTestCase = objectMapper.readValue(jsonContent, RawTestCaseDto.class);
        saveTestCase(RAW_SCENARIO_URL, rawTestCase);
    }

    @Given("global variables defined in (.*)")
    public void aGlobalVarWithMultilineValue(String globalVarName) {
        secureRestClient.defaultRequest()
            .withUrl("/api/ui/globalvar/v1/" + globalVarName)
            .withBody("{\"message\": \"" + StringEscapeUtils.escapeJson(fileContent("raw_global_vars/" + globalVarName + ".hjson")) + "\"}")
            .post();
    }

    @Given("^an existing (.*) written in format (.*)$")
    public void anExistingScenarioWrittenInFormatVersion(String scenarioFileName, String version) {
        String scenarioContent = getScenarioContent(scenarioFileName);

        TestCaseData testCaseData = TestCaseData.builder()
            .withContentVersion(version)
            .withId("")
            .withTitle("already existing scenario")
            .withCreationDate(Instant.now())
            .withDescription("")
            .withTags(emptyList())
            .withDataSet(emptyMap())
            .withRawScenario(scenarioContent)
            .build();

        String id = infraRepo.save(testCaseData);
        context.putScenarioId(id);
    }

    @Given("^an existing testcase (.*) written in old format$")
    public void anWholeExistingScenarioWrittenInFormatVersion(String scenarioFileName) throws IOException {
        TestCaseData testCaseData = objectMapper.readValue(getScenarioContent(scenarioFileName), TestCaseData.class);
        String id = infraRepo.save(testCaseData);
        context.putScenarioId(id);
    }

    @Given("^a testcase (.*) written with GWT form$")
    public void savingAWholeTestCaseWithAScenarioWrittenWithGWTForm(String testCaseFileName) {
        saveTestCase(GWT_SCENARIO_URL, getScenarioContent(testCaseFileName));
    }

    @Given("^saving a test case with a (.*) written with GWT form$")
    public void savingATestCaseWithAScenarioWrittenWithGWTForm(String scenarioFileName) throws IOException {
        String serializeScenario = getScenarioContent(scenarioFileName);
        GwtScenarioDto gwtTestCaseDto = objectMapper.readValue(serializeScenario, GwtScenarioDto.class);

        GwtTestCaseDto gwtTestCase = ImmutableGwtTestCaseDto.builder()
            .title("Saved scenario")
            .description("contains a gwt scenario")
            .scenario(gwtTestCaseDto)
            .build();

        saveTestCase(GWT_SCENARIO_URL, gwtTestCase);
    }

    @When("^saving a test case with a raw (.*) written in an old format$")
    public void savingARawTestCaseWrittenInAnOldFormatScenario(String scenarioFileName) {
        ImmutableRawTestCaseDto rawTestCase = ImmutableRawTestCaseDto.builder()
            .title("Saved scenario")
            .description("contains a raw scenario in old format")
            .content(getScenarioContent(scenarioFileName))
            .build();

        saveTestCase(RAW_SCENARIO_URL, rawTestCase);
    }

    @When("^it is retrieved$")
    public void itIsRetrieved() {
        context.putViewedScenario(getLastAsGwt());
    }

    @When("^it is retrieved as raw$")
    public void itIsRetrievedAsRaw() {
        context.putViewedScenario(getLastAsRaw());
    }

    @Then("^the persisted test case is converted to the last format$")
    public void thePersistedTestCaseIsConvertedToTheLastFormat() {
        TestCase testCase = repository.findById(context.getLastScenarioId());
        assertThat(testCase).isInstanceOf(GwtTestCase.class);

        Optional<TestCaseData> tc = infraRepo.findById(context.getLastScenarioId());
        assertThat(tc).isNotEmpty();
        assertThat(tc.get().contentVersion).isEqualToIgnoringCase("v2.1");
    }

    @Then("^it is viewed in the new format$")
    public void itIsViewedInTheNewFormat() throws IOException {
        String lastViewedScenario = context.getLastViewedScenario();
        GwtTestCaseDto gwtTestCaseDto = objectMapper.readValue(lastViewedScenario, GwtTestCaseDto.class);
        assertThat(gwtTestCaseDto).isInstanceOf(GwtTestCaseDto.class);
    }

    @But("^still persisted in its original format (.*)$")
    public void stillPersistedInTheOldFormat(String version) {
        Optional<TestCaseData> tc = infraRepo.findById(context.getLastScenarioId());
        assertThat(tc).isNotEmpty();
        assertThat(tc.get().contentVersion).isEqualToIgnoringCase(version);
    }

    @Then("^it is viewed as raw$")
    public void itIsViewedAsRaw() throws IOException {
        String lastViewedScenario = context.getLastViewedScenario();
        RawTestCaseDto gwtTestCaseDto = objectMapper.readValue(lastViewedScenario, RawTestCaseDto.class);
        assertThat(gwtTestCaseDto).isInstanceOf(RawTestCaseDto.class);
    }

    @Then("^the task implementation is HJSON readable")
    public void theTaskImplementationIsHjsonReadable() throws IOException {
        String expectedHJSONTask = "{type:fake_typetarget:FAKE_TARGETinputs:{fake_param:fake_value}}";

        // GET GWT
        GwtTestCaseDto gwtTestCaseDto = objectMapper.readValue(getLastAsGwt(), GwtTestCaseDto.class);
        assertThat(gwtTestCaseDto.scenario().givens().get(0).implementation().get().task().replaceAll("\\s+", "")).as("from GWT")
            .isEqualToIgnoringWhitespace(expectedHJSONTask);

        // GET RAW
        RawTestCaseDto rawTestCaseDto = objectMapper.readValue(getLastAsRaw(), RawTestCaseDto.class);
        assertThat(rawTestCaseDto.content().replaceAll("\\s+", "")).as("from RAW")
            .contains(expectedHJSONTask);

    }

    private String getLastAsGwt() {
        ResponseEntity<String> gwt_response = secureRestClient.defaultRequest()
            .withUrl(GWT_SCENARIO_URL + context.getLastScenarioId())
            .get();

        return gwt_response.getBody();
    }

    private String getLastAsRaw() {
        ResponseEntity<String> gwt_response = secureRestClient.defaultRequest()
            .withUrl(RAW_SCENARIO_URL + context.getLastScenarioId())
            .get();

        return gwt_response.getBody();
    }

    private String getScenarioContent(String fileName) {
        return fileContent("raw_scenarios/" + fileName);
    }

    private String fileContent(String resourcePath) {
        return Files.contentOf(new File(Resources.getResource(resourcePath).getPath()), Charset.forName("UTF-8"));
    }

    private void saveTestCase(String endpoint, Object testCase) {
        final ResponseEntity<String> response = secureRestClient.defaultRequest()
            .withUrl(endpoint)
            .withBody(testCase)
            .post(String.class);

        context.putScenarioId(response.getBody());
    }

}
