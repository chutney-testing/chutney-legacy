package com.chutneytesting.engine.domain.execution.engine.evaluation;

@SuppressWarnings("serial")
public class EvaluationException extends RuntimeException {

    EvaluationException(String message) {
        super(message);
    }

    EvaluationException(String message, Exception cause) {
        super(message, cause);
    }
}
