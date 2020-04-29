package com.chutneytesting.tools.ui;

import java.util.Optional;
import org.springframework.lang.NonNull;

public final class OrientUtils {

    public static String toFrontId(@NonNull String id) {
        if (isOrientId(id)) {
            return id.replace("#", "").replace(":", "-");
        }
        return id;
    }

    public static String fromFrontId(String id) {
        return fromFrontId(Optional.of(id));
    }

    public static String fromFrontId(Optional<String> id) {
        return id.map(s -> {
            if (isOrientFrontId(s)) {
                return "#" + s.replace("-", ":");
            }
            return s;
        }).orElse("");
    }

    public static boolean isOrientFrontId(String frontId) {
        return frontId.contains("-");
    }

    public static boolean isOrientId(String testCaseId) {
        return testCaseId.contains("#") && testCaseId.contains(":");
    }
}
