package com.chutneytesting.task.function;

import com.chutneytesting.task.spi.SpelFunction;

public class GenerateFunction {

    @SpelFunction
    public static Generate generate() {
        return new Generate();
    }
}
