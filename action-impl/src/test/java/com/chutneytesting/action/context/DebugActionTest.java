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

package com.chutneytesting.action.context;

import static org.assertj.core.api.Assertions.assertThat;

import com.chutneytesting.action.TestLogger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

public class DebugActionTest {

    private DebugAction sut;

    @Test
    public void should_log_all_inputs_by_default() {
        // G
        TestLogger logger = new TestLogger();
        Map<String, Object> inputs = new HashMap<>();
        inputs.put("my_first_input", "input_value");
        inputs.put("my_second_input", "input_value");
        inputs.put("my_third_input", "input_value");
        List<String> filter = Collections.emptyList();

        sut = new DebugAction(logger, inputs, filter);

        // W
        sut.execute();

        // T
        assertThat(logger.info).containsExactlyInAnyOrder(
            "my_first_input : [input_value]",
            "my_second_input : [input_value]",
            "my_third_input : [input_value]"
        );
    }

    @Test
    public void should_log_all_chosen_inputs_if_defined() {
        // G
        TestLogger logger = new TestLogger();
        Map<String, Object> inputs = new HashMap<>();
        inputs.put("my_first_input", "input_value");
        inputs.put("my_second_input", "input_value");
        inputs.put("my_third_input", "input_value");

        List<String> filter = new ArrayList<>();
        filter.add("my_second_input");

        sut = new DebugAction(logger, inputs, filter);

        // W
        sut.execute();

        // T
        assertThat(logger.info).containsExactly(
            "my_second_input : [input_value]"
        );
    }

    @Test
    public void should_be_compatible_with_older_scenario() {
        // G
        TestLogger logger = new TestLogger();
        Map<String, Object> inputs = new HashMap<>();
        inputs.put("my_first_input", "input_value");
        inputs.put("my_second_input", "input_value");
        inputs.put("my_third_input", "input_value");

        sut = new DebugAction(logger, inputs, null);

        // W
        sut.execute();

        // T
        assertThat(logger.info).containsExactlyInAnyOrder(
            "my_first_input : [input_value]",
            "my_second_input : [input_value]",
            "my_third_input : [input_value]"
        );
    }
}
