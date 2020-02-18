package com.chutneytesting.task.api;

import com.chutneytesting.task.spi.injectable.Input;

public class ComplexParameterTestClass {

    private String firstParameter;
    private Integer secondParameter;

    public ComplexParameterTestClass(@Input("first") String firstParameter, @Input("second") Integer secondParameter) {
        this.firstParameter = firstParameter;
        this.secondParameter = secondParameter;
    }
}
