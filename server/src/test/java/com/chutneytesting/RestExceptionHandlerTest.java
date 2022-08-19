package com.chutneytesting;

import static org.junit.jupiter.params.provider.Arguments.of;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpStatus.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.chutneytesting.environment.domain.exception.AlreadyExistingTargetException;
import com.chutneytesting.execution.domain.ScenarioConversionException;
import com.chutneytesting.instrument.domain.ChutneyMetrics;
import com.chutneytesting.scenario.api.GwtTestCaseController;
import com.chutneytesting.scenario.domain.ScenarioNotFoundException;
import com.chutneytesting.scenario.domain.TestCaseRepository;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConversionException;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultMatcher;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

class RestExceptionHandlerTest {

    private MockMvc mockMvc;
    private TestCaseRepository testCaseRepository = mock(TestCaseRepository.class);
    private ChutneyMetrics mockedMetrics = mock(ChutneyMetrics.class);

    @BeforeEach
    public void setup() {
        GwtTestCaseController testCaseController = new GwtTestCaseController(testCaseRepository, null, null);

        mockMvc = MockMvcBuilders
            .standaloneSetup(testCaseController)
            .setControllerAdvice(new RestExceptionHandler(mockedMetrics)).build();
    }

    public static List<Arguments> usernamePrivateKeyTargets() {
        return List.of(
            of(new ScenarioNotFoundException("12345"), NOT_FOUND, status().isNotFound()),
            of(new HttpMessageConversionException(""), BAD_REQUEST, status().isBadRequest()),
            of(new AlreadyExistingTargetException(""), CONFLICT, status().isConflict()),
            of(new ScenarioConversionException("", mock(Exception.class)), UNPROCESSABLE_ENTITY, status().isUnprocessableEntity()),
            of(new IllegalArgumentException(), FORBIDDEN, status().isForbidden())
        );
    }

    @ParameterizedTest
    @MethodSource("usernamePrivateKeyTargets")
    void should_return_not_found_status(RuntimeException exception, HttpStatus status, ResultMatcher statusMatcher) throws Exception {
        // Given
        when(testCaseRepository.findById("12345")).thenThrow(exception);

        // When
        mockMvc.perform(MockMvcRequestBuilders.get("/api/scenario/v2/12345")
                .accept(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(statusMatcher);

        //Then
        verify(mockedMetrics).onHttpError(eq(status));
    }
}
