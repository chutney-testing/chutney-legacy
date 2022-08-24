package com.chutneytesting.component;

import static java.util.Optional.ofNullable;

public final class ComposableIdUtils {

    public static String toExternalId(String id) {
        if (isComposableDomainId(ofNullable(id).orElse(""))) {
            assert id != null;
            return id.replace("#", "").replace(":", "-");
        }
        return id;
    }

    public static String toInternalId(String id) {
        return ofNullable(id).map(s -> {
            if (isComposableFrontId(s)) {
                return "#" + s.replace("-", ":");
            }
            return s;
        }).orElse("");
    }

    public static boolean isComposableFrontId(String frontId) {
        return frontId.contains("-");
    }

    private static boolean isComposableDomainId(String testCaseId) {
        return testCaseId.contains("#") && testCaseId.contains(":");
    }
}
