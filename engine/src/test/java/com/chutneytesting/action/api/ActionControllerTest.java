package com.chutneytesting.action.api;

import static com.chutneytesting.action.api.ActionController.BASE_URL;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.chutneytesting.action.api.ActionDto.InputsDto;
import com.google.common.collect.Lists;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.skyscreamer.jsonassert.JSONAssert;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

public class ActionControllerTest {

    private EmbeddedActionEngine embeddedActionEngine = mock(EmbeddedActionEngine.class);

    private ActionController sut;

    private MockMvc mockMvc;

    @BeforeEach
    public void setUp() {
        sut = new ActionController(embeddedActionEngine);
        mockMvc = MockMvcBuilders.standaloneSetup(sut).build();
    }

    @Test
    public void should_return_empty_list_when_list_actions_with_no_actions_exist() throws Exception {
        // Given
        when(embeddedActionEngine.getAllActions())
            .thenReturn(Collections.emptyList());
        String expectedResponse = "[]";

        // When
        MvcResult mvcResult = mockMvc.perform(get(BASE_URL))
            .andExpect(status().isOk())
            .andReturn();

        // Then
        verify(embeddedActionEngine).getAllActions();
        JSONAssert.assertEquals(expectedResponse, mvcResult.getResponse().getContentAsString(), false);
    }

    @Test
    public void should_map_one_action_template_when_list_actions() throws Exception {
        // Given
        final String TASK_ID = "action-id";
        final String INPUT_NAME = "inputName";
        List<InputsDto> inputs = Lists.newArrayList(new InputsDto(INPUT_NAME, String.class));
        ActionDto actionDto = new ActionDto(TASK_ID, false, inputs);
        when(embeddedActionEngine.getAllActions())
            .thenReturn(Collections.singletonList(actionDto));
        String expectedResponse = "[ { 'identifier' : '" + TASK_ID + "', 'inputs': [ { 'name': '" + INPUT_NAME + "', 'type': 'java.lang.String' } ] } ]";

        // When
        MvcResult mvcResult = mockMvc.perform(get(BASE_URL))
            .andExpect(status().isOk())
            .andReturn();

        // Then
        verify(embeddedActionEngine).getAllActions();
        JSONAssert.assertEquals(expectedResponse, mvcResult.getResponse().getContentAsString(), false);
    }

    @Test
    public void should_find_action_template_by_id_when_asked_for() throws Exception {
        // Given
        final String TASK_ID = "action-id";
        final String INPUT_NAME = "inputName";
        List<InputsDto> inputs = Lists.newArrayList(new InputsDto(INPUT_NAME, String.class));
        ActionDto actionDto = new ActionDto(TASK_ID, false, inputs);

        when(embeddedActionEngine.getAction(TASK_ID))
            .thenReturn(Optional.of(actionDto));
        String expectedResponse = "{ 'identifier' : '" + TASK_ID + "', 'inputs': [ { 'name': '" + INPUT_NAME + "', 'type': 'java.lang.String' } ] }";

        // When
        MvcResult mvcResult = mockMvc.perform(get(BASE_URL + "/" + TASK_ID))
            .andExpect(status().isOk())
            .andReturn();

        // Then
        verify(embeddedActionEngine).getAction(TASK_ID);
        JSONAssert.assertEquals(expectedResponse, mvcResult.getResponse().getContentAsString(), false);
    }

    @Test
    public void should_return_404_when_aske_for_not_existing_action() throws Exception {
        // Given
        when(embeddedActionEngine.getAction(any()))
            .thenReturn(Optional.empty());

        // When
        final AtomicInteger resultContentLength = new AtomicInteger();
        mockMvc.perform(get("/api/action/unknownActionId/v1"))
            .andDo(result -> resultContentLength.set(result.getResponse().getContentLength()))
            .andExpect(status().isNotFound());

        // Then
        assertThat(resultContentLength.get()).isZero();
    }
}
