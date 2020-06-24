package test.unit.com.chutneytesting.engine.api.glacio.parse.default_;

import static java.util.Locale.ENGLISH;
import static org.assertj.core.api.Assertions.assertThat;

import com.chutneytesting.engine.api.glacio.parse.default_.StrategyParser;
import com.chutneytesting.engine.api.glacio.parse.specific.StrategySoftAssertParser;
import java.util.Arrays;
import java.util.Map;
import org.junit.jupiter.api.Test;

public class StrategyParserTest {

    private StrategyParser sut = new StrategyParser();

    /*

    Scenario: Gracefully fallback to default strategy when type is unknown
        When A step fails (unkown-strat:)
            Do fail
        Then it keeps going to the next step
            Do success
    * */

    @Test
    void should_find_ending_strategy_without_parameters() {
        String sentence = "a step sentence with strategy (strategy:)";
        Map<String, String> actualStrategyDefinition = sut.findStrategyGroups(sentence);

        assertThat(actualStrategyDefinition).containsKey("strategy");
        assertThat(actualStrategyDefinition.get("strategy")).isEqualTo("");
    }

    @Test
    void should_find_strategy_anywhere_in_a_sentence() {
        String sentence = "a step (anywhere:) sentence";
        Map<String, String> actualStrategyDefinition = sut.findStrategyGroups(sentence);

        assertThat(actualStrategyDefinition).containsKeys("anywhere");
        assertThat(actualStrategyDefinition.get("anywhere")).isEqualTo("");
    }

    @Test
    void should_not_find_any_strategy() {
        String sentence = "a step (this is just a comment) sentence  with (another-non-catchable(: comment)";
        Map<String, String> actualStrategyDefinition = sut.findStrategyGroups(sentence);

        assertThat(actualStrategyDefinition).isEmpty();
    }

    @Test
    void should_find_two_strategies() {
        String sentence = "(softly:) Do a step (with a parenthesised comment) using 2 strategies (retry:) (should(: not be catch)";
        Map<String, String> actualStrategyDefinition = sut.findStrategyGroups(sentence);

        assertThat(actualStrategyDefinition).hasSize(2);
        assertThat(actualStrategyDefinition).containsKeys("softly", "retry");
    }

    @Test
    void should_find_strategy_with_hyphen_or_underscore_names() {
        String sentence = "a step sentence with (under_score_strategy:) and (hyphen-strategy:)";
        Map<String, String> actualStrategyDefinition = sut.findStrategyGroups(sentence);

        assertThat(actualStrategyDefinition).containsKeys("under_score_strategy", "hyphen-strategy");
    }

    @Test
    void should_find_parser_for_soft_assert_strategy() {
        StrategyParser sut = new StrategyParser(Arrays.asList(new StrategySoftAssertParser()));

        StrategyParser strategyParser = sut.getStrategyParser(ENGLISH, "softly");

        assertThat(strategyParser).isInstanceOf(StrategySoftAssertParser.class);
    }

}
