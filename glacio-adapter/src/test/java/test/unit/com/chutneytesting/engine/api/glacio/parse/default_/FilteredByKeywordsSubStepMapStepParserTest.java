package test.unit.com.chutneytesting.engine.api.glacio.parse.default_;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Optional.empty;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.chutneytesting.engine.api.glacio.parse.StepParser;
import com.chutneytesting.engine.api.glacio.parse.default_.FilteredByKeywordsSubStepMapStepParser;
import com.github.fridujo.glacio.model.Step;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

public class FilteredByKeywordsSubStepMapStepParserTest {

    private FilteredByKeywordsSubStepMapStepParser sut;
    private StepParser<Map.Entry<String, Object>> entryStepParser;

    @BeforeEach
    public void setUp() {
        entryStepParser = mock(StepParser.class);
    }

    @Test
    public void should_filter_substeps_with_keywords_and_collect_results_from_delegate_to_entry_parser() {
        // Given
        Step step = mock(Step.class);
        Step firstStep = new Step(false, empty(), "TEST step name 1", empty(), emptyList());
        Step thirdStep = new Step(false, empty(), "TST step name 3", empty(), emptyList());
        when(step.getSubsteps())
            .thenReturn(asList(
                firstStep,
                new Step(false, empty(), "TTT step name", empty(), emptyList()),
                thirdStep
            ));
        when(entryStepParser.parseStep(any())).thenReturn(entry("one", "v1"), entry("two", "v2"));

        sut = new FilteredByKeywordsSubStepMapStepParser(entryStepParser, "TEST", "TST");

        // When / Then
        assertThat(sut.parseStep(step)).containsOnly(entry("one", "v1"), entry("two", "v2"));

        ArgumentCaptor<Step> stepArgumentCaptor = ArgumentCaptor.forClass(Step.class);
        verify(entryStepParser, times(2)).parseStep(stepArgumentCaptor.capture());
        assertThat(stepArgumentCaptor.getAllValues())
            .extracting(Step::getText)
            .containsExactlyInAnyOrder("step name 1", "step name 3");
    }
}
