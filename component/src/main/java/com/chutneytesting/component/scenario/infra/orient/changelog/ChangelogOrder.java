package com.chutneytesting.component.scenario.infra.orient.changelog;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
public @interface ChangelogOrder {
    int order();
    String uuid();
}
