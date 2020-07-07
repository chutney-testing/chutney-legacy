package com.chutneytesting.engine.api.glacio.parse.default_;

import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;

import com.chutneytesting.engine.api.glacio.parse.IParseStrategy;
import com.chutneytesting.engine.api.glacio.parse.StepParser;
import com.chutneytesting.engine.domain.environment.TargetImpl;
import com.chutneytesting.engine.domain.execution.strategies.StepStrategyDefinition;
import com.chutneytesting.task.spi.injectable.Target;
import com.github.fridujo.glacio.model.Step;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import org.apache.commons.lang3.tuple.Pair;

public final class EmptyParser {

    public static final StepParser<Map<String, Object>> emptyMapParser = new EmptyMapParser();
    public static final IParseStrategy noStrategyParser = new NoStrategyParser();
    public static final StepParser<Target> noTargetParser = new NoTargetParser();

    private EmptyParser() {
    }

    private static class EmptyMapParser implements StepParser<Map<String, Object>> {
        @Override
        public Map<String, Object> parseStep(Step step) {
            return emptyMap();
        }
    }

    private static class NoStrategyParser implements IParseStrategy {
        @Override
        public Map<Locale, Set<String>> keywords() {
            return emptyMap();
        }

        @Override
        public List<StepStrategyDefinition> parseStep(Locale lang, Step step) {
            return emptyList();
        }

        @Override
        public Pair<Step, List<StepStrategyDefinition>> parseStepAndStripStrategy(Locale lang, Step step) {
            return Pair.of(step, emptyList());
        }
    }

    private static class NoTargetParser implements StepParser<Target> {
        @Override
        public Target parseStep(Step step) {
            return TargetImpl.NONE;
        }
    }
}
