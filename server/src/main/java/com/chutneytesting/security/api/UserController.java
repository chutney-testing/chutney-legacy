package com.chutneytesting.security.api;

import com.chutneytesting.security.domain.User;
import com.chutneytesting.security.domain.UserService;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/user")
@CrossOrigin(origins = "*")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping(path="", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @SuppressWarnings("unused")
    public User currentUser(HttpServletRequest request, HttpServletResponse response) {
        return userService.getCurrentUser();
    }

}
