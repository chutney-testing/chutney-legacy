package com.chutneytesting.task.function;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

public class GenerateFunctionTest {

    @Test
    public void generate_function_produce_a_generate_object() {
        Generate generate = GenerateFunction.generate();
        assertThat(generate).isNotNull();
    }

    @Test
    public void generate_standard_uuid() {
        assertThat(new Generate().uuid()).matches("\\p{XDigit}{8}(?:-\\p{XDigit}{4}){3}-\\p{XDigit}{12}");
    }

    @Test
    public void generate_long() {
        assertThat(new Generate().randomLong()).matches("[-]?\\p{XDigit}+");
    }

}
