package com.chutneytesting.design.domain.dataset;

public class DataSetNotFoundException extends RuntimeException {
    public DataSetNotFoundException(String id) {
        super("Dataset [" + id + "] could not be found");
    }
}
