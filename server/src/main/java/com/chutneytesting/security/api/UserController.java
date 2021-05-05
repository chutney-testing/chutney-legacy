package com.chutneytesting.security.api;

import com.chutneytesting.security.infra.SpringUserService;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/user")
@CrossOrigin(origins = "*")
public class UserController {

    private final SpringUserService userService;

    public UserController(SpringUserService userService) {
        this.userService = userService;
    }

    @GetMapping(path = "", produces = MediaType.APPLICATION_JSON_VALUE)
    @SuppressWarnings("unused")
    public UserDto currentUser(HttpServletRequest request, HttpServletResponse response) {
        return userService.currentUser();
    }
}
