/*
 * Copyright 2017-2023 Enedis
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.chutneytesting.action.spi.validation;

import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.data.Index.atIndex;

import java.util.List;
import java.util.Objects;
import org.assertj.core.api.Condition;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
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

    @ParameterizedTest()
    @MethodSource("parametersForShould_validate_string_as_enum")
    void should_validate_string_as_enum(String input, boolean expected, int numberOfErrors) {
        Validator<String> validateAString = ActionValidatorsUtils.enumValidation(InnerEnum.class, input, "label");
        assertThat(validateAString.isValid()).isEqualTo(expected);
        assertThat(validateAString.getErrors().size()).isEqualTo(numberOfErrors);
    }

    private enum InnerEnum {ONE, TWO, THREE}

    public static Object[] parametersForShould_validate_string_as_enum() {
        return new Object[][]{
            {"ONE", true, 0},
            {"FOUR", false, 1}
        };
    }

    @Test
    void TODO_should_not_say_failing_validation_is_not_applied_when_validation_consists_of_exception_checking() {
        Validator<List<Object>> validation = Validator.of(emptyList())
            .validate(l -> l.get(1), noException -> true, "validation message");

        Condition<String> doesNotHaveNotAppliedMessage = new Condition<>(
            s -> !s.contains("not applied"),
            "Should not contains 'not applied'"
        );

        // TODO - Does not like the current behavior
        Assertions.assertThrows(AssertionError.class,
            () -> assertThat(validation.getErrors()).has(doesNotHaveNotAppliedMessage, atIndex(0))
        );
    }
}
