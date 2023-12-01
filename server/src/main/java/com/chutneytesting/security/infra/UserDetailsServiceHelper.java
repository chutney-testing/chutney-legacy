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

package com.chutneytesting.security.infra;

import com.chutneytesting.security.api.UserDto;
import com.chutneytesting.security.domain.AuthenticationService;
import com.chutneytesting.server.core.domain.security.Authorization;
import com.chutneytesting.server.core.domain.security.Role;
import com.chutneytesting.server.core.domain.security.RoleNotFoundException;
import java.util.Arrays;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class UserDetailsServiceHelper {

    private static final Logger LOGGER = LoggerFactory.getLogger(UserDetailsServiceHelper.class);

    private UserDetailsServiceHelper() {
    }

    public static UserDto grantAuthoritiesFromUserRole(UserDto userDto, AuthenticationService authenticationService) {
        UserDto dto = new UserDto(userDto);

        if (dto.getRoles().stream().anyMatch("admin"::equalsIgnoreCase)) {
            Arrays.stream(Authorization.values()).map(Authorization::name).forEach(dto::grantAuthority);
        } else {
            try {
                Role role = authenticationService.userRoleById(dto.getId());
                dto.addRole(role.name);
                role.authorizations.stream().map(Enum::name).forEach(dto::grantAuthority);
            } catch (RoleNotFoundException rnfe) {
                LOGGER.warn("User {} has no role defined", dto.getId());
                throw new NoRoleUserException(rnfe);
            }
        }

        return dto;
    }
}
