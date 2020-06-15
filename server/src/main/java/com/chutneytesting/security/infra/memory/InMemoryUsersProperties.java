package com.chutneytesting.security.infra.memory;

import com.chutneytesting.security.domain.User;
import java.util.ArrayList;
import java.util.List;

public class InMemoryUsersProperties {

    private List<User> users = new ArrayList<>();

    public List<User> getUsers() {
        return users;
    }

    public void setUsers(List<User> users) {
        this.users = users;
    }
}
