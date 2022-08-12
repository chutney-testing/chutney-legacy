package com.chutneytesting.scenario.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.chutneytesting.RestExceptionHandler;
import com.chutneytesting.campaign.domain.Campaign;
import com.chutneytesting.campaign.domain.CampaignExecutionReport;
import com.chutneytesting.execution.domain.history.ExecutionHistory;
import com.chutneytesting.instrument.domain.ChutneyMetrics;
import com.chutneytesting.scenario.api.raw.dto.ImmutableRawTestCaseDto;
import com.chutneytesting.scenario.api.raw.dto.RawTestCaseDto;
import com.chutneytesting.scenario.domain.gwt.GwtTestCase;
import com.chutneytesting.security.api.UserDto;
import com.chutneytesting.security.infra.SpringUserService;
import com.chutneytesting.server.core.domain.scenario.AggregatedRepository;
import com.chutneytesting.server.core.domain.scenario.ScenarioNotParsableException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Instant;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

public class TestCaseControllerTest {

    private final ObjectMapper om = new ObjectMapper().findAndRegisterModules();
    private static final RawTestCaseDto SAMPLE_SCENARIO = ImmutableRawTestCaseDto.builder()
        .title("test")
        .description("description test")
        .creationDate(Instant.now())
        .scenario("givens: [], when: {}, thens: []")
        .build();

    private MockMvc mockMvc;
    private final AggregatedRepository<GwtTestCase> testCaseRepository = mock(AggregatedRepository.class);
    private final SpringUserService userService = mock(SpringUserService.class);
    private final UserDto currentUser = new UserDto();

    @BeforeEach
    public void setUp() {
        currentUser.setId("currentUser");
        when(userService.currentUser()).thenReturn(currentUser);

        GwtTestCaseController testCaseController = new GwtTestCaseController(testCaseRepository, userService);
        mockMvc = MockMvcBuilders.standaloneSetup(testCaseController)
            .setControllerAdvice(new RestExceptionHandler(Mockito.mock(ChutneyMetrics.class)))
            .build();
    }

    @Test
    public void should_return_new_id_when_save_scenario() throws Exception {
        when(testCaseRepository.save(any(GwtTestCase.class))).thenReturn("1");

        AtomicReference<String> bodyHolder = new AtomicReference<>();
        // Save a scenario
        mockMvc.perform(
            MockMvcRequestBuilders.post("/api/scenario/v2/raw")
                .content(om.writeValueAsString(SAMPLE_SCENARIO))
                .contentType(MediaType.APPLICATION_JSON_VALUE)
        )
            .andDo(result -> bodyHolder.set(result.getResponse().getContentAsString()))
            .andExpect(MockMvcResultMatchers.status().isOk());

        Assertions.assertThat(bodyHolder.get()).isEqualTo("1");
    }

    @Test
    public void should_map_and_save_scenario_in_step_referential_when_save_scenario() throws Exception {
        when(testCaseRepository.save(any(GwtTestCase.class))).thenReturn("1");

        AtomicReference<String> bodyHolder = new AtomicReference<>();
        // Save a scenario
        mockMvc.perform(
            MockMvcRequestBuilders.post("/api/scenario/v2/raw")
                .content(om.writeValueAsString(SAMPLE_SCENARIO))
                .contentType(MediaType.APPLICATION_JSON_VALUE)
        )
//            .andDo(print())
            .andDo(result -> bodyHolder.set(result.getResponse().getContentAsString()))
            .andExpect(MockMvcResultMatchers.status().isOk());
    }

    @Test
    public void should_get_403_when_save_scenario_return_illegal_argument() throws Exception {
        // Given
        when(testCaseRepository.save(any(GwtTestCase.class))).thenThrow(new IllegalArgumentException());
        // When
        final AtomicInteger resultContentLength = new AtomicInteger();
        mockMvc.perform(
            MockMvcRequestBuilders.post("/api/scenario/v2/raw")
                .content(om.writeValueAsString(SAMPLE_SCENARIO))
                .contentType(MediaType.APPLICATION_JSON_VALUE)
        )
            .andDo(result -> resultContentLength.set(result.getResponse().getContentLength()))
            .andExpect(status().isForbidden());

        // Then
        assertThat(resultContentLength.get()).isZero();
    }

    @Test
    public void should_get_422_when_save_scenario_is_not_parsable() throws Exception {
        // Given
        when(testCaseRepository.save(any(GwtTestCase.class))).thenThrow(new ScenarioNotParsableException("a title", new RuntimeException()));
        // When
        String[] message = {null};
        mockMvc.perform(
            MockMvcRequestBuilders.post("/api/scenario/v2/raw")
                .content(om.writeValueAsString(SAMPLE_SCENARIO))
                .contentType(MediaType.APPLICATION_JSON_VALUE)
        )
            .andDo(result -> message[0] = result.getResponse().getContentAsString())
            .andExpect(status().isUnprocessableEntity());

        // Then
        assertThat(message[0]).isEqualToIgnoringCase("TestCase [a title] is not valid: null");
    }

    @Test
    public void should_save_scenario_when_referential_step_save_failed() throws Exception {
        when(testCaseRepository.save(any(GwtTestCase.class))).thenReturn("1");

        AtomicReference<String> bodyHolder = new AtomicReference<>();
        // Save a scenario
        mockMvc.perform(
            MockMvcRequestBuilders.post("/api/scenario/v2/raw")
                .content(om.writeValueAsString(SAMPLE_SCENARIO))
                .contentType(MediaType.APPLICATION_JSON_VALUE)
        )
//            .andDo(print())
            .andDo(result -> bodyHolder.set(result.getResponse().getContentAsString()))
            .andExpect(MockMvcResultMatchers.status().isOk());
    }

}
