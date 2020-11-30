package com.chutneytesting.engine.domain.execution.engine.parameterResolver;

import com.chutneytesting.task.spi.injectable.Input;

class SimpleObject {

    private String aString;
    private Integer aInteger;

    public SimpleObject(@Input("string-name") String aString, @Input("integer-name") Integer aInteger) {
        this.aString = aString;
        this.aInteger = aInteger;
    }
}
