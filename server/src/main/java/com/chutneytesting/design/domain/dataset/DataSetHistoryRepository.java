package com.chutneytesting.design.domain.dataset;

import java.util.List;
import java.util.Optional;
import org.apache.commons.lang3.tuple.Pair;

public interface DataSetHistoryRepository {

    Integer lastVersion(String dataSetId);
    Optional<Pair<String, Integer>> addVersion(DataSet newDataSet, DataSet previousDataSet);
    List<Integer> allVersionNumbers(String dataSetId);
    DataSet version(String dataSetId, Integer version);
    void removeHistory(String dataSetId);
}
