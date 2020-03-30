package com.chutneytesting.engine.api.glacio.parse.default_;

import com.chutneytesting.engine.api.glacio.parse.OutputsParser;
import com.github.fridujo.glacio.ast.Step;
import java.util.Map;
import java.util.stream.Collectors;
import org.apache.commons.lang3.tuple.Pair;

class DefaultOutputsParser implements OutputsParser {

    @Override
    public Map<String, Object> parseTaskOutputs(Step step) {
        return step.getSubsteps().stream()
            .filter(substep -> substep.getText().matches("^Take|Keep.*$"))
            .map(Step::getText)
            .map(text -> {
                int spaceIdx = text.indexOf(" ");
                return Pair.of(
                    text.substring(0, spaceIdx),
                    text.substring(spaceIdx)
                );
            })
            .collect(Collectors.toMap(Pair::getLeft, Pair::getRight));
    }
}
