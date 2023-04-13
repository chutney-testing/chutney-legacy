package com.chutneytesting.engine.api.execution;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public class DatasetDto {

    public final Map<String, String> constants;
    public final List<Map<String, String>> datatable;

    public DatasetDto(Map<String, String> constants, List<Map<String, String>> datatable) {
        this.constants = Collections.unmodifiableMap(constants);
        this.datatable = Collections.unmodifiableList(datatable);
    }

}
