package com.chutneytesting.task.spi.validation;


import static org.assertj.core.api.Assertions.assertThat;

import java.util.Objects;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

class ValidatorTest {

    @ParameterizedTest()
    @MethodSource("parametersForShould_validate_string")
    void should_validate_string(String input, boolean expected, int numberOfErrors) {
        Validator<String> validateAString = Validator.of(input)
            .validate(Objects::nonNull, "should not be null")
            .validate(s -> !s.isEmpty(), "should not be empty")
            .validate(s -> s.equals("valid string"), "should be equal to 'valid string'");
        assertThat(validateAString.isValid()).isEqualTo(expected);
        assertThat(validateAString.getErrors().size()).isEqualTo(numberOfErrors);
    }

    public static Object[] parametersForShould_validate_string() {
        return new Object[][]{
            {"valid string", true, 0},
            {null, false, 3},
            {"", false, 2},
            {"not the good one", false, 1},
        };
    }

    @ParameterizedTest()
    @MethodSource("parametersForShould_validate_pojo")
    void should_validate_pojo(Person input, boolean expected, int numberOfErrors) {
        Validator<Person> validateAPerson = Validator.of(input)
            .validate(Objects::nonNull, "should not be null")
            .validate(Person::getName, n -> n.startsWith("P"), "should start with a P")
            .validate(Person::getAge, a -> a >= 18, "should be eighteen or over");
        assertThat(validateAPerson.isValid()).isEqualTo(expected);
        assertThat(validateAPerson.getErrors().size()).isEqualTo(numberOfErrors);
    }

    public static Object[] parametersForShould_validate_pojo() {
        return new Object[][]{
            {new Person("Pierre", 38), true, 0},
            {new Person("Paul", 9), false, 1},
            {new Person("Jacques", 35), false, 1},
            {new Person("Jak", 6), false, 2},
            {null, false, 3},
        };
    }

    private static class Person {
        private String name;
        private Integer age;

        public Person(String name, Integer age) {
            this.name = name;
            this.age = age;
        }

        public String getName() {
            return name;
        }

        public Integer getAge() {
            return age;
        }
    }
}
