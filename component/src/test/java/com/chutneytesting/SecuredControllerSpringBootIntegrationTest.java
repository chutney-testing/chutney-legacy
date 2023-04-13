package com.chutneytesting;

import static java.util.Optional.ofNullable;
import static org.springframework.http.HttpMethod.DELETE;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpMethod.POST;
import static org.springframework.http.HttpMethod.PUT;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.OK;
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
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
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
            {POST, "/api/v1/admin/database/execute/orient", "ADMIN_ACCESS", "select 1", OK},
            {POST, "/api/v1/admin/database/paginate/orient", "ADMIN_ACCESS", "{\"pageNumber\":1,\"elementPerPage\":1,\"wrappedRequest\":\"\"}", OK},

            {GET, "/api/v1/datasets", "DATASET_READ", null, OK},
            {GET, "/api/v1/datasets", "SCENARIO_WRITE", null, OK},
            {GET, "/api/v1/datasets", "CAMPAIGN_WRITE", null, OK},
            {POST, "/api/v1/datasets", "DATASET_WRITE", "{\"name\":\"secu1\"} ", OK},
            {PUT, "/api/v1/datasets", "DATASET_WRITE", "{\"name\":\"secu2\"} ", NOT_FOUND},

            {DELETE, "/api/v1/datasets/dataSetId", "DATASET_WRITE", null, NOT_FOUND},
            {GET, "/api/v1/datasets/dataSetId", "DATASET_READ", null, NOT_FOUND},
            {GET, "/api/v1/datasets/dataSetId/versions/last", "DATASET_READ", null, NOT_FOUND},
            {GET, "/api/v1/datasets/dataSetId/versions", "DATASET_READ", null, OK},
            {GET, "/api/v1/datasets/dataSetId/versions/42", "DATASET_READ", null, NOT_FOUND},
            {GET, "/api/v1/datasets/dataSetId/42", "DATASET_READ", null, NOT_FOUND},

            {POST, "/api/scenario/component-edition", "SCENARIO_WRITE", "{\"title\":\"\",\"scenario\":{}}", OK},
            {GET, "/api/scenario/component-edition/testCaseId", "SCENARIO_READ", null, NOT_FOUND},
            {DELETE, "/api/scenario/component-edition/testCaseId", "SCENARIO_WRITE", null, OK},
            {GET, "/api/scenario/component-edition/testCaseId/executable", "SCENARIO_READ", null, NOT_FOUND},
            {GET, "/api/scenario/component-edition/testCaseId/executable/parameters", "CAMPAIGN_WRITE", null, NOT_FOUND},
            {POST, "/api/steps/v1", "COMPONENT_WRITE", "{\"name\":\"\"}", OK},
            {DELETE, "/api/steps/v1/stepId", "COMPONENT_WRITE", null, OK},
            {GET, "/api/steps/v1/all", "COMPONENT_READ", null, OK},
            {GET, "/api/steps/v1/all", "SCENARIO_WRITE", null, OK},
            {GET, "/api/steps/v1/stepId/parents", "COMPONENT_READ", null, NOT_FOUND},
            {GET, "/api/steps/v1/stepId", "COMPONENT_READ", null, NOT_FOUND},
            {POST, "/api/ui/componentstep/execution/v1/componentId/env", "COMPONENT_WRITE", null, NOT_FOUND}

        };
    }

    @ParameterizedTest
    @MethodSource({"securedEndPointList"})
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

}
