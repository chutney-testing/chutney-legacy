package com.chutneytesting.action.domain;

public class ResultOrError<RESULT, ERROR> {
    private final RESULT result;
    private final ERROR error;

    private ResultOrError(RESULT result, ERROR error) {
        this.result = result;
        this.error = error;
    }

    public boolean isError() {
        return error != null;
    }

    public boolean isOk() {
        return !isError();
    }

    public RESULT result() {
        return result;
    }

    public ERROR error() {
        return error;
    }

    public static <RESULT, ERROR> ResultOrError<RESULT, ERROR> result(RESULT result) {
        return new ResultOrError(result, null);
    }

    public static <RESULT, ERROR> ResultOrError<RESULT, ERROR> error(ERROR error) {
        return new ResultOrError(null, error);
    }
}
