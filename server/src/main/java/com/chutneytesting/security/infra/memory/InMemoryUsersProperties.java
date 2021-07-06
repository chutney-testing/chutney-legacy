package com.chutneytesting.security.infra.memory;

import com.chutneytesting.security.api.UserDto;
import java.util.ArrayList;
import java.util.List;

public class InMemoryUsersProperties {

    private List<UserDto> users = new ArrayList<>();

    public List<UserDto> getUsers() {
        return users;
    }

    public void setUsers(List<UserDto> users) {
        this.users = users;
    }
}
