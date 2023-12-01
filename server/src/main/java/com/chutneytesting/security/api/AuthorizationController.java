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

import com.chutneytesting.security.domain.Authorizations;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/authorizations")
@CrossOrigin(origins = "*")
public class AuthorizationController {

    private final Authorizations authorizations;

    public AuthorizationController(Authorizations authorizations) {
        this.authorizations = authorizations;
    }

    @PreAuthorize("hasAuthority('ADMIN_ACCESS')")
    @PostMapping(path = "", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public void save(@RequestBody AuthorizationsDto authorizations) {
        this.authorizations.save(AuthorizationMapper.fromDto(authorizations));
    }

    @PreAuthorize("hasAuthority('ADMIN_ACCESS')")
    @GetMapping(path = "", produces = MediaType.APPLICATION_JSON_VALUE)
    public AuthorizationsDto read() {
        return AuthorizationMapper.toDto(authorizations.read());
    }
}
