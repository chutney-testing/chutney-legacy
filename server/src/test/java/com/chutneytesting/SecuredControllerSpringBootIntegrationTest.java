package com.chutneytesting;

import static java.util.Optional.ofNullable;
import static org.hamcrest.core.AnyOf.anyOf;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.springframework.http.HttpMethod.DELETE;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpMethod.PATCH;
import static org.springframework.http.HttpMethod.POST;
import static org.springframework.http.HttpMethod.PUT;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.request;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.chutneytesting.security.api.UserDto;
import com.chutneytesting.tools.file.FileUtils;
import java.io.File;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
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
            {GET, "/api/v1/backups", "ADMIN_ACCESS", null},
            {POST, "/api/v1/backups", "ADMIN_ACCESS", "{\"backupables\": [ \"environments\" ]}"},
            {GET, "/api/v1/backups/backupId", "ADMIN_ACCESS", null},
            {DELETE, "/api/v1/backups/backupId", "ADMIN_ACCESS", null},
            {GET, "/api/v1/backups/id/download", "ADMIN_ACCESS", null},
            {GET, "/api/v1/backups/backupables", "ADMIN_ACCESS", null},
            {POST, "/api/v1/admin/database/execute/orient", "ADMIN_ACCESS", "select 1"},
            {POST, "/api/v1/admin/database/execute/jdbc", "ADMIN_ACCESS", "select 1"},
            {POST, "/api/v1/admin/database/paginate/orient", "ADMIN_ACCESS", "{\"pageNumber\":1,\"elementPerPage\":1,\"wrappedRequest\":\"\"}"},
            {POST, "/api/v1/admin/database/paginate/jdbc", "ADMIN_ACCESS", "{\"pageNumber\":1,\"elementPerPage\":1,\"wrappedRequest\":\"\"}"},
            {POST, "/api/v1/agentnetwork/configuration", "ADMIN_ACCESS", "{}"},
            {GET, "/api/v1/description", "ADMIN_ACCESS", null},
            {POST, "/api/v1/agentnetwork/explore", "ADMIN_ACCESS", "{\"creationDate\":\"1235\"}"},
            {POST, "/api/ui/campaign/v1", "CAMPAIGN_WRITE", "{\"title\":\"secu\",\"scenarioIds\":[],\"tags\":[]}"},
            {PUT, "/api/ui/campaign/v1", "CAMPAIGN_WRITE", "{\"title\":\"secu\",\"scenarioIds\":[],\"tags\":[]}"},
            {DELETE, "/api/ui/campaign/v1/666", "CAMPAIGN_WRITE", null},
            {GET, "/api/ui/campaign/v1/666", "CAMPAIGN_READ", null},
            {GET, "/api/ui/campaign/v1/666/scenarios", "CAMPAIGN_READ", null},
            {GET, "/api/ui/campaign/v1", "CAMPAIGN_READ", null},
            {GET, "/api/ui/campaign/v1/lastexecutions/20", "CAMPAIGN_READ", null},
            {GET, "/api/ui/campaign/v1/scenario/scenarioId", "SCENARIO_READ", null},
            {GET, "/api/ui/campaign/v1/scheduling", "CAMPAIGN_READ", null},
            {POST, "/api/ui/campaign/v1/scheduling", "CAMPAIGN_WRITE", "{}"},
            {DELETE, "/api/ui/campaign/v1/scheduling/666", "CAMPAIGN_WRITE", null},
            {GET, "/api/v1/datasets", "DATASET_READ", null},
            {GET, "/api/v1/datasets", "SCENARIO_WRITE", null},
            {GET, "/api/v1/datasets", "CAMPAIGN_WRITE", null},
            {POST, "/api/v1/datasets", "DATASET_WRITE", "{\"name\":\"secu1\"} "},
            {PUT, "/api/v1/datasets", "DATASET_WRITE", "{\"name\":\"secu2\"} "},
            {DELETE, "/api/v1/datasets/dataSetId", "DATASET_WRITE", null},
            {GET, "/api/v1/datasets/dataSetId", "DATASET_READ", null},
            {GET, "/api/v1/datasets/dataSetId/versions/last", "DATASET_READ", null},
            {GET, "/api/v1/datasets/dataSetId/versions", "DATASET_READ", null},
            {GET, "/api/v1/datasets/dataSetId/versions/666", "DATASET_READ", null},
            {GET, "/api/v1/datasets/dataSetId/666", "DATASET_READ", null},
            {GET, "/api/v1/editions/testcases/testcaseId", "SCENARIO_READ", null},
            {POST, "/api/v1/editions/testcases/testcaseId", "SCENARIO_WRITE", "{}"},
            {DELETE, "/api/v1/editions/testcases/testcaseId", "SCENARIO_WRITE", null},
            {GET, "/api/ui/globalvar/v1", "GLOBAL_VAR_READ", null},
            {POST, "/api/ui/globalvar/v1/secupost", "GLOBAL_VAR_WRITE", "{\"message\":\"{}\"}"},
            {DELETE, "/api/ui/globalvar/v1/secudelete", "GLOBAL_VAR_WRITE", null},
            {GET, "/api/ui/globalvar/v1/secuget", "GLOBAL_VAR_READ", null},
            {GET, "/api/ui/jira/v1/scenario", "SCENARIO_READ", null},
            {GET, "/api/ui/jira/v1/scenario", "CAMPAIGN_WRITE", null},
            {GET, "/api/ui/jira/v1/campaign", "CAMPAIGN_READ", null},
            {GET, "/api/ui/jira/v1/scenario/scenarioId", "SCENARIO_WRITE", null},
            {POST, "/api/ui/jira/v1/scenario", "SCENARIO_WRITE", "{\"id\":\"\",\"chutneyId\":\"\"}"},
            {DELETE, "/api/ui/jira/v1/scenario/scenarioId", "SCENARIO_WRITE", null},
            {GET, "/api/ui/jira/v1/campaign/campaignId", "CAMPAIGN_READ", null},
            // {GET, "/api/ui/jira/v1/testexec/testExecId", "CAMPAIGN_WRITE", null}, need a valid jira url
            {PUT, "/api/ui/jira/v1/testexec/testExecId", "CAMPAIGN_WRITE", "{\"id\":\"\",\"chutneyId\":\"\"}"},
            {POST, "/api/ui/jira/v1/campaign", "CAMPAIGN_WRITE", "{\"id\":\"\",\"chutneyId\":\"\"}"},
            {DELETE, "/api/ui/jira/v1/campaign/campaignId", "CAMPAIGN_WRITE", null},
            {GET, "/api/ui/jira/v1/configuration", "ADMIN_ACCESS", null},
            {GET, "/api/ui/jira/v1/configuration/url", "SCENARIO_READ", null},
            {GET, "/api/ui/jira/v1/configuration/url", "CAMPAIGN_READ", null},
            {POST, "/api/ui/jira/v1/configuration", "ADMIN_ACCESS", "{\"url\":\"\",\"username\":\"\",\"password\":\"\"}"},
            {POST, "/api/v1/ui/plugins/linkifier/", "ADMIN_ACCESS", "{\"pattern\":\"\",\"link\":\"\",\"id\":\"\"}"},
            {DELETE, "/api/v1/ui/plugins/linkifier/id", "ADMIN_ACCESS", null},
            {POST, "/api/scenario/component-edition", "SCENARIO_WRITE", "{\"title\":\"\",\"scenario\":{}}"},
            {GET, "/api/scenario/component-edition/testCaseId", "SCENARIO_READ", null},
            {DELETE, "/api/scenario/component-edition/testCaseId", "SCENARIO_WRITE", null},
            {GET, "/api/scenario/component-edition/testCaseId/executable", "SCENARIO_READ", null},
            {GET, "/api/scenario/component-edition/testCaseId/executable/parameters", "CAMPAIGN_WRITE", null},
            {POST, "/api/steps/v1", "COMPONENT_WRITE", "{\"name\":\"\"}"},
            {DELETE, "/api/steps/v1/stepId", "COMPONENT_WRITE", null},
            {GET, "/api/steps/v1/all", "COMPONENT_READ", null},
            {GET, "/api/steps/v1/all", "SCENARIO_WRITE", null},
            {GET, "/api/steps/v1/stepId/parents", "COMPONENT_READ", null},
            {GET, "/api/steps/v1/stepId", "COMPONENT_READ", null},
            {GET, "/api/scenario/v2/1", "SCENARIO_READ", null},
            {GET, "/api/scenario/v2/testCaseId/metadata", "SCENARIO_READ", null},
            {GET, "/api/scenario/v2", "SCENARIO_READ", null},
            {GET, "/api/scenario/v2", "CAMPAIGN_WRITE", null},
            {POST, "/api/scenario/v2", "SCENARIO_WRITE", "{\"title\":\"\",\"scenario\":{\"when\":{}}}"},
            {PATCH, "/api/scenario/v2", "SCENARIO_WRITE", "{\"title\":\"\",\"scenario\":{\"when\":{}}}"},
            {DELETE, "/api/scenario/v2/testCaseId", "SCENARIO_WRITE", null},
            {POST, "/api/scenario/v2/raw", "SCENARIO_WRITE", "{\"title\":\"\",\"content\":\"{\\\"when\\\":{}}\"}"},
            {GET, "/api/scenario/v2/raw/1", "SCENARIO_READ", null},
            {GET, "/api/ui/campaign/execution/v1/campaignName", "CAMPAIGN_EXECUTE", null},
            {GET, "/api/ui/campaign/execution/v1/campaignName/env", "CAMPAIGN_EXECUTE", null},
            {POST, "/api/ui/campaign/execution/v1/replay/666", "CAMPAIGN_EXECUTE", "{}"},
            {GET, "/api/ui/campaign/execution/v1/campaignPattern/surefire", "CAMPAIGN_EXECUTE", null},
            {GET, "/api/ui/campaign/execution/v1/campaignPattern/surefire/env", "CAMPAIGN_EXECUTE", null},
            {POST, "/api/ui/campaign/execution/v1/666/stop", "CAMPAIGN_EXECUTE", "{}"},
            {GET, "/api/ui/campaign/execution/v1/byID/666", "CAMPAIGN_EXECUTE", null},
            {GET, "/api/ui/campaign/execution/v1/byID/666/env", "CAMPAIGN_EXECUTE", null},
            {GET, "/api/ui/scenario/scenarioId/execution/v1", "SCENARIO_READ", null},
            {GET, "/api/ui/scenario/scenarioId/execution/666/v1", "SCENARIO_READ", null},
            {GET, "/api/ui/scenario/execution/666/summary/v1", "SCENARIO_READ", null},
            {POST, "/api/ui/scenario/execution/v1/scenarioId/env", "SCENARIO_EXECUTE", null},
            {POST, "/api/ui/componentstep/execution/v1/componentId/env", "COMPONENT_WRITE", null},
            {POST, "/api/idea/scenario/execution/env", "SCENARIO_EXECUTE", "{\"content\":\"{\\\"when\\\":{}}\",\"params\":{}} "},
            {POST, "/api/ui/scenario/executionasync/v1/scenarioId/env", "SCENARIO_EXECUTE", "[]"},
            {GET, "/api/ui/scenario/executionasync/v1/scenarioId/execution/666", "SCENARIO_READ", null},
            {POST, "/api/ui/scenario/executionasync/v1/scenarioId/execution/666/stop", "SCENARIO_EXECUTE", null},
            {POST, "/api/ui/scenario/executionasync/v1/scenarioId/execution/666/pause", "SCENARIO_EXECUTE", null},
            {POST, "/api/ui/scenario/executionasync/v1/scenarioId/execution/666/resume", "SCENARIO_EXECUTE", null},
            {POST, "/api/v1/authorizations", "ADMIN_ACCESS", "{}"},
            {GET, "/api/v1/authorizations", "ADMIN_ACCESS", null},
            {POST, "/api/scenario/execution/v1", "SCENARIO_EXECUTE", "{\"scenario\":{}}"},
            {GET, "/api/action/v1", "COMPONENT_READ", null},
            {GET, "/api/action/v1/actionId", "COMPONENT_READ", null},
            {GET, "/api/v2/environment", "ENVIRONMENT_ACCESS", null},
            {GET, "/api/v2/environment/names", "SCENARIO_EXECUTE", null},
            {GET, "/api/v2/environment/names", "CAMPAIGN_WRITE", null},
            {GET, "/api/v2/environment/names", "CAMPAIGN_EXECUTE", null},
            {GET, "/api/v2/environment/names", "COMPONENT_WRITE", null},
            {POST, "/api/v2/environment", "ENVIRONMENT_ACCESS", "{\"name\": \"secuenv\"} "},
            {DELETE, "/api/v2/environment/envName", "ENVIRONMENT_ACCESS", null},
            {PUT, "/api/v2/environment/envName", "ENVIRONMENT_ACCESS", "{}"},
            {GET, "/api/v2/environment/envName/target", "ENVIRONMENT_ACCESS", null},
            {GET, "/api/v2/environment/target", "ENVIRONMENT_ACCESS", null},
            {GET, "/api/v2/environment/target/names", "COMPONENT_READ", null},
            {GET, "/api/v2/environment/envName", "ENVIRONMENT_ACCESS", null},
            {GET, "/api/v2/environment/envName/target/targetName", "ENVIRONMENT_ACCESS", null},
            {POST, "/api/v2/environment/envName/target", "ENVIRONMENT_ACCESS", "{\"name\":\"\",\"url\":\"\"}"},
            {DELETE, "/api/v2/environment/envName/target/targetName", "ENVIRONMENT_ACCESS", null},
            {PUT, "/api/v2/environment/envName/target/targetName", "ENVIRONMENT_ACCESS", "{\"name\":\"\",\"url\":\"\"}"},
            {GET, "/actuator", "ADMIN_ACCESS", null},
            {GET, "/actuator/beans", "ADMIN_ACCESS", null},
            {GET, "/actuator/caches", "ADMIN_ACCESS", null},
            {GET, "/actuator/health", "ADMIN_ACCESS", null},
            {GET, "/actuator/info", "ADMIN_ACCESS", null},
            {GET, "/actuator/conditions", "ADMIN_ACCESS", null},
            {GET, "/actuator/configprops", "ADMIN_ACCESS", null},
            {GET, "/actuator/env", "ADMIN_ACCESS", null},
            {GET, "/actuator/liquidbase", "ADMIN_ACCESS", null},
            {GET, "/actuator/loggers", "ADMIN_ACCESS", null},
            //{HttpMethod.GET, "/actuator/heapdump", "ADMIN_ACCESS", null}, in comment because it takes 2s
            {GET, "/actuator/threaddump", "ADMIN_ACCESS", null},
            {GET, "/actuator/prometheus", "ADMIN_ACCESS", null},
            {GET, "/actuator/metrics", "ADMIN_ACCESS", null},
            {GET, "/actuator/scheduledtasks", "ADMIN_ACCESS", null},
            {GET, "/actuator/mappings", "ADMIN_ACCESS", null},
            // Must be at the end because the network configuration is in wrong staten, why ??
            {POST, "/api/v1/agentnetwork/wrapup", "ADMIN_ACCESS", "{\"agentsGraph\":{\"agents\":[]},\"networkConfiguration\":{\"creationDate\":\"2021-09-06T10:08:36.569227Z\",\"agentNetworkConfiguration\":[],\"environmentsConfiguration\":[]}}"},
        };
    }

    private static Object[] unsecuredEndPointList() {
        return new Object[][]{
            {GET, "/api/v1/user", null, null},
            {POST, "/api/v1/user", null, "{}"},
            {GET, "/api/v1/ui/plugins/linkifier/", null, null},
            {GET, "/api/v1/info/build/version", null, null},
            {GET, "/api/v1/info/appname", null, null},
        };
    }

    @ParameterizedTest
    @MethodSource({"securedEndPointList", "unsecuredEndPointList"})
    public void secured_api_access_verification(HttpMethod httpMethod, String url, String authority, String content) throws Exception {
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
            .andExpect(status().is(anyOf(equalTo(200), equalTo(404))));
    }
}
