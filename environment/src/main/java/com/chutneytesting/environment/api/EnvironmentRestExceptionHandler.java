/*
 * Copyright 2017-2023 Enedis
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.chutneytesting.environment.api;

import com.chutneytesting.environment.domain.exception.AlreadyExistingEnvironmentException;
import com.chutneytesting.environment.domain.exception.AlreadyExistingTargetException;
import com.chutneytesting.environment.domain.exception.EnvVariableNotFoundException;
import com.chutneytesting.environment.domain.exception.EnvironmentNotFoundException;
import com.chutneytesting.environment.domain.exception.InvalidEnvironmentNameException;
import com.chutneytesting.environment.domain.exception.TargetNotFoundException;
import com.chutneytesting.environment.domain.exception.VariableAlreadyExistingException;
import java.time.format.DateTimeParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.converter.HttpMessageNotWritableException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@ControllerAdvice
public class EnvironmentRestExceptionHandler extends ResponseEntityExceptionHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(EnvironmentRestExceptionHandler.class);

    @Override
    protected ResponseEntity<Object> handleHttpMessageNotReadable(HttpMessageNotReadableException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        LOGGER.warn(ex.getMessage());
        return super.handleHttpMessageNotReadable(ex, headers, status, request);
    }

    @Override
    protected ResponseEntity<Object> handleHttpMessageNotWritable(HttpMessageNotWritableException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {
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
        TargetNotFoundException.class,
        EnvironmentNotFoundException.class,
        EnvVariableNotFoundException.class
    })
    protected ResponseEntity<Object> notFound(RuntimeException ex, WebRequest request) {
        String bodyOfResponse = ex.getMessage();
        LOGGER.warn("Not found >> " + bodyOfResponse);
        return handleExceptionInternal(ex, bodyOfResponse, new HttpHeaders(), HttpStatus.NOT_FOUND, request);
    }

    @ExceptionHandler({
        AlreadyExistingTargetException.class,
        AlreadyExistingEnvironmentException.class,
        VariableAlreadyExistingException.class
    })
    protected ResponseEntity<Object> conflict(RuntimeException ex, WebRequest request) {
        String bodyOfResponse = ex.getMessage();
        LOGGER.warn("Conflict >> " + bodyOfResponse);
        return handleExceptionInternal(ex, bodyOfResponse, new HttpHeaders(), HttpStatus.CONFLICT, request);
    }

    @ExceptionHandler({
        DateTimeParseException.class,
        InvalidEnvironmentNameException.class
    })
    protected ResponseEntity<Object> badRequest(RuntimeException ex, WebRequest request) {
        String bodyOfResponse = ex.getMessage();
        LOGGER.warn("Bad request >> " + bodyOfResponse);
        return handleExceptionInternal(ex, bodyOfResponse, new HttpHeaders(), HttpStatus.BAD_REQUEST, request);
    }

    @ExceptionHandler({
        IllegalArgumentException.class
    })
    protected ResponseEntity<Object> forbidden(RuntimeException ex, WebRequest request) {
        String bodyOfResponse = ex.getMessage();
        LOGGER.warn("Forbidden >> " + bodyOfResponse);
        return handleExceptionInternal(ex, bodyOfResponse, new HttpHeaders(), HttpStatus.FORBIDDEN, request);
    }
}
