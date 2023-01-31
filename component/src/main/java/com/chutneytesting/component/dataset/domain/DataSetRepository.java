package com.chutneytesting.component.dataset.domain;

import com.chutneytesting.server.core.domain.dataset.DataSet;
import java.util.List;

public interface DataSetRepository {

    String save(DataSet dataSet);

    DataSet findById(String dataSetId);

    DataSet removeById(String dataSetId);

    List<DataSet> findAll();
}
