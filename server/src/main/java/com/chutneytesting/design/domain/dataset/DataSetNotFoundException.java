package com.chutneytesting.design.domain.dataset;

public class DataSetNotFoundException extends RuntimeException {
    public DataSetNotFoundException() {
        super("The dataset id could not be found");
    }
}
