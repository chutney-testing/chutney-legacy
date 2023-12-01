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

package com.chutneytesting.security.domain;

import com.chutneytesting.server.core.domain.security.Role;
import com.chutneytesting.server.core.domain.security.RoleNotFoundException;
import com.chutneytesting.server.core.domain.security.User;
import com.chutneytesting.server.core.domain.security.UserRoles;

public class AuthenticationService {

    private final Authorizations authorizations;

    public AuthenticationService(Authorizations authorizations) {
        this.authorizations = authorizations;
    }

    public Role userRoleById(String userId) {
        UserRoles userRoles = authorizations.read();
        User user = userRoles.userById(userId)
            .orElseThrow(() -> RoleNotFoundException.forUser(userId));
        return userRoles.roleByName(user.roleName);
    }
}
