package com.chutneytesting.engine.domain.execution.evaluation;

import com.chutneytesting.task.spi.SpelFunction;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import org.springframework.util.ReflectionUtils;

/**
 * Spring ReflectionUtils.MethodCallback use to find all method from specified class with @{@link SpelFunction} annotation.
 *
 * This class produce a SpelFunctions who contains all Method mark with {@link SpelFunction} declared on class from
 * ReflectionUtils.doWithMethods
 */
public class SpelFunctionCallback implements ReflectionUtils.MethodCallback {
    private final SpelFunctions spelFunctions = new SpelFunctions();

    @Override
    public void doWith(Method method) throws IllegalArgumentException {
        if (!method.isAnnotationPresent(SpelFunction.class)) {
            return;
        }
        SpelFunction spelFunction = method.getDeclaredAnnotation(SpelFunction.class);
        if(!Modifier.isStatic(method.getModifiers())) {
            throw new IllegalStateException("Given function " + spelFunction.value() + " (" + method.getName() + ") must match a static method");
        }
        spelFunctions.add(spelFunction, method);
    }

    public SpelFunctions getSpelFunctions() {
        return spelFunctions;
    }
}
