package com.chutneytesting.engine.domain.environment;

import com.google.common.collect.Maps;

public final class NoTarget {

    public static final Target NO_TARGET = Target.builder()
        .withId(Target.TargetId.of(""))
        .withUrl("")
        .withProperties(Maps.newHashMap())
        .build();

    private NoTarget() {
    }
}
