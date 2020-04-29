package com.chutneytesting.design.domain.dataset;

import java.util.Map;

public interface DataSetRepository {

    String save(DataSet dataSet);
    DataSet findById(String dataSetId);
    DataSet removeById(String dataSetId);

    Map<String, DataSetMetaData> findAll();
}
