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

    @Test
    void generate_id_with_prefix() {
        assertThat(new Generate().id("prefix-", 1)).matches("^prefix-\\w$");
    }

    @Test
    void generate_id_with_suffix() {
        assertThat(new Generate().id(1, "-suffix")).matches("^\\w-suffix$");
    }

    @Test
    void generate_id_with_prefix_suffix_and_given_length() {
        assertThat(new Generate().id("pre-", 5, "-suf")).matches("^pre-\\w{5}-suf$");
    }
}
