package com.chutneytesting.scenario.api;

import static java.util.Optional.of;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.chutneytesting.scenario.api.raw.dto.GwtTestCaseMetadataDto;
import com.chutneytesting.scenario.api.raw.dto.TestCaseIndexDto;
import com.chutneytesting.server.core.domain.execution.history.ExecutionHistoryRepository;
import com.chutneytesting.server.core.domain.scenario.TestCase;
import com.chutneytesting.server.core.domain.scenario.TestCaseMetadata;
import com.chutneytesting.server.core.domain.scenario.TestCaseMetadataImpl;
import com.chutneytesting.server.core.domain.scenario.TestCaseRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
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
        String id = "1";
        TestCaseMetadata fakeMetadata = TestCaseMetadataImpl.builder().withId(id).build();
        when(mockTestCase.metadata()).thenReturn(fakeMetadata);
        when(testCaseRepository.findById(eq(id))).thenReturn(of(mockTestCase));

        //When
        MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders.get("/api/scenario/v2/1/metadata")
                .accept(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andReturn();

        //Then
        verify(testCaseRepository).findById(eq(id));

        GwtTestCaseMetadataDto actualMetadata = om.readValue(mvcResult.getResponse().getContentAsString(), TestCaseIndexDto.class).metadata();
        assertThat(actualMetadata.id()).isEqualTo(of(fakeMetadata.id()));
        assertThat(actualMetadata.creationDate()).isEqualTo(fakeMetadata.creationDate());
        assertThat(actualMetadata.updateDate()).isEqualTo(fakeMetadata.updateDate());
        assertThat(actualMetadata.title()).isEqualTo(fakeMetadata.title());
        assertThat(actualMetadata.description()).isEqualTo(of(fakeMetadata.description()));
    }

    @Test
    public void should_call_repository_to_get_all_scenarios_metadata() throws Exception {
        // Given
        String id = "1";
        TestCaseMetadata fakeMetadata = TestCaseMetadataImpl.builder().withId(id).build();
        when(testCaseRepository.findAll()).thenReturn(List.of(fakeMetadata));

        //When
        MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders.get("/api/scenario/v2/")
                .accept(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andReturn();

        //Then
        verify(testCaseRepository).findAll();

        GwtTestCaseMetadataDto actualMetadata = (om.readValue(mvcResult.getResponse().getContentAsString(), new TypeReference<List<TestCaseIndexDto>>() {
        })).get(0).metadata();
        assertThat(actualMetadata.id()).isEqualTo(of(fakeMetadata.id()));
        assertThat(actualMetadata.creationDate()).isEqualTo(fakeMetadata.creationDate());
        assertThat(actualMetadata.updateDate()).isEqualTo(fakeMetadata.updateDate());
        assertThat(actualMetadata.title()).isEqualTo(fakeMetadata.title());
        assertThat(actualMetadata.description()).isEqualTo(of(fakeMetadata.description()));
    }

    @Test
    public void should_call_repository_to_delete_scenario() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.delete("/api/scenario/v2/1")
                .accept(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(MockMvcResultMatchers.status().isOk());
        verify(testCaseRepository).removeById(eq("1"));
    }
}
