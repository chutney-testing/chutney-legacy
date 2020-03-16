package test.unit.com.chutneytesting.engine.api.glacio;

import static org.assertj.core.api.Assertions.assertThat;

import com.chutneytesting.engine.api.glacio.GlacioAdapterSpringConfiguration;
import com.chutneytesting.engine.api.glacio.parse.GlacioExecutableStepParser;
import com.chutneytesting.engine.api.glacio.parse.GlacioSimpleParser;
import java.util.TreeSet;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import test.unit.com.chutneytesting.engine.api.glacio.parse.SimpleSuccessParser;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = {GlacioAdapterSpringConfiguration.class})
public class GlacioAdapterSpringConfigurationTest {

    @Configuration
    @ComponentScan("com.chutneytesting.engine.api.glacio")
    public static class SpringConfig {
    }

    @Autowired
    private TreeSet<GlacioExecutableStepParser> glacioExecutableStepParsers;

    @Test
    public void should_load_in_order_declared_parsers() {
        assertThat(glacioExecutableStepParsers.first()).isInstanceOf(SimpleSuccessParser.class);
        assertThat(glacioExecutableStepParsers.descendingSet().first()).isInstanceOf(GlacioSimpleParser.class);
    }

}
