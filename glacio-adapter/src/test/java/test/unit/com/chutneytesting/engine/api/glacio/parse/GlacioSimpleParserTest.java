package test.unit.com.chutneytesting.engine.api.glacio.parse;

import static java.util.Collections.emptyList;
import static java.util.Optional.empty;
import static org.assertj.core.api.Assertions.assertThat;

import com.chutneytesting.engine.api.glacio.parse.GlacioSimpleParser;
import com.github.fridujo.glacio.ast.Position;
import com.github.fridujo.glacio.ast.Step;
import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(JUnitParamsRunner.class)
public class GlacioSimpleParserTest {

    private GlacioSimpleParser sut = new GlacioSimpleParser();

    @Test
    public void should_have_the_lowest_priority() {
        assertThat(sut.priority()).isEqualTo(Integer.MAX_VALUE);
    }

    @Test
    @Parameters({"", "this is a step name without taskid defined", "(success) it's a step name with taskid defined"})
    public void could_parse_anything(String textToParse) {
        assertThat(sut.couldParse(textToParse)).isTrue();
    }

    @Test
    @Parameters({"success", "debug", "fail", "context-put", "task-id"})
    public void should_take_taskid_in_parenthesis(String taskId) {
        assertThat(sut.parseTaskType(buildSimpleStepWithText("("+taskId+") it's a step name with taskid defined")))
            .isEqualTo(taskId);
    }

    @Test
    @Parameters({"success", "(debug", "fail)", "context-put", "task-id"})
    public void should_take_taskid_as_text_when_no_parenthesis(String taskIdAsText) {
        assertThat(sut.parseTaskType(buildSimpleStepWithText(taskIdAsText)))
            .isEqualTo(taskIdAsText);
    }

    private Step buildSimpleStepWithText(String stepText) {
        return new Step(new Position(0, 0), stepText, emptyList(), empty(), empty());
    }
}
