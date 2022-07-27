package com.chutneytesting.component.scenario.api;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.chutneytesting.component.ComponentRestExceptionHandler;
import com.chutneytesting.component.scenario.api.dto.ImmutableComposableStepDto;
import com.chutneytesting.component.scenario.api.dto.ParentsStepDto;
import com.chutneytesting.component.scenario.domain.ComposableStep;
import com.chutneytesting.component.scenario.domain.ComposableStepNotFoundException;
import com.chutneytesting.component.scenario.domain.ComposableStepRepository;
import com.chutneytesting.component.scenario.domain.ParentStepId;
import com.chutneytesting.tests.OrientDatabaseHelperTest;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Arrays;
import java.util.Collections;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

public class StepControllerTest {

    private final ComposableStepRepository composableStepRepository = mock(ComposableStepRepository.class);

    private MockMvc mockMvc;
    private final ObjectMapper om = OrientDatabaseHelperTest.objectMapper();

    @BeforeEach
    public void setUp() {
        StepController sut = new StepController(composableStepRepository);
        mockMvc = MockMvcBuilders.standaloneSetup(sut)
            .setControllerAdvice(new ComponentRestExceptionHandler())
            .build();

        when(composableStepRepository.findById(any()))
            .thenReturn(ComposableStep.builder().build());
    }

    @Test
    public void should_call_step_repository_when_findById_called() throws Exception {
        // Given
        final String RECORD_ID = encodeRecordId("#2:7");
        // When
        mockMvc.perform(get(StepController.BASE_URL + "/" + RECORD_ID));

        // Then
        verify(composableStepRepository).findById(RECORD_ID); // MockMVC appears not to decode url string ...
    }

    @Test
    public void should_get_404_when_findById_find_nothing() throws Exception {
        // Given
        String recordId = "#2:9";
        final String RECORD_ID = encodeRecordId(recordId);
        when(composableStepRepository.findById(any())).thenThrow(new ComposableStepNotFoundException(recordId));
        // When
        String[] message = {null};
        mockMvc.perform(get(StepController.BASE_URL + "/" + RECORD_ID))
            .andDo(result -> message[0] = result.getResponse().getContentAsString())
            .andExpect(status().isNotFound());

        // Then
        assertThat(message[0]).isEqualToIgnoringCase("Composable step id [" + recordId + "] could not be found");
    }

    @Test
    public void should_not_call_mapping_when_findById_find_nothing() throws Exception {
        // Given
        String recordId = "#10:7";
        final String RECORD_ID = encodeRecordId(recordId);
        when(composableStepRepository.findById(any())).thenThrow(new ComposableStepNotFoundException(recordId));
        // When
        mockMvc.perform(get(StepController.BASE_URL + "/" + RECORD_ID))
            .andExpect(status().isNotFound());
    }

    @Test
    public void should_call_mapping_when_findById_return_func_steps() throws Exception {
        // Given
        final String RECORD_ID = encodeRecordId("#15:2");
        String FSTEP_NAME = "a functional step";
        final ComposableStep fStep = ComposableStep.builder().withName(FSTEP_NAME).build();
        when(composableStepRepository.findById(RECORD_ID))
            .thenReturn(fStep);

        // When
        mockMvc.perform(get(StepController.BASE_URL + "/" + RECORD_ID))
            .andExpect(status().isOk());
    }

    @Test
    public void should_call_mapping_when_findParents_return_parents_step() throws Exception {
        // Given
        when(composableStepRepository.findParents(any()))
            .thenReturn(
                Arrays.asList(
                    new ParentStepId("1-1", "Parent scenario", true),
                    new ParentStepId("2-2", "Parent step", false)
                )
            );
        // When
        MvcResult mvcResult = mockMvc.perform(get(StepController.BASE_URL + "/0-0/parents"))
            .andExpect(status().isOk())
            .andReturn();

        //Then
        ParentsStepDto result = om.readValue(mvcResult.getResponse().getContentAsString(), ParentsStepDto.class);
        assertThat(result.parentScenario()).hasSize(1);
        assertThat(result.parentSteps()).hasSize(1);
        assertThat(result.parentScenario().get(0).name()).isEqualTo("Parent scenario");
        assertThat(result.parentScenario().get(0).id()).isEqualTo("1-1");
        assertThat(result.parentSteps().get(0).name()).isEqualTo("Parent step");
        assertThat(result.parentSteps().get(0).id()).isEqualTo("2-2");
    }

    @Test
    public void should_save_func_step() throws Exception {
        // Given
        String newId = "12-3";
        when(composableStepRepository.save(any())).thenReturn(newId);

        // When
        MvcResult mvcResult = mockMvc.perform(
                post(StepController.BASE_URL, "")
                    .contentType(APPLICATION_JSON_VALUE)
                    .content(om.writeValueAsString(ImmutableComposableStepDto.builder().name("new component").build())))
            .andExpect(status().isOk())
            .andReturn();

        // Then
        assertThat(mvcResult.getResponse().getContentAsString()).isEqualTo("12-3");
    }

    private String encodeRecordId(String recordId) {
        try {
            return URLEncoder.encode(recordId, UTF_8.name());
        } catch (UnsupportedEncodingException e) {
            throw new IllegalArgumentException(e);
        }
    }

    private ComposableStep buildComposableStep(String name) {
        return ComposableStep.builder()
            .withName(name)
            .withSteps(Collections.emptyList())
            .build();
    }
}
