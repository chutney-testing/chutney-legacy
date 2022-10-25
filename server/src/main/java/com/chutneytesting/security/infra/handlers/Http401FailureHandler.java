package com.chutneytesting.security.infra.handlers;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import net.minidev.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;

public class Http401FailureHandler implements AuthenticationFailureHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(Http401FailureHandler.class);

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response, AuthenticationException exception) throws IOException, ServletException {
        String username = request.getParameter("username");
        LOGGER.debug("Authentication failure for user [{}]", username);

        Map<String, String> message = new HashMap<>();
        message.put("message", extractMessageFromException(exception));

        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, "*");
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.getOutputStream().write(JSONObject.toJSONString(message).getBytes());
    }

    private String extractMessageFromException(Exception exception) {
        String msg;
        if (exception.getCause() != null) {
            msg = exception.getCause().getMessage();
        } else {
            msg = exception.getMessage();
        }
        return new String(msg.getBytes(StandardCharsets.UTF_8), StandardCharsets.UTF_8);
    }
}
