package blackbox.stepdef;

import static org.springframework.util.SocketUtils.findAvailableTcpPort;

import blackbox.restclient.RestClient;
import com.chutneytesting.design.api.environment.dto.EnvironmentMetadataDto;
import com.chutneytesting.design.api.environment.dto.TargetMetadataDto;
import com.chutneytesting.design.domain.environment.Environment;
import com.chutneytesting.design.domain.environment.SecurityInfo;
import com.chutneytesting.design.domain.environment.Target;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.google.common.io.Resources;
import io.cucumber.datatable.DataTable;
import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.cucumber.java.en.Given;
import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;

public class TargetStepDefs {

    private static final Logger LOGGER = LoggerFactory.getLogger(TargetStepDefs.class);

    private final RestClient restClient;
    private final TestContext context;

    private Map<String, ResponseEntity<String>> savedTargetNames = new HashMap<>();

    public TargetStepDefs(RestClient restClient, TestContext context) {
        this.restClient = restClient;
        this.context = context;
    }

    @Before
    public void setUp() {
        final ResponseEntity<String> test = restClient.defaultRequest()
            .withUrl("/api/v2/environment/")
            .get();
        if ("[]".equals(test.getBody())) {
            restClient.defaultRequest()
                .withUrl("/api/v2/environment/")
                .withBody(EnvironmentMetadataDto.from(Environment.builder().withName("GLOBAL").build()))
                .post();
        }
    }

    @After
    public void after() {
        savedTargetNames.keySet()
            .forEach(targetName -> {
                if (savedTargetNames.get(targetName) != null && savedTargetNames.get(targetName).getStatusCode().is2xxSuccessful()) {
                    restClient.defaultRequest()
                        .withUrl("/api/v2/environment/GLOBAL/target/" + targetName)
                        .delete();
                }
            });
    }

    @Given("^existing truststore (.+)$")
    public void existing_trustore(String trustore) {
        String absolutePath = Resources.getResource(trustore).getPath();
        context.putTrustStorePath(absolutePath);
    }

    @Given("^an existing target (.+) with url (.*)$")
    public void an_existing_target_on_local_server(String targetName, String targetUrl) {
        Target target = Target.builder()
            .withId(Target.TargetId.of(targetName, "GLOBAL"))
            .withUrl(targetUrl)
            .build();
        enrichGlobalEnvironment(target);
    }

    @Given("^a target (.+) with url (.+) with security$")
    public void a_target_with_url_with_security(String targetName, String targetUrl, DataTable security) {
        String username = (String) security.asMap(String.class, String.class).get("username");
        String pwd = (String) security.asMap(String.class, String.class).get("password");
        String keyStorePath = (String) security.asMap(String.class, String.class).get("keyStorePath");
        String keyStorePassword = (String) security.asMap(String.class, String.class).get("keyStorePassword");

        String absolutePath = null;
        if (keyStorePath != null) {
            absolutePath = new File(Resources.getResource(keyStorePath).getPath()).toString();
        }

        SecurityInfo.Credential credential = null;
        if (username != null && pwd != null) {
            credential = SecurityInfo.Credential.of(username, pwd);
        }

        SecurityInfo secu = SecurityInfo.builder()
            .keyStore(absolutePath)
            .keyStorePassword(keyStorePassword)
            .credential(credential)
            .build();

        Target target = Target.builder()
            .withId(Target.TargetId.of(targetName, "GLOBAL"))
            .withUrl(targetUrl)
            .withSecurity(secu)
            .build();

        enrichGlobalEnvironment(target);
    }

    @Given("^a dyanmic target (.+) with url (.+) with security$")
    public void a_target_with_dynamic_port_with_url_with_security(String targetName, String targetUrl, DataTable security) {
        String targetUrlFull = addDynamicPort(targetName, targetUrl);
        a_target_with_url_with_security(targetName, targetUrlFull, security);
    }

    @Given("^a target (.+) with url (.+) with properties$")
    public void a_target_with_url_with_properties(String targetName, String targetUrl, DataTable properties) {
        Map<String, String> dt = new HashMap<>(properties.asMap(String.class, String.class));
        List<String> emptyKey = dt.entrySet().stream()
            .filter(e -> "<empty>".equals(e.getValue()))
            .map(Map.Entry::getKey)
            .collect(Collectors.toList());
        emptyKey.forEach(k -> dt.put(k, ""));

        Target target = Target.builder()
            .withId(Target.TargetId.of(targetName, "GLOBAL"))
            .withUrl(targetUrl)
            .withProperties(dt)
            .build();

        enrichGlobalEnvironment(target);
    }

    @Given("^a dynamic target (.+) with url (.+) with properties$")
    public void a_target_with_dynamic_port_with_url_with_properties(String targetName, String targetUrl, DataTable properties) {
        String targetUrlFull = addDynamicPort(targetName, targetUrl);
        a_target_with_url_with_properties(targetName, targetUrlFull, properties);
    }

    @Given("^an existing target (.+) on local server$")
    public void an_existing_target_on_local_server(String targetName) {
        Integer port = Integer.valueOf(System.getProperty("port"));
        an_existing_target_on_local_server(targetName, "http://localhost:" + port);
    }

    @Given("^an existing target (.+) on local server with security$")
    public void an_existing_target_on_local_server(String targetName, DataTable security) {
        Integer port = Integer.valueOf(System.getProperty("port"));
        a_target_with_url_with_security(targetName, "http://localhost:" + port, security);
    }

    @Given("^an existing target (.+) having url in system property (.+)$")
    public void an_existing_target_with_url_from_system_property(String targetName, String urlSystemProperty) {
        an_existing_target_on_local_server(targetName, "tcp://" + System.getProperty(urlSystemProperty));
    }

    @Given("^a configured target (.+) for an app$")
    public void an_existing_target_as_mock_server(String targetName) {
        WireMockServer wireMockServer = context.getMockServer();
        an_existing_target_on_local_server(targetName, wireMockServer.baseUrl());
    }

    private void enrichGlobalEnvironment(Target target) {
        try {
            final ResponseEntity<String> responseEntity = restClient.defaultRequest()
                .withUrl("/api/v2/environment/GLOBAL/target")
                .withBody(TargetMetadataDto.from(target))
                .post(String.class);

            LOGGER.info("New target saved : " + target);
            savedTargetNames.put(target.name, responseEntity);
        } catch (HttpClientErrorException | HttpServerErrorException e) {
            throw new IllegalArgumentException("Unable to save target [" + target.name + "]");
        }
    }

    private String addDynamicPort(String targetName, String targetUrl) {
        int port = findAvailableTcpPort();
        String targetPort = String.valueOf(port);
        context.addScenarioVariables(targetName + "_port", targetPort);
        return targetUrl + ":" + targetPort;
    }
}
