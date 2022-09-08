package com.chutneytesting.action.function;

import com.chutneytesting.action.spi.SpelFunction;

public class GenerateFunction {

    @SpelFunction
    public static Generate generate() {
        return new Generate();
    }
}
