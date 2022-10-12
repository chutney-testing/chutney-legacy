package com.chutneytesting.action.sql.core;

public class NonOptimizedQueryException extends RuntimeException {

    public NonOptimizedQueryException() {
        super("Query fetched too many rows. Please try to refine your query.");
    }

}
