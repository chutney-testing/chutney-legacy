package com.chutneytesting.security.infra.handlers;

import java.io.IOException;
import java.util.Map;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.security.web.session.InvalidSessionStrategy;

public class HttpStatusInvalidSessionStrategy implements InvalidSessionStrategy {

    private final HttpStatus httpStatus;
    private final Map<String, String> headers;

    public HttpStatusInvalidSessionStrategy(HttpStatus unauthorized, Map<String, String> headers) {
        httpStatus = unauthorized;
        this.headers = headers;
    }

    @Override
    public void onInvalidSessionDetected(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        response.setStatus(httpStatus.value());
        headers.forEach(response::setHeader);
        response.getOutputStream().println("");
    }
}
