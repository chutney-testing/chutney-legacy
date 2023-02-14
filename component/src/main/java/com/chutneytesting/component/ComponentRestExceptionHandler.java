package com.chutneytesting.component;

import com.chutneytesting.component.scenario.domain.AlreadyExistingComposableStepException;
import com.chutneytesting.component.scenario.domain.ComposableStepCyclicDependencyException;
import com.chutneytesting.component.scenario.domain.ComposableStepNotFoundException;
import com.chutneytesting.server.core.domain.dataset.DataSetNotFoundException;
import com.chutneytesting.server.core.domain.scenario.ScenarioNotFoundException;
import java.time.format.DateTimeParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.converter.HttpMessageNotWritableException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@ControllerAdvice
public class ComponentRestExceptionHandler extends ResponseEntityExceptionHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(ComponentRestExceptionHandler.class);

    @Override
    protected ResponseEntity<Object> handleHttpMessageNotReadable(HttpMessageNotReadableException ex, HttpHeaders headers, HttpStatus status, WebRequest request) {
        LOGGER.warn(ex.getMessage());
        return super.handleHttpMessageNotReadable(ex, headers, status, request);
    }

    @Override
    protected ResponseEntity<Object> handleHttpMessageNotWritable(HttpMessageNotWritableException ex, HttpHeaders headers, HttpStatus status, WebRequest request) {
        LOGGER.warn(ex.getMessage());
        return super.handleHttpMessageNotWritable(ex, headers, status, request);
    }

    @ExceptionHandler({
        RuntimeException.class
    })
    public ResponseEntity<Object> _500(RuntimeException ex, WebRequest request) {
        LOGGER.error("Controller global exception handler", ex);
        String bodyOfResponse = ex.getMessage();
        return handleExceptionInternal(ex, bodyOfResponse, new HttpHeaders(), HttpStatus.INTERNAL_SERVER_ERROR, request);
    }

    @ExceptionHandler({
        ComposableStepNotFoundException.class,
        DataSetNotFoundException.class,
        ScenarioNotFoundException.class
    })
    protected ResponseEntity<Object> notFound(RuntimeException ex, WebRequest request) {
        String bodyOfResponse = ex.getMessage();
        LOGGER.warn("Not found >> " + ex.getMessage());
        return handleExceptionInternal(ex, bodyOfResponse, new HttpHeaders(), HttpStatus.NOT_FOUND, request);
    }

    @ExceptionHandler({
        AlreadyExistingComposableStepException.class,
        ComposableStepCyclicDependencyException.class
    })
    protected ResponseEntity<Object> conflict(RuntimeException ex, WebRequest request) {
        String bodyOfResponse = ex.getMessage();
        LOGGER.warn("Conflict >> " + ex.getMessage());
        return handleExceptionInternal(ex, bodyOfResponse, new HttpHeaders(), HttpStatus.CONFLICT, request);
    }

    @ExceptionHandler({
        DateTimeParseException.class,
    })
    protected ResponseEntity<Object> badRequest(RuntimeException ex, WebRequest request) {
        String bodyOfResponse = ex.getMessage();
        LOGGER.warn("Bad request >> " + ex.getMessage());
        return handleExceptionInternal(ex, bodyOfResponse, new HttpHeaders(), HttpStatus.BAD_REQUEST, request);
    }

    @ExceptionHandler({
        IllegalArgumentException.class
    })
    protected ResponseEntity<Object> forbidden(RuntimeException ex, WebRequest request) {
        String bodyOfResponse = ex.getMessage();
        LOGGER.warn("Forbidden >> " + ex.getMessage());
        return handleExceptionInternal(ex, bodyOfResponse, new HttpHeaders(), HttpStatus.FORBIDDEN, request);
    }
}
