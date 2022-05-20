package com.chutneytesting.glacio.domain.parser.executable.common;

import static java.util.Collections.emptyMap;

import com.chutneytesting.engine.api.execution.TargetExecutionDto;
import com.chutneytesting.glacio.domain.parser.GlacioStepParser;
import com.chutneytesting.glacio.domain.parser.ParsingContext;
import com.chutneytesting.glacio.domain.parser.strategy.IParseStrategy;
import com.chutneytesting.glacio.domain.parser.strategy.NoStrategyParser;
import com.github.fridujo.glacio.model.Step;
import java.util.Map;

public final class EmptyParser {

    public static final GlacioStepParser<Map<String, Object>> emptyMapParser = new EmptyMapParser();
    public static final IParseStrategy noStrategyParser = new NoStrategyParser();
    public static final GlacioStepParser<TargetExecutionDto> noTargetParser = new NoTargetParser();

    private EmptyParser() {
    }

    private static class EmptyMapParser implements GlacioStepParser<Map<String, Object>> {
        @Override
        public Map<String, Object> parseGlacioStep(ParsingContext context, Step step) {
            return emptyMap();
        }
    }

    private static class NoTargetParser implements GlacioStepParser<TargetExecutionDto> {
        @Override
        public TargetExecutionDto parseGlacioStep(ParsingContext context, Step step) {
            return null;
        }
    }
}
