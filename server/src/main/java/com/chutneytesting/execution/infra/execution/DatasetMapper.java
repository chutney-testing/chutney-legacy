package com.chutneytesting.execution.infra.execution;

import com.chutneytesting.engine.api.execution.DatasetDto;
import com.chutneytesting.server.core.domain.dataset.DataSet;

public class DatasetMapper {

    static DatasetDto toDto(DataSet dataset) {
        return new DatasetDto(dataset.constants, dataset.datatable);
    }

}
