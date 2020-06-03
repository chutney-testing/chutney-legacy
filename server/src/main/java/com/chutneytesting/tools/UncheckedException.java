package com.chutneytesting.tools;

import com.chutneytesting.tools.ThrowingFunction;

/**
 * Specific {@link RuntimeException} thrown when checked {@link Exception} occurs in <b>Throwing</b>Functions.<br/>
 * Checked {@link Exception} is set as cause.
 *
 * @see ThrowingFunction#toUnchecked(ThrowingFunction)
 */
@SuppressWarnings("serial")
public class UncheckedException extends RuntimeException {

    private UncheckedException(Exception checkedException) {
        super("Occurred in silenced function", checkedException);
    }

    public static RuntimeException throwUncheckedException(Exception e) {
        if (e instanceof RuntimeException) {
            return (RuntimeException) e;
        } else {
            return new UncheckedException(e);
        }
    }
}
