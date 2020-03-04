package blackbox.stepdef;

import blackbox.restclient.RestClient;
import com.google.common.io.Resources;
import cucumber.api.java.After;
import cucumber.api.java.Before;
import cucumber.api.java.en.Given;
import java.io.File;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import org.assertj.core.util.Files;
import org.springframework.http.ResponseEntity;

public class ComponentStepDefs {

    private static final String STEP_BASE_URL = "/api/steps/v1";
    private static final String COMPONENT_BASE_URL = "/api/scenario/component-edition";

    private static final Map<String, String> componentsIds = new HashMap<>();
    private static final Map<String, String> testCaseTasksId = new HashMap<>();

    private final TestContext context;
    private final RestClient secureRestClient;

    public ComponentStepDefs(TestContext context, RestClient secureRestClient) {
        this.context = context;
        this.secureRestClient = secureRestClient;
    }

    @Before
    public void setUp() {
        testCaseTasksId.clear();
        componentsIds.clear();
    }

    @After
    public void tearDown() {
        testCaseTasksId.forEach((name, id) ->
            secureRestClient.defaultRequest()
                .withUrl(COMPONENT_BASE_URL + "/" + id)
                .delete()
        );

        componentsIds.forEach((name, id) ->
            secureRestClient.defaultRequest()
                .withUrl(STEP_BASE_URL + "/" + id)
                .delete()
        );
    }

    @Given("composable task components")
    public void saveComponentTasks(List<String> componentNamesList) {
        componentNamesList.forEach(componentName -> {
            final ResponseEntity<String> componentId = secureRestClient.defaultRequest()
                .withUrl(STEP_BASE_URL)
                .withBody(fileContent("raw_steps/" + componentName + ".json"))
                .post(String.class);
            ComponentStepDefs.componentsIds.put(componentName, componentId.getBody());
        });
    }

    @Given("a composable testcase (.*)")
    public void saveComponentTestCase(String testCaseName) {
        final ResponseEntity<String> testCaseId = secureRestClient.defaultRequest()
            .withUrl(COMPONENT_BASE_URL)
            .withBody(setComponentIds(fileContent("raw_testcases/" + testCaseName + ".json")))
            .post(String.class);

        testCaseTasksId.put(testCaseName, testCaseId.getBody());
        context.putScenarioId(testCaseId.getBody());
    }

    private String setComponentIds(String componentJson) {
        AtomicReference<String> componentJsonWithIds = new AtomicReference<>(componentJson);
        componentsIds.forEach((name, id) ->
            componentJsonWithIds.set(componentJsonWithIds.get().replaceAll("%%" + name + "%%", id))
        );
        return componentJsonWithIds.get();
    }

    private String fileContent(String resourcePath) {
        return Files.contentOf(new File(Resources.getResource(resourcePath).getPath()), Charset.forName("UTF-8"));
    }
}
