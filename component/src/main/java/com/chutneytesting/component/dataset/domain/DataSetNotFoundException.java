package com.chutneytesting.component.dataset.domain;

public class DataSetNotFoundException extends RuntimeException {
    public DataSetNotFoundException(String id) {
        super("Dataset [" + id + "] could not be found");
    }
}
