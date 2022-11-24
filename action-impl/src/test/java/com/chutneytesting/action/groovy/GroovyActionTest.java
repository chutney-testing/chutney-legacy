package com.chutneytesting.action.groovy;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;

import com.chutneytesting.action.TestLogger;
import com.chutneytesting.action.spi.ActionExecutionResult;
import com.chutneytesting.action.spi.ActionExecutionResult.Status;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;

public class GroovyActionTest {

    @Test
    public void valid_groovy_script() {
        String script = "import com.chutneytesting.action.groovy.GroovyAction\n" +
            "def script = '''\n" +
            "logger.info('Hello World, I can create groovy action into groovy action')\n" +
            "return [:]\n" +
            "'''\n" +
            "return new GroovyAction(script, [:], logger).execute().outputs";

        TestLogger logger = new TestLogger();
        ActionExecutionResult executionResult = new GroovyAction(script, null, logger).execute();

        assertThat(executionResult.status).as("Status of GroovyAction\n" + logger).isEqualTo(Status.Success);
        assertThat(executionResult.outputs).isEmpty();
        assertThat(logger.errors).isEmpty();
        assertThat(logger.info).containsExactly("Hello World, I can create groovy action into groovy action");
    }

    @Test
    public void not_compilable_groovy_script() {
        String script = "priorld') \n retu]";

        TestLogger logger = new TestLogger();
        ActionExecutionResult executionResult = new GroovyAction(script, Collections.emptyMap(), logger).execute();

        assertThat(executionResult.status).isEqualTo(Status.Failure);
        assertThat(logger.errors).hasSize(1).allMatch(s -> s.contains("Cannot compile groovy script"));
    }

    @Test
    public void groovy_script_failing_at_runtime() {
        String script = "throw new IllegalArgumentException(\"test error\")";

        TestLogger logger = new TestLogger();
        ActionExecutionResult executionResult = new GroovyAction(script, Collections.emptyMap(), logger).execute();

        assertThat(executionResult.status).isEqualTo(Status.Failure);
        assertThat(logger.errors).hasSize(1).containsExactlyInAnyOrder("Groovy script failed during execution: test error");
    }

    @Test
    public void output_should_be_in_step_result() {
        String script = "['bob':'éponge', 'spirou':'fantasio']";

        ActionExecutionResult executionResult = new GroovyAction(script, Collections.emptyMap(), new TestLogger()).execute();

        assertThat(executionResult.status).isEqualTo(Status.Success);
        assertThat(executionResult.outputs).containsOnly(entry("bob", "éponge"), entry("spirou", "fantasio"));
    }

    @Test
    public void variable_are_pass_to_groovy_script() {
        String script = "int lol = par1 + par1 \n return ['computation': lol]";
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("par1", 2);

        ActionExecutionResult executionResult = new GroovyAction(script, parameters, new TestLogger()).execute();

        assertThat(executionResult.status).isEqualTo(Status.Success);
        assertThat(executionResult.outputs).containsOnly(entry("computation", 4));
    }
}
