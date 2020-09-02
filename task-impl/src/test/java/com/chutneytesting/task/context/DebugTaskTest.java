package com.chutneytesting.task.context;

import static org.assertj.core.api.Assertions.assertThat;

import com.chutneytesting.task.TestLogger;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.junit.Test;

public class DebugTaskTest {

    private DebugTask sut;

    @Test
    public void should_log_all_inputs_by_default() {
        // G
        TestLogger logger = new TestLogger();
        Map<String, Object> inputs = new HashMap<>();
        inputs.put("my_first_input", "input_value");
        inputs.put("my_second_input", "input_value");
        inputs.put("my_third_input", "input_value");
        Set<String> filter = Collections.emptySet();

        sut = new DebugTask(logger, inputs, filter);

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

        Set<String> filter = new HashSet<>();
        filter.add("my_second_input");

        sut = new DebugTask(logger, inputs, filter);

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

        Set<String> filter = null;

        sut = new DebugTask(logger, inputs, filter);

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
