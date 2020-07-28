package test.unit.com.chutneytesting.engine.api.glacio;

import static org.assertj.core.api.Assertions.assertThat;

import com.chutneytesting.engine.api.glacio.StepFactory.EXECUTABLE_KEYWORD;
import com.chutneytesting.engine.api.glacio.GlacioAdapterConfiguration;
import com.chutneytesting.engine.api.glacio.parse.IParseExecutableStep;
import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import test.unit.com.chutneytesting.engine.api.glacio.parse.DebugParser;
import test.unit.com.chutneytesting.engine.api.glacio.parse.NoGlacioParser;
import test.unit.com.chutneytesting.engine.api.glacio.parse.SuccessParser;

public class GlacioAdapterConfigurationTest {

    private static List<IParseExecutableStep> glacioExecutableStepParsers;
    private static Map<Locale, Map<EXECUTABLE_KEYWORD, Set<String>>> executableStepLanguagesKeywords;
    private static Map<Pair<Locale, String>, IParseExecutableStep> glacioExecutableStepParsersLanguages;

    private final Locale testLang = new Locale("tt", "TT");

    @BeforeAll
    public static void setUp() throws IOException {
        GlacioAdapterConfiguration glacioAdapterConfiguration = new GlacioAdapterConfiguration();

        glacioExecutableStepParsers = glacioAdapterConfiguration.glacioExecutableStepParsers();
        executableStepLanguagesKeywords = glacioAdapterConfiguration.executableStepLanguagesKeywords();
        glacioExecutableStepParsersLanguages = glacioAdapterConfiguration.glacioExecutableStepParsersLanguages();
    }

    @Test
    public void should_load_declared_parsers() {
        List<IParseExecutableStep> testParsers = glacioExecutableStepParsers.stream()
            .filter(parser -> (parser instanceof SuccessParser || parser instanceof DebugParser))
            .collect(Collectors.toList());
        assertThat(testParsers).hasSize(2);
    }

    @Test
    public void should_ignore_declared_parsers_which_dont_implements_interface() {
        List<IParseExecutableStep> testParsers = glacioExecutableStepParsers.stream()
            .filter(parser -> parser instanceof NoGlacioParser)
            .collect(Collectors.toList());
        assertThat(testParsers).isEmpty();
    }

    @Test
    public void should_support_languages_keyword_for_executable_steps() {
        assertSupportLanguagesKeywordForExecutableSteps(Locale.ENGLISH, "Do", "Run");
        assertSupportLanguagesKeywordForExecutableSteps(Locale.FRENCH, "Fait", "Exécute", "Execute");
    }

    @Test
    public void should_support_languages_keyword_for_executable_steps_extensions() {
        assertSupportLanguagesKeywordForExecutableSteps(Locale.ENGLISH, "ENTEST");
        assertSupportLanguagesKeywordForExecutableSteps(testLang, "TEST", "TST");
    }

    @Test
    public void should_map_a_locale_keyword_pair_to_a_unique_executable_step_parser() {
        assertThat(glacioExecutableStepParsersLanguages.get(Pair.of(testLang, "DEBUG"))).isInstanceOfAny(DebugParser.class, SuccessParser.class);
    }

    private void assertSupportLanguagesKeywordForExecutableSteps(Locale lang, String... keywords) {
        assertThat(executableStepLanguagesKeywords).containsKeys(lang);
        Map<EXECUTABLE_KEYWORD, Set<String>> fr = executableStepLanguagesKeywords.get(lang);
        assertThat(fr).containsKeys(EXECUTABLE_KEYWORD.DO);
        assertThat(fr.get(EXECUTABLE_KEYWORD.DO)).contains(keywords);
    }
}
