package com.chutneytesting.server.core.domain.dataset;

import java.util.Map;
import java.util.Optional;
import org.apache.commons.lang3.tuple.Pair;

/**
 * Repository interface for dataset's history management
 */
public interface DataSetHistoryRepository {

    /**
     * Retrieve last version of dataset with given id
     *
     * @param dataSetId The dataset id
     * @return The last version number
     * @throws DataSetNotFoundException
     */
    Integer lastVersion(String dataSetId);

    /**
     * Add a given dataset version
     *
     * @param newDataSet The dataset version to add
     * @return The Pair id / version of created version or empty if no differences were found to save
     * @throws DataSetNotFoundException
     */
    Optional<Pair<String, Integer>> addVersion(DataSet newDataSet);

    /**
     * Retrieve all versions of a dataset with given id
     *
     * @param externalDataSetId The dataset id
     * @return The map of existing datasets with versions keys
     */
    Map<Integer, DataSet> allVersions(String externalDataSetId);

    /**
     * Retrieve a specific dataset version
     *
     * @param externalDataSetId The dataset id
     * @param version   The version number
     * @return The dataset version
     * @throws DataSetNotFoundException
     */
    DataSet version(String externalDataSetId, Integer version);

    /**
     * Delete the version history of dataset with given id
     *
     * @param externalDataSetId Teh dataset id
     */
    void removeHistory(String externalDataSetId);
}
