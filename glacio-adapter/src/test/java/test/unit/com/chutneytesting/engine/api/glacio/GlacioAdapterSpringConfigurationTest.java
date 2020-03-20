package test.unit.com.chutneytesting.engine.api.glacio;

import static org.assertj.core.api.Assertions.assertThat;

import com.chutneytesting.ExecutionSpringConfiguration;
import com.chutneytesting.engine.api.glacio.GlacioAdapterSpringConfiguration;
import com.chutneytesting.engine.api.glacio.parse.GlacioExecutableStepParser;
import java.util.List;
import java.util.TreeSet;
import java.util.stream.Collectors;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import test.unit.com.chutneytesting.engine.api.glacio.parse.DebugParser;
import test.unit.com.chutneytesting.engine.api.glacio.parse.NoGlacioParser;
import test.unit.com.chutneytesting.engine.api.glacio.parse.SuccessParser;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = {ExecutionSpringConfiguration.class, GlacioAdapterSpringConfiguration.class})
public class GlacioAdapterSpringConfigurationTest {

    @Configuration
    @ComponentScan("com.chutneytesting")
    public static class SpringConfig {
    }

    @Autowired
    private TreeSet<GlacioExecutableStepParser> glacioExecutableStepParsers;

    @Test
    public void should_load_in_order_declared_parsers() {
        List<GlacioExecutableStepParser> testParsers = glacioExecutableStepParsers.stream()
            .filter(parser -> (parser instanceof SuccessParser || parser instanceof DebugParser))
            .collect(Collectors.toList());

        assertThat(testParsers.get(0)).isInstanceOf(SuccessParser.class);
        assertThat(testParsers.get(1)).isInstanceOf(DebugParser.class);
    }

    @Test
    public void should_ignore_declared_parsers_who_dont_implements_interface() {
        List<GlacioExecutableStepParser> testParsers = glacioExecutableStepParsers.stream()
            .filter(parser -> parser instanceof NoGlacioParser)
            .collect(Collectors.toList());

        assertThat(testParsers).isEmpty();
    }

}
