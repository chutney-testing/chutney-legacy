package com.chutneytesting.server.core.domain.dataset;

public class DataSetAlreadyExistException  extends RuntimeException {
    public DataSetAlreadyExistException(String name) {
        super("Dataset [" + name + "] already exists");
    }
}
