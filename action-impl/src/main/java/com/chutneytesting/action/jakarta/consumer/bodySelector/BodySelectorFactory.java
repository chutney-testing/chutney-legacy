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

package com.chutneytesting.action.jakarta.consumer.bodySelector;

import java.util.Arrays;
import java.util.Optional;
import java.util.stream.Collectors;

public class BodySelectorFactory {

    private static final BodySelectorParser[] FACTORIES = new BodySelectorParser[]{
        new XpathBodySelectorParser()
    };

    /**
     * @throws IllegalArgumentException if the selector matches a parser but {@link BodySelector} cannot be built nonetheless
     */
    public BodySelector build(String selector) throws IllegalArgumentException {
        return Arrays
            .stream(FACTORIES)
            .map(factory -> factory.tryParse(selector))
            .filter(Optional::isPresent)
            .map(Optional::get)
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException("Wrong bodySelector syntax. " + syntaxDescription()));
    }

    public String syntaxDescription() {
        return "Available syntaxes are:\n\t- " +
            Arrays
                .stream(FACTORIES)
                .map(BodySelectorParser::description)
                .collect(Collectors.joining("\n\t- "));

    }
}
