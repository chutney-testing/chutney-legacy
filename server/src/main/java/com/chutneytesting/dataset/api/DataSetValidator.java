package com.chutneytesting.dataset.api;

import com.chutneytesting.server.core.domain.tools.ui.KeyValue;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

public class DataSetValidator {
    public static void validateDatasetSave(DataSetDto dataSet) {
        if (dataSet == null || dataSet.datatable() == null) {
            throw new NoSuchElementException("Dataset is null");
        }
        if (dataSet.datatable().isEmpty()) {
            return;
        }
        if (checkDuplicatedHeader(dataSet.datatable())) {
            throw new IllegalArgumentException("Duplicated header");
        }
    }

    private static boolean checkDuplicatedHeader(List<List<KeyValue>> datatable) {
        return datatable.get(0).stream().map(KeyValue::key).collect(Collectors.toSet()).size() != datatable.get(0).size();
    }
}
