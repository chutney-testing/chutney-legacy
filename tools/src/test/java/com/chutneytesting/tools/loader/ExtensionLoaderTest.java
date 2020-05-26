package com.chutneytesting.tools.loader;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

public class ExtensionLoaderTest {

    @Test
    public void loading_from_classpath_to_class_returns_a_set_of_classes() {
        ExtensionLoader<Class<?>> classExtensionLoader = ExtensionLoaders.classpathToClass("META-INF-TEST/class_line");

        assertThat(classExtensionLoader.load()).containsOnly(String.class, Integer.class);
    }
}
