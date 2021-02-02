package com.chutneytesting.security.infra.handlers;

import java.io.IOException;
import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.security.web.session.InvalidSessionStrategy;

public class HttpStatusInvalidSessionStrategy implements InvalidSessionStrategy {

    private final HttpStatus httpStatus;
    private final Map<String, String> headers;
    private final boolean sessionCookieHttpOnly;
    private final boolean sessionCookieSecure;

    public HttpStatusInvalidSessionStrategy(HttpStatus httpStatus,
                                            Map<String, String> headers,
                                            boolean sessionCookieHttpOnly,
                                            boolean sessionCookieSecure) {
        this.httpStatus = httpStatus;
        this.headers = headers;
        this.sessionCookieHttpOnly = sessionCookieHttpOnly;
        this.sessionCookieSecure = sessionCookieSecure;
    }

    @Override
    public void onInvalidSessionDetected(HttpServletRequest request, HttpServletResponse response) throws IOException {
        removeInvalidSessionCookie(request, response);
        response.setStatus(httpStatus.value());
        headers.forEach(response::setHeader);
        response.getOutputStream().println("");
    }

    private void removeInvalidSessionCookie(HttpServletRequest request, HttpServletResponse response) {
        String sessionCookieName = request.getServletContext().getSessionCookieConfig().getName();
        Optional<Cookie> sessionCookie = Arrays.stream(request.getCookies()).filter(c -> sessionCookieName.equals(c.getName())).findFirst();
        sessionCookie.ifPresent(c -> {
            Cookie cookieToDelete = new Cookie(c.getName(), c.getValue());
            cookieToDelete.setMaxAge(0);
            cookieToDelete.setPath("/");
            cookieToDelete.setHttpOnly(sessionCookieHttpOnly);
            cookieToDelete.setSecure(sessionCookieSecure);
            response.addCookie(cookieToDelete);
        });
    }
}
