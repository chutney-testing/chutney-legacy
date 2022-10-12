package com.chutneytesting.action.spi;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Put this annotation on static method of class register in META-INF/extension/chutney.functions
 * Method will be available in SpEL expression, reference by value of annotation, <b>or default to methodName</b>
 *
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
public @interface SpelFunction {
    String value() default "";
}
