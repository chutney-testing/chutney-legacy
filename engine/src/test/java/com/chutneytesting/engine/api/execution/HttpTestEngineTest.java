package com.chutneytesting.engine.api.execution;

import static com.chutneytesting.engine.api.execution.HttpTestEngine.EXECUTION_URL;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.chutneytesting.engine.domain.execution.report.Status;
import com.chutneytesting.engine.domain.execution.report.StepExecutionReport;
import com.chutneytesting.engine.domain.execution.report.StepExecutionReportBuilder;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import java.io.IOException;
import java.time.Instant;
import org.hamcrest.CoreMatchers;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

public class HttpTestEngineTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(HttpTestEngineTest.class);

    @Test
    public void controller_maps_anemic_request_and_call_engine() throws Exception {
        TestEngine engine = mock(TestEngine.class);
        MappingJackson2HttpMessageConverter mappingJackson2HttpMessageConverter = new MappingJackson2HttpMessageConverter();
        mappingJackson2HttpMessageConverter.setObjectMapper(new ObjectMapper().findAndRegisterModules());

        MockMvc mvc = MockMvcBuilders
            .standaloneSetup(new HttpTestEngine(engine))
            .setMessageConverters(mappingJackson2HttpMessageConverter)
            .build();

        StepExecutionReport report = new StepExecutionReportBuilder()
            .setName("test")
            .setDuration(2L)
            .setStartDate(Instant.now())
            .setStatus(Status.SUCCESS)
            .setType("taskType")
            .setStrategy("strategy")
            .setTargetName("targetName")
            .setTargetUrl("targetUrl")
            .createStepExecutionReport();

        when(engine.execute(any()))
            .thenReturn(StepExecutionReportMapper.toDto(report));

        ExecutionRequestDto executionRequestDto = Jsons.loadJsonFromClasspath("scenarios_examples/scenario_sample_1.json", ExecutionRequestDto.class);
        String body = Jsons.objectMapper().writeValueAsString(executionRequestDto);

        mvc
            .perform(MockMvcRequestBuilders
                .post(EXECUTION_URL)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(body)
            )
            .andDo(result -> LOGGER.info(result.getResponse().getContentAsString()))
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andExpect(MockMvcResultMatchers.jsonPath("$.duration", CoreMatchers.equalTo(2)))
            .andExpect(MockMvcResultMatchers.jsonPath("$.status", CoreMatchers.equalTo(Status.SUCCESS.name())))
            .andExpect(MockMvcResultMatchers.jsonPath("$.type", CoreMatchers.equalTo("taskType")))
            .andExpect(MockMvcResultMatchers.jsonPath("$.strategy", CoreMatchers.equalTo("strategy")))
            .andExpect(MockMvcResultMatchers.jsonPath("$.targetName", CoreMatchers.equalTo("targetName")))
            .andExpect(MockMvcResultMatchers.jsonPath("$.targetUrl", CoreMatchers.equalTo("targetUrl")))
        ;

        verify(engine, times(1)).execute(any());
    }

    private static final class Jsons {
        private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper().findAndRegisterModules();

        private Jsons() {
        }

        static <T> T loadJsonFromClasspath(String path, Class<T> targetClass) {
            try {
                return OBJECT_MAPPER.readValue(Jsons.class.getClassLoader().getResourceAsStream(path), targetClass);
            } catch (IOException e) {
                throw new IllegalArgumentException("Cannot deserialize " + path + " to " + targetClass.getSimpleName(), e);
            }
        }

        static ObjectMapper objectMapper() {
            return OBJECT_MAPPER;
        }
    }

    @Test
    public void method_should_not_be_implemented_for_remote() {
        TestEngine engine = mock(TestEngine.class);
        HttpTestEngine httpTestEngine = new HttpTestEngine(engine);
        assertThatExceptionOfType(IllegalArgumentException.class).isThrownBy(() -> httpTestEngine.executeAsync(null));
        assertThatExceptionOfType(IllegalArgumentException.class).isThrownBy(() -> httpTestEngine.pauseExecution(null));
        assertThatExceptionOfType(IllegalArgumentException.class).isThrownBy(() -> httpTestEngine.resumeExecution(null));
        assertThatExceptionOfType(IllegalArgumentException.class).isThrownBy(() -> httpTestEngine.stopExecution(null));
        assertThatExceptionOfType(IllegalArgumentException.class).isThrownBy(() -> httpTestEngine.receiveNotification(null));
    }
}
