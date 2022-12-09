package com.chutneytesting.scenario.api;


import static java.time.temporal.ChronoUnit.SECONDS;
import static java.util.Optional.of;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.chutneytesting.scenario.api.raw.dto.TestCaseIndexDto;
import com.chutneytesting.server.core.domain.execution.history.ExecutionHistoryRepository;
import com.chutneytesting.server.core.domain.scenario.TestCase;
import com.chutneytesting.server.core.domain.scenario.TestCaseMetadata;
import com.chutneytesting.server.core.domain.scenario.TestCaseRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Instant;
import java.util.List;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

class AggregatedTestCaseControllerTest {

    private final ObjectMapper om = new ObjectMapper().findAndRegisterModules();
    private final ExecutionHistoryRepository executionHistoryRepository = mock(ExecutionHistoryRepository.class);
    private final TestCaseRepository testCaseRepository = mock(TestCaseRepository.class);

    private MockMvc mockMvc;

    @BeforeEach
    public void setUp() {
        AggregatedTestCaseController testCaseController = new AggregatedTestCaseController(testCaseRepository, executionHistoryRepository);
        mockMvc = MockMvcBuilders.standaloneSetup(testCaseController).build();
    }

    @Test
    public void should_call_repository_to_get_scenario_metadata() throws Exception {
        // Given
        TestCase mockTestCase = mock(TestCase.class);
        TestCaseMetadata mockTestCaseMetadata = mock(TestCaseMetadata.class);
        when(mockTestCaseMetadata.id()).thenReturn("1");
        when(mockTestCaseMetadata.creationDate()).thenReturn(Instant.now());
        when(mockTestCaseMetadata.updateDate()).thenReturn(Instant.now());
        when(mockTestCaseMetadata.title()).thenReturn("TestCase title");
        when(mockTestCaseMetadata.description()).thenReturn("TestCase description");
        when(mockTestCase.metadata()).thenReturn(mockTestCaseMetadata);
        when(testCaseRepository.findById(eq("1"))).thenReturn(of(mockTestCase));

        //When
        MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders.get("/api/scenario/v2/1/metadata")
                .accept(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andReturn();

        //Then
        verify(testCaseRepository).findById(eq("1"));

        TestCaseIndexDto testCaseIndexDto = om.readValue(mvcResult.getResponse().getContentAsString(), TestCaseIndexDto.class);
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(testCaseIndexDto.metadata().id()).isEqualTo(of("1"));
        softly.assertThat(testCaseIndexDto.metadata().creationDate()).isAfter(Instant.now().minus(1, SECONDS));
        softly.assertThat(testCaseIndexDto.metadata().updateDate()).isAfter(Instant.now().minus(1, SECONDS));
        softly.assertThat(testCaseIndexDto.metadata().title()).isEqualTo("TestCase title");
        softly.assertThat(testCaseIndexDto.metadata().description()).isEqualTo(of("TestCase description"));
        softly.assertAll();
    }

    @Test
    public void should_call_repository_to_get_all_scenarios_metadata() throws Exception {
        // Given
        TestCaseMetadata mockTestCaseMetadata = mock(TestCaseMetadata.class);
        when(mockTestCaseMetadata.id()).thenReturn("1");
        when(mockTestCaseMetadata.creationDate()).thenReturn(Instant.now());
        when(mockTestCaseMetadata.updateDate()).thenReturn(Instant.now());
        when(mockTestCaseMetadata.title()).thenReturn("TestCase title");
        when(mockTestCaseMetadata.description()).thenReturn("TestCase description");
        when(testCaseRepository.findAll()).thenReturn(List.of(mockTestCaseMetadata));

        //When
        MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders.get("/api/scenario/v2/")
                .accept(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andReturn();

        //Then
        verify(testCaseRepository).findAll();

        TestCaseIndexDto testCaseIndexDto = (om.readValue(mvcResult.getResponse().getContentAsString(), new TypeReference<List<TestCaseIndexDto>>() {
        })).get(0);
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(testCaseIndexDto.metadata().id()).isEqualTo(of("1"));
        softly.assertThat(testCaseIndexDto.metadata().creationDate()).isAfter(Instant.now().minus(10, SECONDS));
        softly.assertThat(testCaseIndexDto.metadata().updateDate()).isAfter(Instant.now().minus(10, SECONDS));
        softly.assertThat(testCaseIndexDto.metadata().title()).isEqualTo("TestCase title");
        softly.assertThat(testCaseIndexDto.metadata().description()).isEqualTo(of("TestCase description"));
        softly.assertAll();
    }

    @Test
    public void should_call_repository_to_delete_scenario() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.delete("/api/scenario/v2/1")
                .accept(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(MockMvcResultMatchers.status().isOk());
        verify(testCaseRepository).removeById(eq("1"));
    }
}
