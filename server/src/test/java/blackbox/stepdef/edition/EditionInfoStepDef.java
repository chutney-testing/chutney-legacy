package blackbox.stepdef.edition;

import static java.time.Instant.now;
import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;

import blackbox.restclient.RestClient;
import blackbox.stepdef.TestContext;
import com.chutneytesting.design.api.compose.dto.ComposableTestCaseDto;
import com.chutneytesting.design.api.compose.dto.ImmutableComposableScenarioDto;
import com.chutneytesting.design.api.compose.dto.ImmutableComposableTestCaseDto;
import com.chutneytesting.design.api.scenario.v2_0.dto.GwtTestCaseDto;
import com.chutneytesting.design.api.scenario.v2_0.dto.ImmutableGwtScenarioDto;
import com.chutneytesting.design.api.scenario.v2_0.dto.ImmutableGwtStepDto;
import com.chutneytesting.design.api.scenario.v2_0.dto.ImmutableGwtTestCaseDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import cucumber.api.java.Before;
import cucumber.api.java.en.And;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import java.io.IOException;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;

public class EditionInfoStepDef {

    private final TestContext context;
    private final RestClient secureRestClient;
    private final ObjectMapper om;
    private Instant startInstant;

    private static final String GWT_SCENARIO_URL = "/api/scenario/v2/";
    private static final String COMPOSABLE_SCENARIO_URL = "/api/scenario/component-edition/";

    public EditionInfoStepDef(TestContext context, RestClient secureRestClient) {
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

    @Given("^(.+) has created a testcase with metadata$")
    public void createTestCase(String user, List<Map<String, String>> metadataTable) {
        GwtTestCaseDto testCase = addMetadata(ImmutableGwtTestCaseDto.builder(), metadataTable.get(0)).build();
        createTestCase(user, GWT_SCENARIO_URL, testCase);
    }

    @Given("^(.+) has created a composable testcase with metadata$")
    public void createComposableTestCase(String user, List<Map<String, String>> metadataTable) {
        ComposableTestCaseDto testCase = addMetadata(ImmutableComposableTestCaseDto.builder(), metadataTable.get(0)).build();
        createTestCase(user, COMPOSABLE_SCENARIO_URL, testCase);
    }

    @And("^(.+) has updated it with metadata$")
    public void updateTestCase(String user, List<Map<String, String>> metadataTable) {
        String lastScenarioId = context.getLastScenarioId();

        if (isComposableTestCase(lastScenarioId)) {
            ImmutableComposableTestCaseDto.Builder testCaseBuilder =
                ImmutableComposableTestCaseDto.builder().id(lastScenarioId);

            ComposableTestCaseDto testCase = addMetadata(testCaseBuilder, metadataTable.get(0)).build();
            updateComposableTestCase(user, testCase);
        } else {
            ImmutableGwtTestCaseDto.Builder testCaseBuilder =
                ImmutableGwtTestCaseDto.builder().id(lastScenarioId);

            GwtTestCaseDto testCase = addMetadata(testCaseBuilder, metadataTable.get(0)).build();
            updateTestCase(user, testCase);
        }
    }

    @When("^(.+) consult it$")
    public void consultLastTestCase(String reader) throws IOException {
        getTestCase(reader, context.getLastScenarioId());
    }

    @When("^(.+) updates it with wrong version$")
    public void updateWrongversion(String user, List<Map<String, String>> metadataTable) {
        updateTestCase(user, metadataTable);
    }

    @Then("^the title is (.+)$")
    public void checkTitle(String expected) {
        String title;
        if (isComposableTestCase(context.getLastScenarioId())) {
            ComposableTestCaseDto testCase = context.getLastViewedScenario();
            title = testCase.title();
        } else {
            GwtTestCaseDto testCase = context.getLastViewedScenario();
            title = testCase.title();
        }
        assertThat(title).isEqualTo(expected);
    }

    @And("^the description is (.+)$")
    public void checkDescription(String expected) {
        Optional<String> description;
        if (isComposableTestCase(context.getLastScenarioId())) {
            ComposableTestCaseDto testCase = context.getLastViewedScenario();
            description = testCase.description();
        } else {
            GwtTestCaseDto testCase = context.getLastViewedScenario();
            description = testCase.description();
        }
        assertThat(description).hasValue(expected);
    }

    @And("^the tags is (.+)$")
    public void checkTags(String expected) {
        List<String> expectedTags = Arrays.stream(expected.split(",")).map(String::toUpperCase).collect(toList());
        List<String> tags;
        if (isComposableTestCase(context.getLastScenarioId())) {
            ComposableTestCaseDto testCase = context.getLastViewedScenario();
            tags = testCase.tags();
        } else {
            GwtTestCaseDto testCase = context.getLastViewedScenario();
            tags = testCase.tags();
        }
        assertThat(tags).containsExactlyElementsOf(expectedTags);
    }

    @And("^the creation date is (.+)$")
    public void checkCreationDate(String expected) {
        Instant expectedCreationDate = Instant.parse(expected);
        Instant creationDate;
        if (isComposableTestCase(context.getLastScenarioId())) {
            ComposableTestCaseDto testCase = context.getLastViewedScenario();
            creationDate = testCase.creationDate();
        } else {
            GwtTestCaseDto testCase = context.getLastViewedScenario();
            creationDate = testCase.creationDate().get();
        }
        assertThat(creationDate).isEqualTo(expectedCreationDate);
    }

    @And("^the author is (.+)")
    public void checkAuthor(String expected) {
        String author;
        if (isComposableTestCase(context.getLastScenarioId())) {
            ComposableTestCaseDto testCase = context.getLastViewedScenario();
            author = testCase.author();
        } else {
            GwtTestCaseDto testCase = context.getLastViewedScenario();
            author = testCase.author();
        }
        assertThat(author).isEqualTo(expected);
    }

    @And("^the update date is equal to creation date$")
    public void checkFirstUpdateDate() {
        Instant updateDate;
        Instant creationDate;
        if (isComposableTestCase(context.getLastScenarioId())) {
            ComposableTestCaseDto testCase = context.getLastViewedScenario();
            updateDate = testCase.updateDate();
            creationDate = testCase.creationDate();
        } else {
            GwtTestCaseDto testCase = context.getLastViewedScenario();
            updateDate = testCase.updateDate();
            creationDate = testCase.creationDate().get();
        }
        assertThat(updateDate).isEqualTo(creationDate);
    }

    @And("^the update date is set by the system$")
    public void checkUpdateDate() {
        Instant updateDate;
        if (isComposableTestCase(context.getLastScenarioId())) {
            ComposableTestCaseDto testCase = context.getLastViewedScenario();
            updateDate = testCase.updateDate();
        } else {
            GwtTestCaseDto testCase = context.getLastViewedScenario();
            updateDate = testCase.updateDate();
        }
        assertThat(updateDate).isAfter(startInstant);
    }

    @And("^the version is equal to (.+)$")
    public void checkVersion(Integer expected) {
        Integer version;
        if (isComposableTestCase(context.getLastScenarioId())) {
            ComposableTestCaseDto testCase = context.getLastViewedScenario();
            version = testCase.version();
        } else {
            GwtTestCaseDto testCase = context.getLastViewedScenario();
            version = testCase.version();
        }
        assertThat(version).isEqualTo(expected);
    }

    @Then("^request fails with error status (.+) and message contains (.+)$")
    public void checkHttpStatusAndMessage(Integer statusCode, String message) {
        assertThat((Integer) context.getStatus()).isEqualTo(statusCode);
        assertThat(context.getResponseBody()).contains(message);
    }

    private ImmutableComposableTestCaseDto.Builder addMetadata(ImmutableComposableTestCaseDto.Builder builder, Map<String, String> metadata) {
        return builder
            .title(metadata.get("title"))
            .description(metadata.get("description"))
            .tags(Arrays.asList(metadata.get("tags").split(",")))
            .author(metadata.get("author"))
            .creationDate(Instant.parse(metadata.get("creationDate")))
            .updateDate(Instant.parse(metadata.get("updateDate")))
            .version(Integer.valueOf(metadata.get("version")))
            .scenario(ImmutableComposableScenarioDto.builder().build());
    }

    private ImmutableGwtTestCaseDto.Builder addMetadata(ImmutableGwtTestCaseDto.Builder builder, Map<String, String> metadata) {
        return builder
            .title(metadata.get("title"))
            .description(metadata.get("description"))
            .tags(Arrays.asList(metadata.get("tags").split(",")))
            .author(metadata.get("author"))
            .creationDate(Instant.parse(metadata.get("creationDate")))
            .updateDate(Instant.parse(metadata.get("updateDate")))
            .version(Integer.valueOf(metadata.get("version")))
            .scenario(ImmutableGwtScenarioDto.builder()
                .when(ImmutableGwtStepDto.builder().build())
                .build());
    }

    private void getTestCase(String user, String testCaseId) throws IOException {
        boolean isComposableTestCase = isComposableTestCase(testCaseId);
        ResponseEntity<String> response = secureRestClient.defaultRequestAs(user, user)
            .withUrl((isComposableTestCase ? COMPOSABLE_SCENARIO_URL : GWT_SCENARIO_URL) + testCaseId)
            .get();

        Class<?> testCaseDtoClass = isComposableTestCase ? ComposableTestCaseDto.class : GwtTestCaseDto.class;
        context.putViewedScenario(om.readValue(response.getBody(), testCaseDtoClass));
    }

    private void createTestCase(String user, String url, Object testCase) {
        final ResponseEntity<String> response = secureRestClient.defaultRequestAs(user, user)
            .withUrl(url)
            .withBody(testCase)
            .post();

        context.putScenarioId(response.getBody());
    }

    private void updateTestCase(String user, GwtTestCaseDto testCase) {
        try {
            final String response = secureRestClient.defaultRequestAs(user, user)
                .withUrl(GWT_SCENARIO_URL)
                .withBody(testCase)
                .patch();

            assertThat(response).isEqualTo(context.getLastScenarioId());
        } catch (HttpClientErrorException hcee) {
            context.putStatus(hcee.getRawStatusCode());
            context.putResponseBody(hcee.getResponseBodyAsString());
        }
    }

    private void updateComposableTestCase(String user, ComposableTestCaseDto testCase) {
        try {
            final ResponseEntity<String> response = secureRestClient.defaultRequestAs(user, user)
                .withUrl(COMPOSABLE_SCENARIO_URL)
                .withBody(testCase)
                .post();

            assertThat(response.getBody()).isEqualTo(context.getLastScenarioId());
        } catch (HttpClientErrorException hcee) {
            context.putStatus(hcee.getRawStatusCode());
            context.putResponseBody(hcee.getResponseBodyAsString());
        }
    }

    private boolean isComposableTestCase(String lastScenarioId) {
        return lastScenarioId.contains("-");
    }
}
