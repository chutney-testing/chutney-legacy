package com.chutneytesting.engine.domain.execution.engine.spelfunction;

import com.chutneytesting.engine.domain.execution.evaluation.SpelFunctionCallback;
import com.chutneytesting.engine.domain.execution.evaluation.SpelFunctions;
import com.chutneytesting.task.spi.SpelFunction;
import org.assertj.core.api.Assertions;
import com.chutneytesting.tools.ThrowingConsumer;
import org.junit.Test;
import org.springframework.util.ReflectionUtils;

public class SpelFunctionCallbackTest {

    static Object mockObject = new Object();

    static class TestClassWithNoArgument {
        @SpelFunction("testNoArgument")
        public static Object test() {
            return mockObject;
        }
    }

    private void assertTestClassWithNoArgument(SpelFunctions.NamedFunctionLink entry) throws Exception {
        Assertions.assertThat("testNoArgument").isEqualTo(entry.getName());
        Assertions.assertThat(TestClassWithNoArgument.class.getMethod("test")).isEqualToComparingFieldByField(entry.getMethod());
    }

    static class TestClassWithArguments {
        @SpelFunction("testWithArguments")
        public static Object testWithArguments(String t) {
            return t;
        }
    }

    private void assertTestClassWithArguments(SpelFunctions.NamedFunctionLink entry) throws Exception {
        Assertions.assertThat("testWithArguments").isEqualTo(entry.getName());
        Assertions.assertThat(TestClassWithArguments.class.getMethod("testWithArguments", String.class)).isEqualToComparingFieldByField(entry.getMethod());
    }

    static class TestClassMethodNameValue {
        @SpelFunction()
        public static Object testWithMethodNameValue(String t) {
            return t;
        }
    }

    private void assertTestClassMethodNameValue(SpelFunctions.NamedFunctionLink entry) throws Exception {
        Assertions.assertThat("testWithMethodNameValue").isEqualTo(entry.getName());
        Assertions.assertThat(TestClassMethodNameValue.class.getMethod("testWithMethodNameValue", String.class)).isEqualToComparingFieldByField(entry.getMethod());
    }

    @Test
    public void should_create_spelFunctions_containing_method_with_name() throws Exception {
        SpelFunctionCallback callback = new SpelFunctionCallback();
        ReflectionUtils.doWithMethods(TestClassWithNoArgument.class, callback);
        Assertions.assertThat(1L).isEqualTo(callback.getSpelFunctions().stream().count());
        assertTestClassWithNoArgument(callback.getSpelFunctions().stream().findFirst().get());
    }

    @Test
    public void should_create_spelFunctions_containing_method_with_name_and_arguments() throws Exception {
        SpelFunctionCallback callback = new SpelFunctionCallback();
        ReflectionUtils.doWithMethods(TestClassWithArguments.class, callback);
        Assertions.assertThat(1L).isEqualTo(callback.getSpelFunctions().stream().count());
        assertTestClassWithArguments(callback.getSpelFunctions().stream().findFirst().get());
    }

    @Test
    public void should_create_spelFunctions_containing_method_with_function_name_when_not_provided() throws Exception {
        SpelFunctionCallback callback = new SpelFunctionCallback();
        ReflectionUtils.doWithMethods(TestClassMethodNameValue.class, callback);
        Assertions.assertThat(1L).isEqualTo(callback.getSpelFunctions().stream().count());
        assertTestClassMethodNameValue(callback.getSpelFunctions().stream().findFirst().get());
    }

    @Test
    public void should_create_spelFunctions_containing_multiple_method_with_name_and_arguments() throws Exception {
        SpelFunctionCallback callback = new SpelFunctionCallback();
        ReflectionUtils.doWithMethods(TestClassWithArguments.class, callback);
        ReflectionUtils.doWithMethods(TestClassWithNoArgument.class, callback);
        ReflectionUtils.doWithMethods(TestClassMethodNameValue.class, callback);
        Assertions.assertThat(3L).isEqualTo(callback.getSpelFunctions().stream().count());
        callback.getSpelFunctions().stream()
            .filter(f -> f.getName() == "testNoArgument")
            .forEach(ThrowingConsumer.toUnchecked(f -> assertTestClassWithNoArgument(f)));
        callback.getSpelFunctions().stream()
            .filter(f -> f.getName() == "testWithArguments")
            .forEach(ThrowingConsumer.toUnchecked(f -> assertTestClassWithArguments(f)));
        callback.getSpelFunctions().stream()
            .filter(f -> f.getName() == "testWithMethodNameValue")
            .forEach(ThrowingConsumer.toUnchecked(f -> assertTestClassMethodNameValue(f)));
    }

    class TestNoStaticClass {
        @SpelFunction("testNoStatic")
        public Object testNoStatic() {
            return null;
        }
    }

    @Test(expected = IllegalStateException.class)
    public void should_throw_exception_if_method_not_static() throws Exception {
        SpelFunctionCallback callback = new SpelFunctionCallback();
        ReflectionUtils.doWithMethods(TestNoStaticClass.class, callback);
    }
}
