package com.chutneytesting.design.api.scenario.compose;

import static com.chutneytesting.design.api.scenario.compose.StepController.FIND_STEPS_LIMIT_DEFAULT_VALUE;
import static com.chutneytesting.design.api.scenario.compose.StepController.FIND_STEPS_NAME_DEFAULT_VALUE;
import static com.chutneytesting.design.api.scenario.compose.StepController.FIND_STEPS_START_DEFAULT_VALUE;
import static com.chutneytesting.design.api.scenario.compose.StepController.FIND_STEPS_USAGE_DEFAULT_VALUE;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON_UTF8_VALUE;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.chutneytesting.RestExceptionHandler;
import com.chutneytesting.WebConfiguration;
import com.chutneytesting.design.api.scenario.compose.dto.ImmutableFunctionalStepDto;
import com.chutneytesting.design.api.scenario.compose.dto.ParentsStepDto;
import com.chutneytesting.design.domain.scenario.compose.FunctionalStep;
import com.chutneytesting.design.domain.scenario.compose.FunctionalStepNotFoundException;
import com.chutneytesting.design.domain.scenario.compose.ParentStepId;
import com.chutneytesting.design.domain.scenario.compose.StepRepository;
import com.chutneytesting.design.domain.scenario.compose.StepUsage;
import com.chutneytesting.tools.ImmutablePaginatedDto;
import com.chutneytesting.tools.ImmutablePaginationRequestParametersDto;
import com.chutneytesting.tools.ImmutableSortRequestParametersDto;
import com.chutneytesting.tools.PaginationRequestParametersDto;
import com.chutneytesting.tools.SortRequestParametersDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.MethodRule;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

public class StepControllerTest {

    @Rule
    public MethodRule mockitoRule = MockitoJUnit.rule();

    @Mock private StepRepository stepRepository;
    @InjectMocks private StepController sut;

    private MockMvc mockMvc;

    private final ObjectMapper om = new WebConfiguration().objectMapper();

    @Before
    public void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(sut)
                                 .setControllerAdvice(new RestExceptionHandler())
                                 .build();

        when(stepRepository.findById(any()))
            .thenReturn(FunctionalStep.builder().build());
        when(stepRepository.find(any(), any(), any()))
            .thenReturn(
                ImmutablePaginatedDto.<FunctionalStep>builder()
                    .totalCount(0)
                    .build());
    }

    @Test
    public void should_call_step_repository_when_findSteps_called() throws Exception {
        // When
        mockMvc.perform(get(StepController.BASE_URL))
            .andExpect(status().isOk());

        // Then
        verify(stepRepository).find(
            buildDefaultPaginationRequestParametersDto(),
            buildDefaultSortRequestParametersDto(),
            buildDefaultFunctionalStep());
    }

    @Test
    public void should_get_empty_response_when_findSteps_return_empty_list() throws Exception {
        // When
        final AtomicInteger resultContentLength = new AtomicInteger();
        mockMvc.perform(get(StepController.BASE_URL))
            .andDo(result -> resultContentLength.set(result.getResponse().getContentLength()))
            .andExpect(status().isOk());

        // Then
        assertThat(resultContentLength.get()).isZero();
    }

    @Test
    public void should_not_call_mapping_when_findSteps_return_empty_list() throws Exception {
        // When
        mockMvc.perform(get(StepController.BASE_URL))
            .andExpect(status().isOk());
    }

    @Test
    public void should_call_mapping_when_findSteps_return_func_steps() throws Exception {
        // Given
        String FSTEP_NAME = "a functional step";
        final List<FunctionalStep> fStepList = Arrays.asList(
            FunctionalStep.builder().withName(FSTEP_NAME).build(), FunctionalStep.builder().withName(FSTEP_NAME).build());
        when(stepRepository.find(
                buildPaginationRequestParametersDto(1, 100),
                buildSortRequestParametersDto("name", "name"),
                buildFunctionalStep("my name", StepUsage.GIVEN.name())))
            .thenReturn(
                ImmutablePaginatedDto.<FunctionalStep>builder()
                    .totalCount(2)
                    .addAllData(fStepList)
                    .build());

        // When
        mockMvc.perform(get(StepController.BASE_URL + "?start=1&limit=100&name=my name&usage=GIVEN&sort=name&desc=name"))
            .andExpect(status().isOk());
    }

    @Test
    public void should_call_step_repository_when_findById_called() throws Exception {
        // Given
        final String RECORD_ID = encodeRecordId("#2:7");
        // When
        mockMvc.perform(get(StepController.BASE_URL + "/" + RECORD_ID));

        // Then
        verify(stepRepository).findById(RECORD_ID); // MockMVC appears not to decode url string ...
    }

    @Test
    public void should_get_404_when_findById_find_nothing() throws Exception {
        // Given
        final String RECORD_ID = encodeRecordId("#2:9");
        when(stepRepository.findById(any())).thenThrow(new FunctionalStepNotFoundException());
        // When
        String[] message = { null };
        mockMvc.perform(get(StepController.BASE_URL + "/" + RECORD_ID))
            .andDo(result -> message[0] = result.getResponse().getContentAsString())
            .andExpect(status().isNotFound());

        // Then
        assertThat(message[0]).isEqualToIgnoringCase("The functional step id could not be found");
    }

    @Test
    public void should_not_call_mapping_when_findById_find_nothing() throws Exception {
        // Given
        final String RECORD_ID = encodeRecordId("#10:7");
        when(stepRepository.findById(any())).thenThrow(new FunctionalStepNotFoundException());
        // When
        mockMvc.perform(get(StepController.BASE_URL + "/" + RECORD_ID))
            .andExpect(status().isNotFound());
    }

    @Test
    public void should_call_mapping_when_findById_return_func_steps() throws Exception {
        // Given
        final String RECORD_ID = encodeRecordId("#15:2");
        String FSTEP_NAME = "a functional step";
        final FunctionalStep fStep = FunctionalStep.builder().withName(FSTEP_NAME).build();
        when(stepRepository.findById(RECORD_ID))
            .thenReturn(fStep);

        // When
        mockMvc.perform(get(StepController.BASE_URL + "/" + RECORD_ID))
            .andExpect(status().isOk());
    }

    @Test
    public void should_call_step_repository_when_findIdenticalStepsByName_called() throws Exception {
        // When
        mockMvc.perform(post(StepController.BASE_URL + "/search/name")
            .contentType(APPLICATION_JSON_UTF8_VALUE)
            .content("a functional step name"))
            .andExpect(status().isOk());

        // Then
        verify(stepRepository).queryByName("a functional step name");
    }

    @Test
    public void should_get_empty_response_when_findIdenticalStepsByName_return_empty_list() throws Exception {
        // When
        final AtomicInteger resultContentLength = new AtomicInteger();
        mockMvc.perform(post(StepController.BASE_URL + "/search/name")
            .contentType(APPLICATION_JSON_UTF8_VALUE)
            .content("no_name"))
            .andDo(result -> resultContentLength.set(result.getResponse().getContentLength()))
            .andExpect(status().isOk());

        // Then
        assertThat(resultContentLength.get()).isZero();
    }

    @Test
    public void should_not_call_mapping_when_findIdenticalStepsByName_return_empty_list() throws Exception {
        // When
        mockMvc.perform(post(StepController.BASE_URL + "/search/name")
            .contentType(APPLICATION_JSON_UTF8_VALUE)
            .content("no_name"))
            .andExpect(status().isOk());
    }

    @Test
    public void should_call_mapping_when_findIdenticalStepsByName_return_func_steps() throws Exception {
        // Given
        when(stepRepository.queryByName("functional step"))
            .thenReturn(
                Arrays.asList(
                    FunctionalStep.builder()
                        .withId("#-1:-1")
                        .withName("a functional step")
                        .build(),
                    FunctionalStep.builder()
                        .withId("#-1:-1")
                        .withName("another functional step")
                        .build()
                )
            );

        // When
        mockMvc.perform(post(StepController.BASE_URL + "/search/name")
            .contentType(APPLICATION_JSON_UTF8_VALUE)
            .content("functional step"))
            .andExpect(status().isOk());
    }

    @Test
    public void should_call_mapping_when_findParents_return_parents_step() throws Exception {
        // Given
        when(stepRepository.findParents(any()))
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
        String newId = "#12:3";
        when(stepRepository.save(any())).thenReturn(newId);

        // When
        MvcResult mvcResult = mockMvc.perform(
            post(StepController.BASE_URL, "")
                .contentType(APPLICATION_JSON_UTF8_VALUE)
                .content(om.writeValueAsString(ImmutableFunctionalStepDto.builder().name("new component").build())))
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

    private FunctionalStep buildDefaultFunctionalStep() {
        return buildFunctionalStep(FIND_STEPS_NAME_DEFAULT_VALUE, FIND_STEPS_USAGE_DEFAULT_VALUE);
    }

    private FunctionalStep buildFunctionalStep(String name, String usage) {
        return FunctionalStep.builder()
            .withName(name)
            .withUsage(StepUsage.fromName(usage))
            .withSteps(Collections.emptyList())
            .build();
    }

    private PaginationRequestParametersDto buildDefaultPaginationRequestParametersDto() {
        return buildPaginationRequestParametersDto(Long.valueOf(FIND_STEPS_START_DEFAULT_VALUE), Long.valueOf(FIND_STEPS_LIMIT_DEFAULT_VALUE));
    }

    private PaginationRequestParametersDto buildPaginationRequestParametersDto(long startElementIdx, long limit) {
        return ImmutablePaginationRequestParametersDto.builder()
            .start(startElementIdx)
            .limit(limit)
            .build();
    }

    private SortRequestParametersDto buildDefaultSortRequestParametersDto() {
        return buildSortRequestParametersDto(null, null);
    }

    private SortRequestParametersDto buildSortRequestParametersDto(String sort, String desc) {
        return ImmutableSortRequestParametersDto.builder()
            .sort(sort)
            .desc(desc)
            .build();
    }
}
