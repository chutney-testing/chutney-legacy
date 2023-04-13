package com.chutneytesting.engine.infrastructure.delegation;

import com.chutneytesting.engine.api.execution.DatasetDto;
import com.chutneytesting.engine.domain.execution.engine.Dataset;

public class DatasetMapper {

    static DatasetDto toDto(Dataset dataset) {
        return new DatasetDto(dataset.constants, dataset.datatable);
    }

}
