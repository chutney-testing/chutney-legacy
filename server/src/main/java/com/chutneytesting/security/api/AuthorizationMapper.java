package com.chutneytesting.security.api;

import static java.util.stream.Collectors.toList;

import com.chutneytesting.server.core.security.UserRoles;
import com.chutneytesting.server.core.security.Authorization;
import com.chutneytesting.server.core.security.Role;
import com.chutneytesting.server.core.security.User;
import java.util.List;
import java.util.stream.Stream;
import org.apache.commons.lang3.tuple.Pair;

public final class AuthorizationMapper {

    public static AuthorizationsDto toDto(UserRoles userRoles) {
        AuthorizationsDto dto = new AuthorizationsDto();
        dto.setRoles(userRoles.roles().stream().map(AuthorizationMapper::roleToDto).collect(toList()));
        dto.setAuthorizations(AuthorizationMapper.userRolesToDto(userRoles));
        return dto;
    }

    public static UserRoles fromDto(AuthorizationsDto dto) {
        List<Role> roles = dto.getRoles().stream().map(AuthorizationMapper::roleFromDto).collect(toList());
        List<User> users = dto.getAuthorizations().stream().flatMap(AuthorizationMapper::usersFromDto).collect(toList());
        return UserRoles.builder()
            .withRoles(roles)
            .withUsers(users)
            .build();
    }

    private static AuthorizationsDto.RoleDto roleToDto(Role role) {
        AuthorizationsDto.RoleDto dto = new AuthorizationsDto.RoleDto();
        dto.setName(role.name);
        dto.setRights(role.authorizations.stream().map(Authorization::name).collect(toList()));
        return dto;
    }

    private static List<AuthorizationsDto.RoleUsersDto> userRolesToDto(UserRoles userRoles) {
        return userRoles.roles().stream()
            .map(r -> Pair.of(
                r,
                userRoles.usersByRole(r).stream().map(u -> u.id).collect(toList())
            ))
            .filter(p -> !p.getRight().isEmpty())
            .map(p -> {
                AuthorizationsDto.RoleUsersDto dto = new AuthorizationsDto.RoleUsersDto();
                dto.setName(p.getLeft().name);
                dto.setUsers(p.getRight());
                return dto;
            })
            .collect(toList());
    }

    private static Role roleFromDto(AuthorizationsDto.RoleDto roleDto) {
        return Role.builder()
            .withName(roleDto.getName())
            .withAuthorizations(roleDto.getRights())
            .build();
    }

    private static Stream<User> usersFromDto(AuthorizationsDto.RoleUsersDto roleUsersDto) {
        return roleUsersDto.getUsers().stream()
            .map(id -> User.builder()
                .withId(id)
                .withRole(roleUsersDto.getName())
                .build()
            );
    }
}
