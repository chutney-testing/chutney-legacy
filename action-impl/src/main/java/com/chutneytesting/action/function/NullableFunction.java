package com.chutneytesting.action.function;

import com.chutneytesting.action.spi.SpelFunction;
import java.util.Optional;

public class NullableFunction {

    @SpelFunction
    public static Object nullable(Object input) {
        return Optional.ofNullable(input).orElse("null");
    }

}
