package com.chutneytesting.engine.domain.environment;

import com.google.common.collect.Maps;

public final class NoTarget {

    public static final Target NO_TARGET = ImmutableTarget.builder()
        .id(Target.TargetId.of(""))
        .url("")
        .properties(Maps.newHashMap())
        .build();

    private NoTarget() {
    }
}
