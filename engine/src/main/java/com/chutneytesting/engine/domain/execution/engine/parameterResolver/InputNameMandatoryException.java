package com.chutneytesting.engine.domain.execution.engine.parameterResolver;

@SuppressWarnings("serial")
class InputNameMandatoryException extends RuntimeException {

    public InputNameMandatoryException() {
        super("Input name is always mandatory");
    }
}
