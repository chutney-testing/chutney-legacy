package com.chutneytesting.admin.domain;

import com.fasterxml.jackson.annotation.JsonCreator;

public class HomePage {
    public final String content;

    @JsonCreator
    public HomePage(String content) {
        this.content = content;
    }
}
