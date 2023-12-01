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

package com.chutneytesting.security.api;

import com.chutneytesting.security.infra.SpringUserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(UserController.BASE_URL)
@CrossOrigin(origins = "*")
public class UserController {

    public static final String BASE_URL = "/api/v1/user";

    private final SpringUserService userService;

    public UserController(SpringUserService userService) {
        this.userService = userService;
    }

    @GetMapping(path = "", produces = MediaType.APPLICATION_JSON_VALUE)
    @SuppressWarnings("unused")
    public UserDto currentUser(HttpServletRequest request, HttpServletResponse response) {
        return userService.currentUser();
    }

    @PostMapping(path = "", produces = MediaType.APPLICATION_JSON_VALUE)
    @SuppressWarnings("unused")
    public UserDto loginForwardUser(HttpServletRequest request, HttpServletResponse response) {
        return this.currentUser(request, response);
    }
}
