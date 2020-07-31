package blackbox.stepdef.campaign;

import static blackbox.stepdef.ScenarioExecutionRequestStepDefs.ENV;
import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;
import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;

import blackbox.assertion.Assertions;
import blackbox.restclient.RestClient;
import blackbox.restclient.RestRequestBuilder;
import blackbox.stepdef.TestContext;
import com.jayway.jsonpath.JsonPath;
import cucumber.api.java.After;
import cucumber.api.java.Before;
import cucumber.api.java.en.And;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import com.chutneytesting.design.api.campaign.dto.CampaignDto;
import com.chutneytesting.design.api.scenario.v2_0.dto.ImmutableRawTestCaseDto;
import com.chutneytesting.execution.domain.report.ServerReportStatus;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;

public class CampaignExecutionStepDefs {
    private static final String CAMPAIGN_URL = "/api/ui/campaign/v1";
    private static final String RAW_EDITION_URL = "/api/scenario/v2/raw";
    private static final String SCENARIO_EDITION_URL = "/api/scenario/v2";
    private static final String CAMPAIGN_EXECUTION_ID_URL = "/api/ui/campaign/execution/v1/byID/";
    private static final String CAMPAIGN_EXECUTION_NAME_URL = "/api/ui/campaign/execution/v1/";

    private final RestClient secureRestClient;
    private final TestContext context;
    private final Map<String, String> nameScenarioWithId;
    private final List<String> usedScenarios;

    public CampaignExecutionStepDefs(RestClient secureRestClient, TestContext context) {
        this.secureRestClient = secureRestClient;
        this.context = context;
        nameScenarioWithId = new LinkedHashMap<>();
        usedScenarios = new ArrayList<>();
    }

    @Before
    public void setUp() {
        nameScenarioWithId.clear();
        usedScenarios.clear();
    }

    @After
    public void tearDown() {
        CampaignDto campaign = context.getCampaign();
        if (campaign != null) {
            secureRestClient.request()
                .withUrl(CAMPAIGN_URL + "/" + campaign.getId())
                .delete();
        }

        nameScenarioWithId.forEach((name, id) ->
            secureRestClient.request()
                .withUrl(SCENARIO_EDITION_URL + "/" + id)
                .delete()
        );
    }

    @Then("^the campaign is not found$")
    public void theCampaignIsNotFound() {
        assertThat(context.<Object>getExecutionReport())
            .isInstanceOfSatisfying(HttpClientErrorException.class, error ->
                assertThat(error.getRawStatusCode()).isEqualTo(404));
    }

    @Then("^the campaign report is empty$")
    public void theCampaignReportIsEmpty() {
        assertThat(context.<Object>getExecutionReport())
            .isInstanceOfSatisfying(String.class, errorCode ->
                assertThat(errorCode).isEqualTo("[]"));
    }

    @Given("^a scenario with name \"([^\"]*)\" is stored$")
    public void aScenarioWithNameIsStored(String scenarioName, String scenarioContent) {
        final ResponseEntity<String> responseEntity = secureRestClient.request()
            .withUrl(RAW_EDITION_URL)
            .withBody(ImmutableRawTestCaseDto.builder().content(scenarioContent).creationDate(Instant.now()).title(scenarioName).build())
            .post();
        String idScenario = responseEntity.getBody();
        nameScenarioWithId.put(scenarioName, idScenario);
    }

    @Given("^a campaign with name \"([^\"]*)\" is stored with the following scenarios :$")
    public void aCampaignStoredWithTheFollowingScenarios(String campaignName, List<String> scenarios) {
        usedScenarios.addAll(scenarios);
        List<String> scenarioIds = nameScenarioWithId.keySet().stream()
            .filter(scenarios::contains)
            .map(nameScenarioWithId::get)
            .collect(toList());
        storeCampaign(campaignName, scenarioIds);
    }

    @When("^this campaign is executed by name$")
    public void thisCampaignIsExecutedByName() {
        executeCampaign(context.getCampaign(), "byName");
    }

    @When("^this campaign is executed for surefire$")
    public void thisCampaignIsExecutedForSurefire() {
        executeCampaign(context.getCampaign(), "Surefire");
    }

    @When("^this campaign is executed by id")
    public void thisCampaignIsExecutedById() {
        executeCampaign(context.getCampaign());
    }

    @When("^an unknown campaign is executed by id")
    public void anUnknownCampaignIsExecutedById() throws Throwable {
        executeCampaign(
            buildCampaign("unknown", 0L, new ArrayList<>())
        );
    }

    @When("^an unknown campaign is executed by name$")
    public void anUnknownCampaignIsExecutedByName() throws Throwable {
        executeCampaign(
            buildCampaign("unknown", 0L, new ArrayList<>()), "byName"
        );
    }

    @When("^an unknown campaign with name \"([^\"]*)\" is executed$")
    public void anUnknownCampaignIsExecuted(String campaignName) throws Throwable {
        executeCampaign(
            buildCampaign(campaignName, 0L, new ArrayList<>()),
            "byName");
    }

    private void executeCampaign(CampaignDto campaign) {
        executeCampaign(campaign, "Byid");
    }

    private void executeCampaign(CampaignDto campaign, String typeExecution) {
        try {
            RestRequestBuilder requestBuilder = secureRestClient.request();
            if ("byName".equals(typeExecution)) {
                requestBuilder.withUrl(CAMPAIGN_EXECUTION_NAME_URL + campaign.getTitle());
            } else if ("Surefire".equals(typeExecution)) {
                requestBuilder.withUrl(CAMPAIGN_EXECUTION_NAME_URL + campaign.getTitle() + "/surefire");
            } else {
                requestBuilder.withUrl(CAMPAIGN_EXECUTION_ID_URL + campaign.getId());
            }
            ResponseEntity<String> stringResponseEntity = requestBuilder.get();
            context.putHEADERS(stringResponseEntity.getHeaders());
            context.putExecutionReport(stringResponseEntity.getBody());
            context.putStatus(stringResponseEntity.getStatusCode().value());
        } catch (HttpClientErrorException e) {
            context.putExecutionReport(e);
        }
    }

    @Then("^the execution report is returned$")
    public void theExecutionReportIsReturned() {
        assertJsonExecutionReport(context.getExecutionReport());
    }

    @Then("^the execution reports are returned$")
    @SuppressWarnings("unchecked")
    public void theExecutionReportsAreReturned() {
        Assertions.assertThatJson(context.getExecutionReport())
            .hasPathSatisfy("$[*]", jsonResult -> {
                JSONArray executionReports = (JSONArray) jsonResult;
                executionReports.parallelStream().forEach(o -> {
                    JSONObject report = new JSONObject((Map<String, ?>) o);
                    assertJsonExecutionReport(report.toJSONString());
                });
            });
    }

    @Then("^the execution is SUCCESS$")
    public void theExecutionIsOK() {
        assertThat(context.<Object>getStatus())
            .isInstanceOfSatisfying(Integer.class, statusCode ->
                assertThat(statusCode).isEqualTo(200));

        assertThat(context.<Object>getHEADERS())
            .isInstanceOfSatisfying(HttpHeaders.class, val ->
                assertThat(val.getContentType().toString()).isEqualTo("application/zip"));

        assertThat(context.<Object>getHEADERS())
            .isInstanceOfSatisfying(HttpHeaders.class, val ->
                assertThat(val.getContentLength()).isNotNull());
    }

    @And("^this execution report is stored in the campaign execution history$")
    public void thisExecutionReportIsStoredInTheCampaignExecutionHistory() {
        ResponseEntity<String> responseEntity = secureRestClient.defaultRequest()
            .withUrl(CAMPAIGN_URL + "/" + ((CampaignDto) context.getCampaign()).getId())
            .get();
        String campaignContent = responseEntity.getBody();
        Assertions.assertThatJson(campaignContent)
            .hasPathSatisfy("$.campaignExecutionReports[0].executionId", executionId -> assertThat(executionId).isInstanceOf(Number.class));
    }

    private void storeCampaign(String campaignName, List<String> scenarioIds) {
        CampaignDto campaign = buildCampaign(campaignName, 0L, scenarioIds);
        final ResponseEntity<String> responseEntity = secureRestClient.defaultRequest()
            .withUrl(CAMPAIGN_URL)
            .withBody(campaign)
            .post();

        Integer id = JsonPath.read(responseEntity.getBody(), "$.id");
        campaign = new CampaignDto(id.longValue(),
            campaign.getTitle(),
            campaign.getDescription(),
            scenarioIds,
            emptyMap(),
            emptyList(),
            "00:00",
            ENV, false, false, null);
        context.putCampaign(campaign);
    }

    private CampaignDto buildCampaign(String campaignName, Long campaignId, List<String> scenarioIds) {
        return new CampaignDto(campaignId,
            campaignName,
            "description 123",
            scenarioIds,
            emptyMap(),
            emptyList(),
            "00:00",
            ENV, false, false, null);
    }

    private void assertJsonExecutionReport(String executionReport) {
        Assertions.assertThatJson(executionReport)
            .hasPathSatisfy("$.executionId", executionId ->
                assertThat(executionId).isInstanceOf(Number.class))
            .hasPathEqualsTo("$.status", ServerReportStatus.SUCCESS.name())
            .hasPathSatisfy("$.scenarioExecutionReports[*].scenarioId", ids ->
                usedScenarios.stream()
                    .map(nameScenarioWithId::get)
                    .forEach(assertThat(ids).asList()::contains)
            );
    }

}
