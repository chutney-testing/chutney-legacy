package com.chutneytesting;

import com.chutneytesting.instrument.domain.ChutneyMetrics;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.request.WebRequest;

class RestExceptionHandlerTest {

    ChutneyMetrics mockedMetrics = Mockito.mock(ChutneyMetrics.class);
    RuntimeException ex = Mockito.mock(RuntimeException.class);
    WebRequest request = Mockito.mock(WebRequest.class);
    RestExceptionHandler exceptionHandler = new RestExceptionHandler(mockedMetrics);
    private MockMvc mockMvc;

    @BeforeEach
    public void setup() {
        mockMvc = MockMvcBuilders.standaloneSetup(exceptionHandler).build();
    }

    @Test
    void should_return_not_found_status() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("https://localhost:8443/#/scenario/3/execution/last")
                .accept(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(MockMvcResultMatchers.status().isNotFound());

        exceptionHandler.notFound(ex, request);
        Mockito.verify(mockedMetrics).onHttpError(Mockito.eq(HttpStatus.NOT_FOUND));
    }

    @Test
    void should_return_bad_request_status() throws Exception {
        exceptionHandler.badRequest(ex, request);
        Mockito.verify(mockedMetrics).onHttpError(Mockito.eq(HttpStatus.BAD_REQUEST));
    }

    @Test
    void should_return_conflict_status() {
        exceptionHandler.conflict(ex, request);
        Mockito.verify(mockedMetrics).onHttpError(Mockito.eq(HttpStatus.CONFLICT));
    }

    @Test
    void should_return_unprocessable_entity() {
        exceptionHandler.unprocessableEntity(ex, request);
        Mockito.verify(mockedMetrics).onHttpError(Mockito.eq(HttpStatus.UNPROCESSABLE_ENTITY));
    }

    @Test
    void should_return_forbidden_status() throws Exception {
        exceptionHandler.forbidden(ex, request);
        Mockito.verify(mockedMetrics).onHttpError(Mockito.eq(HttpStatus.FORBIDDEN));
    }
}
