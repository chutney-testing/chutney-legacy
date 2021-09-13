package com.chutneytesting;

import static org.hamcrest.core.AnyOf.anyOf;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.request;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.chutneytesting.design.infra.storage.scenario.git.GitClient;
import com.chutneytesting.security.api.UserDto;
import com.chutneytesting.tools.file.FileUtils;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Answers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

@SpringBootTest
@TestPropertySource(properties = "spring.datasource.url=jdbc:h2:mem:testdbsecu")
public class SecuredControllerSpringBootIntegrationTest {

    @Autowired
    private WebApplicationContext context;

    @MockBean(answer = Answers.RETURNS_DEEP_STUBS)
    private GitClient gitClient;

    private MockMvc mvc;

    @BeforeAll
    public static void cleanUp() {
        FileUtils.deleteFolder(Path.of("./target/.chutney"));
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
            {HttpMethod.GET, "/api/v1/backups/git", "ADMIN_ACCESS", null},
            {HttpMethod.POST, "/api/v1/backups/git", "ADMIN_ACCESS", "{\"name\":\"secuback\",\"url\":\"\",\"branch\":\"\",\"privateKeyPath\":\"\",\"privateKeyPassphrase\":\"\"}"},
            {HttpMethod.DELETE, "/api/v1/backups/git/name", "ADMIN_ACCESS", null},
            {HttpMethod.GET, "/api/v1/backups/git/name/backup", "ADMIN_ACCESS", null},
            {HttpMethod.GET, "/api/v1/backups/git/name/import", "ADMIN_ACCESS", null},
            {HttpMethod.GET, "/api/v1/backups", "ADMIN_ACCESS", null},
            {HttpMethod.POST, "/api/v1/backups", "ADMIN_ACCESS", "{\"homePage\":true}"},
            {HttpMethod.GET, "/api/v1/backups/backupId", "ADMIN_ACCESS", null},
            {HttpMethod.DELETE, "/api/v1/backups/backupId", "ADMIN_ACCESS", null},
            {HttpMethod.GET, "/api/v1/backups/id/download", "ADMIN_ACCESS", null},
            {HttpMethod.POST, "/api/v1/admin/database/execute/orient", "ADMIN_ACCESS", "select 1"},
            {HttpMethod.POST, "/api/v1/admin/database/execute/jdbc", "ADMIN_ACCESS", "select 1"},
            {HttpMethod.POST, "/api/v1/admin/database/paginate/orient", "ADMIN_ACCESS", "{\"pageNumber\":1,\"elementPerPage\":1,\"wrappedRequest\":\"\"}"},
            {HttpMethod.POST, "/api/v1/admin/database/paginate/jdbc", "ADMIN_ACCESS", "{\"pageNumber\":1,\"elementPerPage\":1,\"wrappedRequest\":\"\"}"},
            {HttpMethod.GET, "/api/source/git/v1", "ADMIN_ACCESS", null},
            {HttpMethod.POST, "/api/source/git/v1", "ADMIN_ACCESS", "{}"},
            {HttpMethod.DELETE, "/api/source/git/v1/666", "ADMIN_ACCESS", null},
            {HttpMethod.POST, "/api/homepage/v1", "ADMIN_ACCESS", "{\"content\":\"\"}"},
            {HttpMethod.POST, "/api/v1/agentnetwork/configuration", "ADMIN_ACCESS", "{}"},
            {HttpMethod.GET, "/api/v1/description", "ADMIN_ACCESS", null},
            {HttpMethod.POST, "/api/v1/agentnetwork/explore", "ADMIN_ACCESS", "{\"creationDate\":\"1235\"}"},
            {HttpMethod.POST, "/api/ui/campaign/v1", "CAMPAIGN_WRITE", "{\"title\":\"secu\",\"scenarioIds\":[],\"tags\":[]}"},
            {HttpMethod.PUT, "/api/ui/campaign/v1", "CAMPAIGN_WRITE", "{\"title\":\"secu\",\"scenarioIds\":[],\"tags\":[]}"},
            {HttpMethod.DELETE, "/api/ui/campaign/v1/666", "CAMPAIGN_WRITE", null},
            {HttpMethod.GET, "/api/ui/campaign/v1/666", "CAMPAIGN_READ", null},
            {HttpMethod.GET, "/api/ui/campaign/v1/666/scenarios", "CAMPAIGN_READ", null},
            {HttpMethod.GET, "/api/ui/campaign/v1", "CAMPAIGN_READ", null},
            {HttpMethod.GET, "/api/ui/campaign/v1/lastexecutions/20", "CAMPAIGN_READ", null},
            {HttpMethod.GET, "/api/ui/campaign/v1/scenario/scenarioId", "SCENARIO_READ", null},
            {HttpMethod.GET, "/api/ui/campaign/v1/scheduling", "CAMPAIGN_READ", null},
            {HttpMethod.POST, "/api/ui/campaign/v1/scheduling", "CAMPAIGN_WRITE", "{}"},
            {HttpMethod.DELETE, "/api/ui/campaign/v1/scheduling/666", "CAMPAIGN_WRITE", null},
            {HttpMethod.GET, "/api/v1/datasets", "DATASET_READ", null},
            {HttpMethod.GET, "/api/v1/datasets", "SCENARIO_WRITE", null},
            {HttpMethod.GET, "/api/v1/datasets", "CAMPAIGN_WRITE", null},
            {HttpMethod.POST, "/api/v1/datasets", "DATASET_WRITE", "{\"name\":\"secu1\"} "},
            {HttpMethod.PUT, "/api/v1/datasets", "DATASET_WRITE", "{\"name\":\"secu2\"} "},
            {HttpMethod.DELETE, "/api/v1/datasets/dataSetId", "DATASET_WRITE", null},
            {HttpMethod.GET, "/api/v1/datasets/dataSetId", "DATASET_READ", null},
            {HttpMethod.GET, "/api/v1/datasets/dataSetId/versions/last", "DATASET_READ", null},
            {HttpMethod.GET, "/api/v1/datasets/dataSetId/versions", "DATASET_READ", null},
            {HttpMethod.GET, "/api/v1/datasets/dataSetId/versions/666", "DATASET_READ", null},
            {HttpMethod.GET, "/api/v1/datasets/dataSetId/666", "DATASET_READ", null},
            {HttpMethod.GET, "/api/v1/editions/testcases/testcaseId", "SCENARIO_READ", null},
            {HttpMethod.POST, "/api/v1/editions/testcases/testcaseId", "SCENARIO_WRITE", "{}"},
            {HttpMethod.DELETE, "/api/v1/editions/testcases/testcaseId", "SCENARIO_WRITE", null},
            {HttpMethod.GET, "/api/ui/globalvar/v1", "GLOBAL_VAR_READ", null},
            {HttpMethod.POST, "/api/ui/globalvar/v1/secupost", "GLOBAL_VAR_WRITE", "{\"message\":\"{}\"}"},
            {HttpMethod.DELETE, "/api/ui/globalvar/v1/secudelete", "GLOBAL_VAR_WRITE", null},
            {HttpMethod.GET, "/api/ui/globalvar/v1/secuget", "GLOBAL_VAR_READ", null},
            {HttpMethod.GET, "/api/ui/jira/v1/scenario", "SCENARIO_READ", null},
            {HttpMethod.GET, "/api/ui/jira/v1/scenario", "CAMPAIGN_WRITE", null},
            {HttpMethod.GET, "/api/ui/jira/v1/campaign", "CAMPAIGN_READ", null},
            {HttpMethod.GET, "/api/ui/jira/v1/scenario/scenarioId", "SCENARIO_WRITE", null},
            {HttpMethod.POST, "/api/ui/jira/v1/scenario", "SCENARIO_WRITE", "{\"id\":\"\",\"chutneyId\":\"\"}"},
            {HttpMethod.DELETE, "/api/ui/jira/v1/scenario/scenarioId", "SCENARIO_WRITE", null},
            {HttpMethod.GET, "/api/ui/jira/v1/campaign/campaignId", "CAMPAIGN_READ", null},
            {HttpMethod.GET, "/api/ui/jira/v1/testexec/testExecId", "CAMPAIGN_WRITE", null},
            {HttpMethod.POST, "/api/ui/jira/v1/campaign", "CAMPAIGN_WRITE", "{\"id\":\"\",\"chutneyId\":\"\"}"},
            {HttpMethod.DELETE, "/api/ui/jira/v1/campaign/campaignId", "CAMPAIGN_WRITE", null},
            {HttpMethod.GET, "/api/ui/jira/v1/configuration", "ADMIN_ACCESS", null},
            {HttpMethod.GET, "/api/ui/jira/v1/configuration/url", "SCENARIO_READ", null},
            {HttpMethod.GET, "/api/ui/jira/v1/configuration/url", "CAMPAIGN_READ", null},
            {HttpMethod.POST, "/api/ui/jira/v1/configuration", "ADMIN_ACCESS", "{\"url\":\"\",\"username\":\"\",\"password\":\"\"}"},
            {HttpMethod.POST, "/api/v1/ui/plugins/linkifier/", "ADMIN_ACCESS", "{\"pattern\":\"\",\"link\":\"\",\"id\":\"\"}"},
            {HttpMethod.DELETE, "/api/v1/ui/plugins/linkifier/id", "ADMIN_ACCESS", null},
            {HttpMethod.POST, "/api/scenario/component-edition", "SCENARIO_WRITE", "{\"title\":\"\",\"scenario\":{}}"},
            {HttpMethod.GET, "/api/scenario/component-edition/testCaseId", "SCENARIO_READ", null},
            {HttpMethod.DELETE, "/api/scenario/component-edition/testCaseId", "SCENARIO_WRITE", null},
            {HttpMethod.GET, "/api/scenario/component-edition/testCaseId/executable", "SCENARIO_READ", null},
            {HttpMethod.GET, "/api/scenario/component-edition/testCaseId/executable/parameters", "CAMPAIGN_WRITE", null},
            {HttpMethod.POST, "/api/steps/v1", "COMPONENT_WRITE", "{\"name\":\"\"}"},
            {HttpMethod.DELETE, "/api/steps/v1/stepId", "COMPONENT_WRITE", null},
            {HttpMethod.GET, "/api/steps/v1/all", "COMPONENT_READ", null},
            {HttpMethod.GET, "/api/steps/v1/all", "SCENARIO_WRITE", null},
            {HttpMethod.GET, "/api/steps/v1/stepId/parents", "COMPONENT_READ", null},
            {HttpMethod.GET, "/api/steps/v1", "COMPONENT_READ", null},
            {HttpMethod.GET, "/api/steps/v1/stepId ", "COMPONENT_READ", null},
            {HttpMethod.GET, "/api/scenario/v2/testCaseId", "SCENARIO_READ", null},
            {HttpMethod.GET, "/api/scenario/v2/testCaseId/metadata ", "SCENARIO_READ", null},
            {HttpMethod.GET, "/api/scenario/v2", "SCENARIO_READ", null},
            {HttpMethod.GET, "/api/scenario/v2", "CAMPAIGN_WRITE", null},
            {HttpMethod.POST, "/api/scenario/v2", "SCENARIO_WRITE", "{\"title\":\"\",\"scenario\":{\"when\":{}}}"},
            {HttpMethod.PATCH, "/api/scenario/v2", "SCENARIO_WRITE", "{\"title\":\"\",\"scenario\":{\"when\":{}}}"},
            {HttpMethod.DELETE, "/api/scenario/v2/testCaseId", "SCENARIO_WRITE", null},
            {HttpMethod.POST, "/api/scenario/v2/raw", "SCENARIO_WRITE", "{\"title\":\"\",\"content\":\"\"}"},
            {HttpMethod.GET, "/api/scenario/v2/raw/testCaseId", "SCENARIO_READ", null},
            {HttpMethod.GET, "/api/documentation", "ADMIN_ACCESS", null},
            {HttpMethod.POST, "/api/documentation", "ADMIN_ACCESS", null},
            {HttpMethod.GET, "/api/ui/campaign/execution/v1/campaignName", "CAMPAIGN_EXECUTE", null},
            {HttpMethod.GET, "/api/ui/campaign/execution/v1/campaignName/env", "CAMPAIGN_EXECUTE", null},
            {HttpMethod.POST, "/api/ui/campaign/execution/v1/replay/666", "CAMPAIGN_EXECUTE", "{}"},
            {HttpMethod.GET, "/api/ui/campaign/execution/v1/campaignPattern/surefire", "CAMPAIGN_EXECUTE", null},
            {HttpMethod.GET, "/api/ui/campaign/execution/v1/campaignPattern/surefire/env", "CAMPAIGN_EXECUTE", null},
            {HttpMethod.POST, "/api/ui/campaign/execution/v1/666/stop", "CAMPAIGN_EXECUTE", "{}"},
            {HttpMethod.GET, "/api/ui/campaign/execution/v1/byID/666", "CAMPAIGN_EXECUTE", null},
            {HttpMethod.GET, "/api/ui/campaign/execution/v1/byID/666/env", "CAMPAIGN_EXECUTE", null},
            {HttpMethod.GET, "/api/ui/scenario/scenarioId/execution/v1", "SCENARIO_READ", null},
            {HttpMethod.GET, "/api/ui/scenario/scenarioId/execution/666/v1", "SCENARIO_READ", null},
            {HttpMethod.POST, "/api/ui/scenario/execution/v1/scenarioId/env", "SCENARIO_EXECUTE", null},
            {HttpMethod.POST, "/api/ui/component/execution/v1/componentId/env", "COMPONENT_WRITE", null},
            {HttpMethod.POST, "/api/idea/scenario/execution/env", "SCENARIO_EXECUTE", "{\"content\":\"{}\",\"params\":{}} "},
            {HttpMethod.POST, "/api/ui/scenario/executionasync/v1/scenarioId/env", "SCENARIO_EXECUTE", "[]"},
            {HttpMethod.GET, "/api/ui/scenario/executionasync/v1/scenarioId/execution/666", "SCENARIO_READ", null},
            {HttpMethod.POST, "/api/ui/scenario/executionasync/v1/scenarioId/execution/666/stop", "SCENARIO_EXECUTE", null},
            {HttpMethod.POST, "/api/ui/scenario/executionasync/v1/scenarioId/execution/666/pause", "SCENARIO_EXECUTE", null},
            {HttpMethod.POST, "/api/ui/scenario/executionasync/v1/scenarioId/execution/666/resume", "SCENARIO_EXECUTE", null},
            {HttpMethod.POST, "/api/v1/authorizations", "ADMIN_ACCESS", "{}"},
            {HttpMethod.GET, "/api/v1/authorizations", "ADMIN_ACCESS", null},
            {HttpMethod.POST, "/api/scenario/execution/v1", "SCENARIO_EXECUTE", "{\"scenario\":{}}"},
            {HttpMethod.GET, "/api/task/v1", "COMPONENT_READ", null},
            {HttpMethod.GET, "/api/task/v1/taskId", "COMPONENT_READ", null},
            {HttpMethod.GET, "/api/v2/environment", "ENVIRONMENT_ACCESS", null},
            {HttpMethod.GET, "/api/v2/environment/names", "SCENARIO_EXECUTE", null},
            {HttpMethod.GET, "/api/v2/environment/names", "CAMPAIGN_WRITE", null},
            {HttpMethod.GET, "/api/v2/environment/names", "CAMPAIGN_EXECUTE", null},
            {HttpMethod.GET, "/api/v2/environment/names", "COMPONENT_WRITE", null},
            {HttpMethod.POST, "/api/v2/environment", "ENVIRONMENT_ACCESS", "{\"name\": \"secuenv\"} "},
            {HttpMethod.DELETE, "/api/v2/environment/envName", "ENVIRONMENT_ACCESS", null},
            {HttpMethod.PUT, "/api/v2/environment/envName", "ENVIRONMENT_ACCESS", "{}"},
            {HttpMethod.GET, "/api/v2/environment/envName/target", "ENVIRONMENT_ACCESS", null},
            {HttpMethod.GET, "/api/v2/environment/target", "ENVIRONMENT_ACCESS", null},
            {HttpMethod.GET, "/api/v2/environment/target/names ", "COMPONENT_READ", null},
            {HttpMethod.GET, "/api/v2/environment/envName", "ENVIRONMENT_ACCESS", null},
            {HttpMethod.GET, "/api/v2/environment/envName/target/targetName", "ENVIRONMENT_ACCESS", null},
            {HttpMethod.POST, "/api/v2/environment/envName/target", "ENVIRONMENT_ACCESS", "{\"name\":\"\",\"url\":\"\"}"},
            {HttpMethod.DELETE, "/api/v2/environment/envName/target/targetName", "ENVIRONMENT_ACCESS", null},
            {HttpMethod.PUT, "/api/v2/environment/envName/target/targetName", "ENVIRONMENT_ACCESS", "{\"name\":\"\",\"url\":\"\"}"},
            {HttpMethod.GET, "/actuator", "ADMIN_ACCESS", null},
            {HttpMethod.GET, "/actuator/beans", "ADMIN_ACCESS", null},
            {HttpMethod.GET, "/actuator/caches", "ADMIN_ACCESS", null},
            {HttpMethod.GET, "/actuator/health", "ADMIN_ACCESS", null},
            {HttpMethod.GET, "/actuator/info", "ADMIN_ACCESS", null},
            {HttpMethod.GET, "/actuator/conditions", "ADMIN_ACCESS", null},
            {HttpMethod.GET, "/actuator/configprops", "ADMIN_ACCESS", null},
            {HttpMethod.GET, "/actuator/env", "ADMIN_ACCESS", null},
            {HttpMethod.GET, "/actuator/liquidbase", "ADMIN_ACCESS", null},
            {HttpMethod.GET, "/actuator/loggers", "ADMIN_ACCESS", null},
            //{HttpMethod.GET, "/actuator/heapdump", "ADMIN_ACCESS", null}, in comment because it takes 2s
            {HttpMethod.GET, "/actuator/threaddump", "ADMIN_ACCESS", null},
            {HttpMethod.GET, "/actuator/prometheus", "ADMIN_ACCESS", null},
            {HttpMethod.GET, "/actuator/metrics", "ADMIN_ACCESS", null},
            {HttpMethod.GET, "/actuator/scheduledtasks", "ADMIN_ACCESS", null},
            {HttpMethod.GET, "/actuator/mappings", "ADMIN_ACCESS", null},
            // Must be at the end because the network configuration is in wrong staten, why ??
            {HttpMethod.POST, "/api/v1/agentnetwork/wrapup", "ADMIN_ACCESS", "{\"agentsGraph\":{\"agents\":[]},\"networkConfiguration\":{\"creationDate\":\"2021-09-06T10:08:36.569227Z\",\"agentNetworkConfiguration\":[],\"environmentsConfiguration\":[]}}"},

        };
    }

    private static Object[] unsecuredEndPointList() {
        return new Object[][]{
            {HttpMethod.GET, "/api/v1/user"},
            {HttpMethod.POST, "/api/v1/user"},
            {HttpMethod.GET, "/api/v1/ui/plugins/linkifier/"},
            {HttpMethod.GET, "/api/homepage/v1"},
            {HttpMethod.GET, "/home"}
        };
    }

    @ParameterizedTest
    @MethodSource("securedEndPointList")
    public void secured_api_access_verification(HttpMethod httpMethod, String url, String authority, String content) throws Exception {
        Collection<GrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority(authority));
        UserDto user = new UserDto();
        user.setName("userName");
        user.grantAuthority(authority);
        MockHttpServletRequestBuilder request = request(httpMethod, url)
            .secure(true)
            .with(user(user))
            .contentType(MediaType.APPLICATION_JSON);
        if (content != null) {
            request.content(content);
        }
        mvc.perform(request)
            .andExpect(status().is(anyOf(equalTo(200), equalTo(404))));
    }

    @ParameterizedTest
    @MethodSource("unsecuredEndPointList")
    @WithUserDetails
    public void unsecured_api_access_verification(HttpMethod httpMethod, String url) throws Exception {
        MockHttpServletRequestBuilder request = request(httpMethod, url)
            .secure(true)
            .contentType(MediaType.APPLICATION_JSON);
        mvc.perform(request)
            .andExpect(status().is(anyOf(equalTo(200), equalTo(404))));
    }
}
