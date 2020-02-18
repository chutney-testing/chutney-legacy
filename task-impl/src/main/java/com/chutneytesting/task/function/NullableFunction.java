package com.chutneytesting.task.function;

import com.chutneytesting.task.spi.SpelFunction;
import java.util.Optional;

public class NullableFunction {

    @SpelFunction
    public static Object nullable(Object input) {
        return Optional.ofNullable(input).orElse("null");
    }

}
