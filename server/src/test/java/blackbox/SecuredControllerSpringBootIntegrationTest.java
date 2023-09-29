package blackbox;

import static java.util.Optional.ofNullable;
import static org.springframework.http.HttpMethod.DELETE;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpMethod.PATCH;
import static org.springframework.http.HttpMethod.POST;
import static org.springframework.http.HttpMethod.PUT;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.request;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.chutneytesting.ServerConfiguration;
import com.chutneytesting.security.api.UserDto;
import com.chutneytesting.tools.file.FileUtils;
import java.io.File;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

@SpringBootTest(classes = {ServerConfiguration.class})
@TestPropertySource(properties = "spring.datasource.url=jdbc:h2:mem:testdbsecu")
@TestPropertySource(properties = "spring.config.location=classpath:blackbox/")
public class SecuredControllerSpringBootIntegrationTest {

    @Autowired
    private WebApplicationContext context;

    private MockMvc mvc;

    @BeforeAll
    public static void cleanUp() {
        FileUtils.deleteFolder(new File("./target/.chutney").toPath());
    }

    @BeforeEach
    public void setup() {

        mvc = MockMvcBuilders
            .webAppContextSetup(context)
            .apply(springSecurity())
            .build();
    }

    private static Object[] securedEndPointList() {
        return new Object[][]{
            {GET, "/api/v1/backups", "ADMIN_ACCESS", null, OK},
            {POST, "/api/v1/backups", "ADMIN_ACCESS", "{\"backupables\": [ \"environments\" ]}", OK},
            {GET, "/api/v1/backups/backupId", "ADMIN_ACCESS", null, NOT_FOUND},
            {DELETE, "/api/v1/backups/backupId", "ADMIN_ACCESS", null, NOT_FOUND},
            {GET, "/api/v1/backups/id/download", "ADMIN_ACCESS", null, OK},
            {GET, "/api/v1/backups/backupables", "ADMIN_ACCESS", null, OK},

            {POST, "/api/v1/admin/database/execute/jdbc", "ADMIN_ACCESS", "select 1", OK},
            {POST, "/api/v1/admin/database/paginate/jdbc", "ADMIN_ACCESS", "{\"pageNumber\":1,\"elementPerPage\":1,\"wrappedRequest\":\"\"}", OK},
            {POST, "/api/v1/agentnetwork/configuration", "ADMIN_ACCESS", "{}", OK},
            {GET, "/api/v1/description", "ADMIN_ACCESS", null, OK},
            {POST, "/api/v1/agentnetwork/explore", "ADMIN_ACCESS", "{\"creationDate\":\"1235\"}", OK},
            {POST, "/api/ui/campaign/v1", "CAMPAIGN_WRITE", "{\"title\":\"secu\",\"description\":\"desc\",\"scenarioIds\":[],\"tags\":[]}", OK},
            {PUT, "/api/ui/campaign/v1", "CAMPAIGN_WRITE", "{\"title\":\"secu\",\"description\":\"desc\",\"scenarioIds\":[],\"tags\":[]}", OK},
            {DELETE, "/api/ui/campaign/v1/666", "CAMPAIGN_WRITE", null, OK},
            {GET, "/api/ui/campaign/v1/666", "CAMPAIGN_READ", null, NOT_FOUND},
            {GET, "/api/ui/campaign/v1/666/scenarios", "CAMPAIGN_READ", null, NOT_FOUND},
            {GET, "/api/ui/campaign/v1", "CAMPAIGN_READ", null, OK},
            {GET, "/api/ui/campaign/v1/lastexecutions/20", "CAMPAIGN_READ", null, OK},
            {GET, "/api/ui/campaign/v1/scenario/scenarioId", "SCENARIO_READ", null, OK},
            {GET, "/api/ui/campaign/v1/scheduling", "CAMPAIGN_READ", null, OK},
            {POST, "/api/ui/campaign/v1/scheduling", "CAMPAIGN_WRITE", "{}", OK},
            {DELETE, "/api/ui/campaign/v1/scheduling/666", "CAMPAIGN_WRITE", null, OK},


            {GET, "/api/v1/editions/testcases/testcaseId", "SCENARIO_READ", null, OK},
            {POST, "/api/v1/editions/testcases/testcaseId", "SCENARIO_WRITE", "{}", NOT_FOUND},
            {DELETE, "/api/v1/editions/testcases/testcaseId", "SCENARIO_WRITE", null, OK},
            {GET, "/api/ui/globalvar/v1", "GLOBAL_VAR_READ", null, OK},
            {POST, "/api/ui/globalvar/v1/secupost", "GLOBAL_VAR_WRITE", "{\"message\":\"{}\"}", OK},
            {DELETE, "/api/ui/globalvar/v1/secudelete", "GLOBAL_VAR_WRITE", null, NOT_FOUND},
            {GET, "/api/ui/globalvar/v1/secuget", "GLOBAL_VAR_READ", null, NOT_FOUND},
            {GET, "/api/ui/jira/v1/scenario", "SCENARIO_READ", null, OK},
            {GET, "/api/ui/jira/v1/scenario", "CAMPAIGN_WRITE", null, OK},
            {GET, "/api/ui/jira/v1/campaign", "CAMPAIGN_READ", null, OK},
            {GET, "/api/ui/jira/v1/scenario/scenarioId", "SCENARIO_WRITE", null, OK},
            {POST, "/api/ui/jira/v1/scenario", "SCENARIO_WRITE", "{\"id\":\"\",\"chutneyId\":\"\"}", OK},
            {DELETE, "/api/ui/jira/v1/scenario/scenarioId", "SCENARIO_WRITE", null, OK},
            {GET, "/api/ui/jira/v1/campaign/campaignId", "CAMPAIGN_READ", null, OK},
            // {GET, "/api/ui/jira/v1/testexec/testExecId", "CAMPAIGN_WRITE", null, OK}, need a valid jira url
            {PUT, "/api/ui/jira/v1/testexec/testExecId", "CAMPAIGN_WRITE", "{\"id\":\"\",\"chutneyId\":\"\"}", OK},
            {POST, "/api/ui/jira/v1/campaign", "CAMPAIGN_WRITE", "{\"id\":\"\",\"chutneyId\":\"\"}", OK},
            {DELETE, "/api/ui/jira/v1/campaign/campaignId", "CAMPAIGN_WRITE", null, OK},
            {GET, "/api/ui/jira/v1/configuration", "ADMIN_ACCESS", null, OK},
            {GET, "/api/ui/jira/v1/configuration/url", "SCENARIO_READ", null, OK},
            {GET, "/api/ui/jira/v1/configuration/url", "CAMPAIGN_READ", null, OK},
            {POST, "/api/ui/jira/v1/configuration", "ADMIN_ACCESS", "{\"url\":\"\",\"username\":\"\",\"password\":\"\"}", OK},
            {POST, "/api/v1/ui/plugins/linkifier/", "ADMIN_ACCESS", "{\"pattern\":\"\",\"link\":\"\",\"id\":\"\"}", OK},
            {DELETE, "/api/v1/ui/plugins/linkifier/id", "ADMIN_ACCESS", null, OK},

            {GET, "/api/scenario/v2/1", "SCENARIO_READ", null, NOT_FOUND},
            {GET, "/api/scenario/v2/testCaseId/metadata", "SCENARIO_READ", null, NOT_FOUND},
            {GET, "/api/scenario/v2", "SCENARIO_READ", null, OK},
            {GET, "/api/scenario/v2", "CAMPAIGN_WRITE", null, OK},
            {POST, "/api/scenario/v2", "SCENARIO_WRITE", "{\"title\":\"\",\"scenario\":{\"when\":{}}}", OK},
            {PATCH, "/api/scenario/v2", "SCENARIO_WRITE", "{\"title\":\"\",\"scenario\":{\"when\":{}}}", OK},
            {DELETE, "/api/scenario/v2/testCaseId", "SCENARIO_WRITE", null, OK},
            {POST, "/api/scenario/v2/raw", "SCENARIO_WRITE", "{\"title\":\"\",\"content\":\"{\\\"when\\\":{}}\"}", OK},
            {GET, "/api/scenario/v2/raw/1", "SCENARIO_READ", null, OK},
            {GET, "/api/ui/campaign/execution/v1/campaignName", "CAMPAIGN_EXECUTE", null, OK},
            {GET, "/api/ui/campaign/execution/v1/campaignName/env", "CAMPAIGN_EXECUTE", null, OK},
            {POST, "/api/ui/campaign/execution/v1/replay/666", "CAMPAIGN_EXECUTE", "{}", NOT_FOUND},
            {GET, "/api/ui/campaign/execution/v1/campaignPattern/surefire", "CAMPAIGN_EXECUTE", null, OK},
            {GET, "/api/ui/campaign/execution/v1/campaignPattern/surefire/env", "CAMPAIGN_EXECUTE", null, OK},
            {POST, "/api/ui/campaign/execution/v1/666/stop", "CAMPAIGN_EXECUTE", "{}", NOT_FOUND},
            {GET, "/api/ui/campaign/execution/v1/byID/666", "CAMPAIGN_EXECUTE", null, NOT_FOUND},
            {GET, "/api/ui/campaign/execution/v1/byID/666/env", "CAMPAIGN_EXECUTE", null, NOT_FOUND},
            {GET, "/api/ui/scenario/123/execution/v1", "SCENARIO_READ", null, OK},
            {GET, "/api/ui/scenario/123/execution/666/v1", "SCENARIO_READ", null, NOT_FOUND},
            {GET, "/api/ui/scenario/execution/666/summary/v1", "SCENARIO_READ", null, NOT_FOUND},
            {POST, "/api/ui/scenario/execution/v1/scenarioId/env", "SCENARIO_EXECUTE", null, NOT_FOUND},
            {POST, "/api/idea/scenario/execution/env", "SCENARIO_EXECUTE", "{\"content\":\"{\\\"when\\\":{}}\",\"params\":{}} ", OK},
            {POST, "/api/ui/scenario/executionasync/v1/scenarioId/env", "SCENARIO_EXECUTE", "[]", NOT_FOUND},
            {GET, "/api/ui/scenario/executionasync/v1/scenarioId/execution/666", "SCENARIO_READ", null, NOT_FOUND},
            {POST, "/api/ui/scenario/executionasync/v1/scenarioId/execution/666/stop", "SCENARIO_EXECUTE", null, NOT_FOUND},
            {POST, "/api/ui/scenario/executionasync/v1/scenarioId/execution/666/pause", "SCENARIO_EXECUTE", null, NOT_FOUND},
            {POST, "/api/ui/scenario/executionasync/v1/scenarioId/execution/666/resume", "SCENARIO_EXECUTE", null, NOT_FOUND},
            {POST, "/api/v1/authorizations", "ADMIN_ACCESS", "{}", OK},
            {GET, "/api/v1/authorizations", "ADMIN_ACCESS", null, OK},
            {POST, "/api/scenario/execution/v1", "SCENARIO_EXECUTE", "{\"scenario\":{}}", OK},
            {GET, "/api/action/v1", "SCENARIO_READ", null, OK},
            {GET, "/api/action/v1/actionId", "SCENARIO_READ", null, NOT_FOUND},
            {GET, "/api/v2/environments", "ENVIRONMENT_ACCESS", null, OK},
            {GET, "/api/v2/environments/names", "SCENARIO_EXECUTE", null, OK},
            {GET, "/api/v2/environments/names", "CAMPAIGN_WRITE", null, OK},
            {GET, "/api/v2/environments/names", "CAMPAIGN_EXECUTE", null, OK},
            {POST, "/api/v2/environments", "ENVIRONMENT_ACCESS", "{\"name\": \"secuenv\"} ", OK},
            {DELETE, "/api/v2/environments/envName", "ENVIRONMENT_ACCESS", null, NOT_FOUND},
            {PUT, "/api/v2/environments/envName", "ENVIRONMENT_ACCESS", "{}", NOT_FOUND},
            {GET, "/api/v2/environments/envName/target", "ENVIRONMENT_ACCESS", null, NOT_FOUND},
            {GET, "/api/v2/environments/targets", "ENVIRONMENT_ACCESS", null, NOT_FOUND},
            {GET, "/api/v2/environments/envName", "ENVIRONMENT_ACCESS", null, NOT_FOUND},
            {GET, "/api/v2/environments/envName/targets/targetName", "ENVIRONMENT_ACCESS", null, NOT_FOUND},
            {POST, "/api/v2/targets", "ENVIRONMENT_ACCESS", "{\"name\":\"targetName\",\"url\":\"http://localhost\", \"environment\":\"secuenv\"}", OK},
            {PUT, "/api/v2/targets/targetName", "ENVIRONMENT_ACCESS", "{\"name\":\"targetName\",\"url\":\"https://localhost\", \"environment\":\"secuenv\"}", OK},
            {DELETE, "/api/v2/environments/envName/targets/targetName", "ENVIRONMENT_ACCESS", null, NOT_FOUND},
            // Must be at the end because the network configuration is in wrong staten, why ??
            {POST, "/api/v1/agentnetwork/wrapup", "ADMIN_ACCESS", "{\"agentsGraph\":{\"agents\":[]},\"networkConfiguration\":{\"creationDate\":\"2021-09-06T10:08:36.569227Z\",\"agentNetworkConfiguration\":[],\"environmentsConfiguration\":[]}}", OK},
            {GET, "/api/v2/features", null, null, OK},
        };
    }

    private static Object[] unsecuredEndPointList() {
        return new Object[][]{
            {GET, "/api/v1/user", null, null, OK},
            {POST, "/api/v1/user", null, "{}", OK},
            {GET, "/api/v1/ui/plugins/linkifier/", null, null, OK},
            {GET, "/api/v1/info/build/version", null, null, OK},
            {GET, "/api/v1/info/appname", null, null, OK},
            {GET, "/actuator", null, null, OK},
            {GET, "/actuator/beans", null, null, OK},
            {GET, "/actuator/caches", null, null, OK},
            {GET, "/actuator/health", null, null, OK},
            {GET, "/actuator/info", null, null, OK},
            {GET, "/actuator/conditions", null, null, OK},
            {GET, "/actuator/configprops", null, null, OK},
            {GET, "/actuator/env", null, null, OK},
            {GET, "/actuator/liquibase", null, null, OK},
            {GET, "/actuator/loggers", null, null, OK},
            //{HttpMethod.GET, "/actuator/heapdump",  null, null, OK}, in comment because it takes 2s
            {GET, "/actuator/threaddump", null, null, OK},
            {GET, "/actuator/prometheus", null, null, NOT_FOUND},
            {GET, "/actuator/metrics", null, null, OK},
            {GET, "/actuator/scheduledtasks", null, null, OK},
            {GET, "/actuator/mappings", null, null, OK},
        };
    }

    @ParameterizedTest
    @MethodSource({"securedEndPointList", "unsecuredEndPointList"})
    public void secured_api_access_verification(HttpMethod httpMethod, String url, String authority, String content, HttpStatus status) throws Exception {
        UserDto user = new UserDto();
        user.setName("userName");
        ofNullable(authority).ifPresent(user::grantAuthority);
        MockHttpServletRequestBuilder request = request(httpMethod, url)
            .secure(true)
            .with(user(user))
            .contentType(MediaType.APPLICATION_JSON);
        if (content != null) {
            request.content(content);
        }
        mvc.perform(request)
            .andExpect(status().is(status.value()));
    }

    @ParameterizedTest
    @MethodSource({"uploadEndpoints"})
    public void secured_upload_api_access_verification(String url, String authority, String content, HttpStatus expectedStatus) throws Exception {
        UserDto user = new UserDto();
        user.setName("userName");
        ofNullable(authority).ifPresent(user::grantAuthority);
        MockHttpServletRequestBuilder request = MockMvcRequestBuilders
            .multipart(url)
            .file(new MockMultipartFile("file", "myFile.json", "text/json", content.getBytes()))
            .contentType(MediaType.MULTIPART_FORM_DATA)
            .secure(true)
            .with(user(user));
        mvc.perform(request)
            .andExpect(status().is(expectedStatus.value()));
    }

    private static Stream<Arguments> uploadEndpoints() {
        return Stream.of(
            Arguments.of("/api/v2/environments", "ENVIRONMENT_ACCESS", "{\"name\": \"env\"}", OK),
            Arguments.of("/api/v2/environments/env/targets", "ENVIRONMENT_ACCESS", "{\"name\":\"targetName\",\"url\":\"tcp://localhost\", \"environment\":\"env\"}", OK)
        );
    }
}
