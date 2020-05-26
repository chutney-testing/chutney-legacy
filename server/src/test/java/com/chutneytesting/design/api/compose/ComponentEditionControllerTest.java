package com.chutneytesting.design.api.compose;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON_UTF8_VALUE;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.chutneytesting.RestExceptionHandler;
import com.chutneytesting.design.api.compose.dto.ComposableTestCaseDto;
import com.chutneytesting.design.api.compose.dto.ImmutableComposableScenarioDto;
import com.chutneytesting.design.api.compose.dto.ImmutableComposableTestCaseDto;
import com.chutneytesting.design.domain.compose.ComposableScenario;
import com.chutneytesting.design.domain.compose.ComposableTestCase;
import com.chutneytesting.design.domain.compose.ComposableTestCaseRepository;
import com.chutneytesting.design.domain.scenario.TestCaseMetadataImpl;
import com.chutneytesting.design.domain.scenario.TestCaseRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

public class ComponentEditionControllerTest {

    private final String DEFAULT_COMPOSABLE_TESTCASE_DB_ID = "#30:1";
    private final String DEFAULT_COMPOSABLE_TESTCASE_ID = "30-1";

    private final ObjectMapper om = new ObjectMapper().findAndRegisterModules();
    private final ComposableTestCaseDto composableTestCaseDto =
        ImmutableComposableTestCaseDto.builder()
            .id(DEFAULT_COMPOSABLE_TESTCASE_ID)
            .title("Default title")
            .scenario(
                ImmutableComposableScenarioDto.builder().build()
            )
            .build();

    private ComposableTestCaseRepository composableTestCaseRepository = mock(ComposableTestCaseRepository.class);
    private TestCaseRepository testCaseRepository = mock(TestCaseRepository.class);

    private MockMvc mockMvc;

    @BeforeEach
    public void setUp() {
        ComponentEditionController sut = new ComponentEditionController(composableTestCaseRepository, testCaseRepository);

        mockMvc = MockMvcBuilders.standaloneSetup(sut)
            .setControllerAdvice(new RestExceptionHandler())
            .build();

        when(composableTestCaseRepository.findById(any()))
            .thenReturn(new ComposableTestCase(DEFAULT_COMPOSABLE_TESTCASE_DB_ID, TestCaseMetadataImpl.builder().build(), ComposableScenario.builder().build()));

        when(composableTestCaseRepository.save(any()))
            .thenReturn(DEFAULT_COMPOSABLE_TESTCASE_DB_ID);
    }

    @Test
    public void should_call_repository_and_mapper_when_save_called() throws Exception {
        // When
        mockMvc.perform(post(ComponentEditionController.BASE_URL)
            .contentType(APPLICATION_JSON_UTF8_VALUE)
            .content(om.writeValueAsString(composableTestCaseDto)))
            .andExpect(status().isOk());

        // Then
        verify(composableTestCaseRepository).save(any());
    }

    @Test
    public void should_call_repository_and_mapper_when_get_called() throws Exception {
        // When
        mockMvc.perform(get(ComponentEditionController.BASE_URL + "/" + DEFAULT_COMPOSABLE_TESTCASE_ID));

        // Then
        verify(composableTestCaseRepository).findById(DEFAULT_COMPOSABLE_TESTCASE_DB_ID);
    }

    @Test
    public void should_call_repository_and_mapper_when_delete_called() throws Exception {
        // When
        mockMvc.perform(delete(ComponentEditionController.BASE_URL + "/" + DEFAULT_COMPOSABLE_TESTCASE_ID));

        // Then
        verify(composableTestCaseRepository).removeById(DEFAULT_COMPOSABLE_TESTCASE_DB_ID);
        verify(testCaseRepository).removeById(DEFAULT_COMPOSABLE_TESTCASE_DB_ID);
    }
}
