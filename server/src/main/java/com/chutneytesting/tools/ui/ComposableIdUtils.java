package com.chutneytesting.tools.ui;

import static java.util.Optional.ofNullable;

import java.util.Optional;

public final class ComposableIdUtils {

    public static String toFrontId(String id) {
        if (isComposableDomainId(ofNullable(id).orElse(""))) {
            return id.replace("#", "").replace(":", "-");
        }
        return id;
    }

    public static String fromFrontId(String id) {
        return fromFrontId(ofNullable(id));
    }

    public static String fromFrontId(Optional<String> id) {
        return id.map(s -> {
            if (isComposableFrontId(s)) {
                return "#" + s.replace("-", ":");
            }
            return s;
        }).orElse("");
    }

    public static boolean isComposableFrontId(String frontId) {
        return frontId.contains("-");
    }

    public static boolean isComposableDomainId(String testCaseId) {
        return testCaseId.contains("#") && testCaseId.contains(":");
    }
}
