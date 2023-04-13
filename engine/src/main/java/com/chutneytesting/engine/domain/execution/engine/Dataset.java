package com.chutneytesting.engine.domain.execution.engine;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public class Dataset {

    public final Map<String, String> constants;
    public final List<Map<String, String>> datatable;

    public Dataset() {
        this(Collections.emptyMap(), Collections.emptyList());
    }

    public Dataset(Map<String, String> constants, List<Map<String, String>> datatable) {
        this.constants = Collections.unmodifiableMap(constants);
        this.datatable = Collections.unmodifiableList(datatable);
    }

}
