package com.chutneytesting.server.core.domain.feature;

public interface Feature {

    String name();
    default boolean active() {return true;}
}
