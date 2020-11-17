package com.chutneytesting.design.api.testcase;

import static com.chutneytesting.security.domain.User.ANONYMOUS_USER;
import static java.time.Instant.now;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON_UTF8_VALUE;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.chutneytesting.RestExceptionHandler;
import com.chutneytesting.WebConfiguration;
import com.chutneytesting.design.domain.scenario.TestCaseMetadata;
import com.chutneytesting.design.domain.scenario.TestCaseMetadataImpl;
import com.chutneytesting.design.domain.testcase.TestCaseEdition;
import com.chutneytesting.design.domain.testcase.TestCaseEditionsService;
import com.chutneytesting.security.domain.UserService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Instant;
import java.util.List;
import org.assertj.core.util.Lists;
import org.junit.Before;
import org.junit.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

public class TestCaseEditionControllerTest {

    private final TestCaseEditionsService testCaseEditionService = mock(TestCaseEditionsService.class);
    private final UserService userService = mock(UserService.class);
    private MockMvc mockMvc;
    private final ObjectMapper om = new WebConfiguration().objectMapper();

    @Before
    public void before() {
        TestCaseEditionController sut = new TestCaseEditionController(testCaseEditionService, userService);

        mockMvc = MockMvcBuilders.standaloneSetup(sut)
            .setControllerAdvice(new RestExceptionHandler())
            .build();

        when(userService.getCurrentUser()).thenReturn(ANONYMOUS_USER);
    }

    @Test
    public void should_get_testcase_editions() throws Exception {
        // Given
        String testCaseId = "testCaseId";
        TestCaseEdition firstTestCaseEdition = buildEdition(testCaseId, 2, now(), "editor");
        TestCaseEdition secondTestCaseEdition = buildEdition(testCaseId, 5, now().minusSeconds(45), "another editor");
        when(testCaseEditionService.getTestCaseEditions(testCaseId)).thenReturn(
            Lists.list(firstTestCaseEdition, secondTestCaseEdition)
        );

        // When
        MvcResult mvcResult = mockMvc.perform(get(TestCaseEditionController.BASE_URL + "/" + testCaseId))
            .andExpect(status().isOk())
            .andReturn();

        // Then
        List<TestCaseEditionDto> result = om.readValue(
            mvcResult.getResponse().getContentAsString(),
            new TypeReference<List<TestCaseEditionDto>>() {
            }
        );

        assertThat(result).hasSize(2);
        assertDtoIsEqualToEdition(result.get(0), firstTestCaseEdition);
        assertDtoIsEqualToEdition(result.get(1), secondTestCaseEdition);
    }

    @Test
    public void should_edit_testcase() throws Exception {
        // Given
        String testCaseId = "testCaseId";
        TestCaseEdition newTestCaseEdition = buildEdition(testCaseId, 2, now(), ANONYMOUS_USER.getId());
        when(testCaseEditionService.editTestCase(testCaseId, ANONYMOUS_USER.getId())).thenReturn(newTestCaseEdition);

        // When
        MvcResult mvcResult = mockMvc.perform(
            post(TestCaseEditionController.BASE_URL + "/" + testCaseId)
                .contentType(APPLICATION_JSON_UTF8_VALUE)
                .content("")
        )
            .andExpect(status().isOk())
            .andReturn();

        // Then
        TestCaseEditionDto dto = om.readValue(mvcResult.getResponse().getContentAsString(), TestCaseEditionDto.class);
        assertDtoIsEqualToEdition(dto, newTestCaseEdition);
    }

    @Test
    public void should_end_testcase_edition() throws Exception {
        // Given
        String testCaseId = "testCaseId";

        // When
        MvcResult mvcResult = mockMvc.perform(delete(TestCaseEditionController.BASE_URL + "/" + testCaseId))
            .andExpect(status().isOk())
            .andReturn();

        // Then
        verify(testCaseEditionService).endTestCaseEdition(testCaseId, ANONYMOUS_USER.getId());
    }

    private void assertDtoIsEqualToEdition(TestCaseEditionDto dto, TestCaseEdition edition) {
        assertThat(dto.testCaseId()).isEqualTo(edition.testCaseMetadata.id());
        assertThat(dto.testCaseVersion()).isEqualTo(edition.testCaseMetadata.version());
        assertThat(dto.editionUser()).isEqualTo(edition.editor);
        assertThat(dto.editionStartDate()).isEqualTo(edition.startDate);
    }

    private TestCaseEdition buildEdition(String testCaseId, Integer version, Instant startDate, String editor) {
        return new TestCaseEdition(
            buildMetadataForEdition(testCaseId, version),
            startDate,
            editor
        );
    }

    private TestCaseMetadata buildMetadataForEdition(String testCaseId, Integer version) {
        return TestCaseMetadataImpl.builder()
            .withId(testCaseId)
            .withVersion(version)
            .build();
    }
}
