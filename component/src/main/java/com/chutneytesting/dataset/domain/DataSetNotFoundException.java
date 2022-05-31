package com.chutneytesting.dataset.domain;

public class DataSetNotFoundException extends RuntimeException {
    public DataSetNotFoundException(String id) {
        super("Dataset [" + id + "] could not be found");
    }
}
