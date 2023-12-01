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
