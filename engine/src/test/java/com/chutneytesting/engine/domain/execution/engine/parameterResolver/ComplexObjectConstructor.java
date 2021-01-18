package com.chutneytesting.engine.domain.execution.engine.parameterResolver;

import com.chutneytesting.task.spi.injectable.Input;

class ComplexObjectConstructor {

    private SimpleObject aSimpleObject;

    public ComplexObjectConstructor(@Input("simple-object-name") SimpleObject aSimpleObject) {
        this.aSimpleObject = aSimpleObject;
    }
}
