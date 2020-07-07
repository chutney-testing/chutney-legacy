package test.unit.com.chutneytesting.engine.api.glacio.parse.specific;

import com.chutneytesting.engine.api.glacio.parse.specific.StrategyRetryParser;
import com.chutneytesting.engine.domain.execution.strategies.StrategyProperties;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

public class StrategyRetryParserTest {

    StrategyRetryParser sut = new StrategyRetryParser();

    @Test
    void should_parse_parameters() {
        Map<String, Object> expected = new HashMap<>(2);
        expected.put("timeOut", "1 s");
        expected.put("retryDelay", "2 s");

        StrategyProperties strategyProperties = sut.parseProperties(Locale.ENGLISH, "every 1 s for 2 s");

        Assertions.assertThat(strategyProperties).isEqualTo(expected);

    }

    @Test
    void should_parse_parameters_2() {
        Map<String, Object> expected = new HashMap<>(2);
        expected.put("timeOut", "5 s");
        expected.put("retryDelay", "3 s");

        StrategyProperties strategyProperties = sut.parseProperties(Locale.ENGLISH, "this are free words every 5 s noise noise words for 3 s still noise ");

        Assertions.assertThat(strategyProperties).isEqualTo(expected);

    }
}
