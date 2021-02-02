package com.chutneytesting.engine.api.glacio.parse;

import com.github.fridujo.glacio.model.DataTable;
import com.github.fridujo.glacio.model.DocString;
import com.github.fridujo.glacio.model.Step;
import com.github.fridujo.glacio.model.StepArgument;
import java.util.Optional;

public class ParserHelper {

    private ParserHelper() {}

    public static Optional<DataTable> stepDataTable(Step step) {
        return step.getArgument().map(ParserHelper::stepArgumentDataTable);
    }

    public static Optional<DocString> stepDocString(Step step) {
        return step.getArgument().map(ParserHelper::stepArgumentDocString);
    }

    private static DataTable stepArgumentDataTable(StepArgument stepArgument) {
        if (StepArgument.Type.DATA_TABLE.equals(stepArgument.getType())) {
            return ((DataTable) stepArgument);
        }
        return null;
    }

    private static DocString stepArgumentDocString(StepArgument stepArgument) {
        if (StepArgument.Type.DOC_STRING.equals(stepArgument.getType())) {
            return ((DocString) stepArgument);
        }
        return null;
    }
}
