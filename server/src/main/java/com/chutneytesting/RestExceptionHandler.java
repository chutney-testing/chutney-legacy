package com.chutneytesting;

import com.chutneytesting.admin.domain.BackupNotFoundException;
import com.chutneytesting.admin.domain.gitbackup.UnreachableRemoteException;
import com.chutneytesting.campaign.domain.CampaignNotFoundException;
import com.chutneytesting.dataset.domain.DataSetNotFoundException;
import com.chutneytesting.environment.domain.exception.AlreadyExistingEnvironmentException;
import com.chutneytesting.environment.domain.exception.AlreadyExistingTargetException;
import com.chutneytesting.environment.domain.exception.EnvironmentNotFoundException;
import com.chutneytesting.environment.domain.exception.InvalidEnvironmentNameException;
import com.chutneytesting.environment.domain.exception.TargetNotFoundException;
import com.chutneytesting.execution.domain.ScenarioConversionException;
import com.chutneytesting.execution.domain.campaign.CampaignAlreadyRunningException;
import com.chutneytesting.execution.domain.campaign.CampaignExecutionNotFoundException;
import com.chutneytesting.execution.domain.history.ReportNotFoundException;
import com.chutneytesting.execution.domain.scenario.FailedExecutionAttempt;
import com.chutneytesting.execution.domain.scenario.ScenarioNotRunningException;
import com.chutneytesting.globalvar.domain.GlobalVarNotFoundException;
import com.chutneytesting.instrument.domain.ChutneyMetrics;
import com.chutneytesting.scenario.domain.AlreadyExistingScenarioException;
import com.chutneytesting.scenario.domain.ComposableStepNotFoundException;
import com.chutneytesting.scenario.domain.ScenarioNotFoundException;
import com.chutneytesting.scenario.domain.ScenarioNotParsableException;
import com.chutneytesting.security.domain.CurrentUserNotFoundException;
import java.time.format.DateTimeParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageConversionException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.converter.HttpMessageNotWritableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@ControllerAdvice
public class RestExceptionHandler extends ResponseEntityExceptionHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(RestExceptionHandler.class);
    private final ChutneyMetrics metrics;

    public RestExceptionHandler(ChutneyMetrics metrics) {
        this.metrics = metrics;
    }

    @Override
    protected ResponseEntity<Object> handleHttpMessageNotReadable(HttpMessageNotReadableException ex, HttpHeaders headers, HttpStatus status, WebRequest request) {
        LOGGER.warn(ex.getMessage());
        metrics.onHttpError(status);
        return super.handleHttpMessageNotReadable(ex, headers, status, request);
    }

    @Override
    protected ResponseEntity<Object> handleHttpMessageNotWritable(HttpMessageNotWritableException ex, HttpHeaders headers, HttpStatus status, WebRequest request) {
        LOGGER.warn(ex.getMessage());
        metrics.onHttpError(status);
        return super.handleHttpMessageNotWritable(ex, headers, status, request);
    }

    @ExceptionHandler({
        RuntimeException.class,
        FailedExecutionAttempt.class
    })
    public ResponseEntity<Object> _500(RuntimeException ex, WebRequest request) {
        LOGGER.error("Controller global exception handler", ex);
        return handleExceptionInternalWithExceptionMessageAsBody(ex, HttpStatus.INTERNAL_SERVER_ERROR, request);
    }

    @ExceptionHandler({
        TargetNotFoundException.class,
        ScenarioNotFoundException.class,
        CampaignNotFoundException.class,
        CampaignExecutionNotFoundException.class,
        EnvironmentNotFoundException.class,
        CurrentUserNotFoundException.class,
        BackupNotFoundException.class,
        DataSetNotFoundException.class,
        ScenarioNotRunningException.class,
        GlobalVarNotFoundException.class,
        ReportNotFoundException.class,
        UnreachableRemoteException.class,
        ComposableStepNotFoundException.class // TODO should be in ComponentHandler
    })
    protected ResponseEntity<Object> notFound(RuntimeException ex, WebRequest request) {
        LOGGER.warn("Not found >> " + ex.getMessage());
        metrics.onHttpError(HttpStatus.NOT_FOUND);
        return handleExceptionInternalWithExceptionMessageAsBody(ex, HttpStatus.NOT_FOUND, request);
    }

    @ExceptionHandler({
        AlreadyExistingTargetException.class,
        AlreadyExistingEnvironmentException.class,
        CampaignAlreadyRunningException.class,
        AlreadyExistingScenarioException.class,
    })
    protected ResponseEntity<Object> conflict(RuntimeException ex, WebRequest request) {
        LOGGER.warn("Conflict >> " + ex.getMessage());
        metrics.onHttpError(HttpStatus.CONFLICT);
        return handleExceptionInternalWithExceptionMessageAsBody(ex, HttpStatus.CONFLICT, request);
    }

    @ExceptionHandler({
        ScenarioConversionException.class,
        ScenarioNotParsableException.class
    })
    protected ResponseEntity<Object> unprocessableEntity(RuntimeException ex, WebRequest request) {
        LOGGER.warn("Unprocessable Entity >> " + ex.getMessage());
        metrics.onHttpError(HttpStatus.UNPROCESSABLE_ENTITY);
        return handleExceptionInternalWithExceptionMessageAsBody(ex, HttpStatus.UNPROCESSABLE_ENTITY, request);
    }

    @ExceptionHandler({
        DateTimeParseException.class,
        InvalidEnvironmentNameException.class,
        HttpMessageConversionException.class
    })
    protected ResponseEntity<Object> badRequest(RuntimeException ex, WebRequest request) {
        LOGGER.warn("Bad Request >> " + ex.getMessage());
        metrics.onHttpError(HttpStatus.BAD_REQUEST);
        return handleExceptionInternalWithExceptionMessageAsBody(ex, HttpStatus.BAD_REQUEST, request);
    }

    @ExceptionHandler({
        IllegalArgumentException.class,
        AccessDeniedException.class
    })
    protected ResponseEntity<Object> forbidden(RuntimeException ex, WebRequest request) {
        LOGGER.warn("Forbidden >> " + ex.getMessage());
        metrics.onHttpError(HttpStatus.FORBIDDEN);
        return handleExceptionInternalWithExceptionMessageAsBody(ex, HttpStatus.FORBIDDEN, request);
    }

    private ResponseEntity<Object> handleExceptionInternalWithExceptionMessageAsBody(RuntimeException ex, HttpStatus status, WebRequest request) {
        String bodyOfResponse = ex.getMessage();
        return handleExceptionInternal(ex, bodyOfResponse, new HttpHeaders(), status, request);
    }
}
