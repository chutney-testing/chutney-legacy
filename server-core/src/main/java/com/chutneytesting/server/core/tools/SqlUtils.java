package com.chutneytesting.server.core.tools;

public final class SqlUtils {

    public static String count(String query) {
        return "SELECT count(*) as count FROM (" + query + ")";
    }
}
