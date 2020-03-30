package com.chutneytesting.engine.api.glacio.parse;

import com.github.fridujo.glacio.ast.Step;
import java.util.Map;

public interface OutputsParser {
    Map<String, Object> parseTaskOutputs(Step step);
}
