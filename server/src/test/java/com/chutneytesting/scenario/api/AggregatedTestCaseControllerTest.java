package com.chutneytesting.scenario.api;


import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.chutneytesting.security.api.UserDto;
import com.chutneytesting.security.infra.SpringUserService;
import com.chutneytesting.server.core.domain.execution.history.ExecutionHistoryRepository;
import com.chutneytesting.server.core.domain.scenario.TestCaseRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

class AggregatedTestCaseControllerTest {

    private MockMvc mockMvc;
    private final ExecutionHistoryRepository executionHistoryRepository = mock(ExecutionHistoryRepository.class);
    private final TestCaseRepository testCaseRepository = mock(TestCaseRepository.class);

    @BeforeEach
    public void setUp() {
        AggregatedTestCaseController testCaseController = new AggregatedTestCaseController(testCaseRepository, executionHistoryRepository);
        mockMvc = MockMvcBuilders.standaloneSetup(testCaseController).build();
    }

    @Test
    public void should_delete_scenario_with_repository_when_delete_scenario() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.delete("/api/scenario/v2/1")
                .accept(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(MockMvcResultMatchers.status().isOk());
        verify(testCaseRepository).removeById(eq("1"));
    }
}
