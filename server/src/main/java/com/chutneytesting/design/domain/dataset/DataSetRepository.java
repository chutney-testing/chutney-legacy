package com.chutneytesting.design.domain.dataset;

import java.util.List;

public interface DataSetRepository {

    String save(DataSet dataSet);
    DataSet findById(String dataSetId);
    DataSet removeById(String dataSetId);
    List<DataSet> findAll();
}
