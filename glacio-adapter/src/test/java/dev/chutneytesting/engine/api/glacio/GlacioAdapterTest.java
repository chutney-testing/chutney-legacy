package dev.chutneytesting.engine.api.glacio;

import static com.google.common.collect.Lists.newArrayList;
import static java.util.Collections.emptyMap;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;

import com.chutneytesting.engine.api.glacio.GlacioAdapter;
import com.chutneytesting.engine.domain.execution.StepDefinition;
import com.google.common.io.Resources;
import java.io.File;
import java.nio.charset.Charset;
import java.util.List;
import org.assertj.core.util.Files;
import org.junit.Test;

public class GlacioAdapterTest {

    @Test
    public void should_adapt_glacio_scenario_to_chutney_engine_stepdef() {
        // setup
        GlacioAdapter sut = new GlacioAdapter();

        StepDefinition successAction = new StepDefinition("Do: success", null, "success", null, null, null, null);
        StepDefinition when = new StepDefinition("When something is good", null, "", null, null, singletonList(successAction), null);
        StepDefinition then = new StepDefinition("Then it is very good", null, "", null, null, singletonList(successAction), null);
        StepDefinition expected = new StepDefinition("Good feature - Success", null, "", null, emptyMap(), newArrayList(when, then), emptyMap());

        // Given
        String scenario = fileContent("glacio/do_succeed.feature");

        // When
        List<StepDefinition> actual = sut.toChutneyStepDefinition(scenario);

        // Then
        assertThat(actual).hasSize(1);
        assertThat(actual.get(0)).isEqualTo(expected);
    }

    private String fileContent(String resourcePath) {
        return Files.contentOf(new File(Resources.getResource(resourcePath).getPath()), Charset.forName("UTF-8"));
    }
}
