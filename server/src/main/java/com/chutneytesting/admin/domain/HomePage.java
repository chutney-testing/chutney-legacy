package com.chutneytesting.admin.domain;

import com.fasterxml.jackson.annotation.JsonProperty;

public class HomePage {
    public final String content;

    public HomePage(@JsonProperty("content") String content) {
        this.content = content;
    }
}
