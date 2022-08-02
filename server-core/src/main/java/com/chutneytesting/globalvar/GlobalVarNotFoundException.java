package com.chutneytesting.globalvar;

public class GlobalVarNotFoundException extends RuntimeException {
    public GlobalVarNotFoundException(String id) {
        super("Global var group [" + id + "] could not be found");
    }
}
