package com.chutneytesting.design.api.scenario.compose;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON_UTF8_VALUE;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.chutneytesting.RestExceptionHandler;
import com.chutneytesting.design.api.scenario.compose.dto.ComposableTestCaseDto;
import com.chutneytesting.design.api.scenario.compose.dto.ImmutableComposableScenarioDto;
import com.chutneytesting.design.api.scenario.compose.dto.ImmutableComposableTestCaseDto;
import com.chutneytesting.design.domain.scenario.compose.ComposableScenario;
import com.chutneytesting.design.domain.scenario.compose.ComposableTestCase;
import com.chutneytesting.design.domain.scenario.compose.ComposableTestCaseRepository;
import com.chutneytesting.design.domain.scenario.TestCaseMetadataImpl;
import com.chutneytesting.design.domain.scenario.TestCaseRepository;
import com.chutneytesting.security.domain.User;
import com.chutneytesting.security.domain.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.MethodRule;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
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

    @Rule
    public MethodRule mockitoRule = MockitoJUnit.rule();

    @Mock
    private ComposableTestCaseRepository composableTestCaseRepository;
    @Mock
    private TestCaseRepository testCaseRepository;
    @Mock
    private UserService userService;
    @InjectMocks
    private ComponentEditionController sut;

    private MockMvc mockMvc;

    @Before
    public void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(sut)
            .setControllerAdvice(new RestExceptionHandler())
            .build();

        when(composableTestCaseRepository.findById(any()))
            .thenReturn(new ComposableTestCase(DEFAULT_COMPOSABLE_TESTCASE_DB_ID, TestCaseMetadataImpl.builder().build(), ComposableScenario.builder().build()));

        when(composableTestCaseRepository.save(any()))
            .thenReturn(DEFAULT_COMPOSABLE_TESTCASE_DB_ID);

        when(userService.getCurrentUser()).thenReturn(User.ANONYMOUS_USER);
    }

    @Test
    public void should_save_testCase() throws Exception {
        // When
        mockMvc.perform(post(ComponentEditionController.BASE_URL)
            .contentType(APPLICATION_JSON_UTF8_VALUE)
            .content(om.writeValueAsString(composableTestCaseDto)))
            .andExpect(status().isOk());

        // Then
        verify(composableTestCaseRepository).save(any());
    }

    @Test
    public void should_find_testCase() throws Exception {
        // When
        mockMvc.perform(get(ComponentEditionController.BASE_URL + "/" + DEFAULT_COMPOSABLE_TESTCASE_ID));

        // Then
        verify(composableTestCaseRepository).findById(DEFAULT_COMPOSABLE_TESTCASE_DB_ID);
    }

    @Test
    public void should_delete_testCase() throws Exception {
        // When
        mockMvc.perform(delete(ComponentEditionController.BASE_URL + "/" + DEFAULT_COMPOSABLE_TESTCASE_ID));

        // Then
        verify(composableTestCaseRepository).removeById(DEFAULT_COMPOSABLE_TESTCASE_DB_ID);
        verify(testCaseRepository).removeById(DEFAULT_COMPOSABLE_TESTCASE_DB_ID);
    }
}
