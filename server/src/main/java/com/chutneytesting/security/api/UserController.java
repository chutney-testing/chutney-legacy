package com.chutneytesting.security.api;

import com.chutneytesting.security.domain.CurrentUserNotFound;
import java.util.Optional;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/user")
@CrossOrigin(origins = "*")
public class UserController {

    @PostMapping(path="", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @SuppressWarnings("unused")
    public User currentUser(HttpServletRequest request, HttpServletResponse response) {
        final Optional<Authentication> authentication = Optional.ofNullable(SecurityContextHolder.getContext().getAuthentication());
        return (User) authentication
                        .orElseThrow(CurrentUserNotFound::new)
                        .getPrincipal();
    }

}
