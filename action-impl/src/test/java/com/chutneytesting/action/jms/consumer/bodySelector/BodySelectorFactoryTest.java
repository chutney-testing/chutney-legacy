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

package com.chutneytesting.action.jms.consumer.bodySelector;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import com.chutneytesting.action.jms.consumer.bodySelector.BodySelector;
import com.chutneytesting.action.jms.consumer.bodySelector.BodySelectorFactory;
import org.junit.jupiter.api.Test;

public class BodySelectorFactoryTest {

    private final BodySelectorFactory bodySelectorFactory = new BodySelectorFactory();

    @Test
    public void can_build_selector_with_valid_syntax() {
        BodySelector bodySelector = bodySelectorFactory.build("XPATH 'boolean(/test)'");
        assertThat(bodySelector).isNotNull();
    }

    @Test
    public void building_selector_with_invalid_syntax_throws() {
        assertThatExceptionOfType(IllegalArgumentException.class)
            .isThrownBy(() -> bodySelectorFactory.build("test"))
            .withMessage("Wrong bodySelector syntax. Available syntaxes are:\n" +
                "\t- XPath selector: ^XPATH '(?<xpath>.+)'$");
    }
}
