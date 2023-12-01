/*
 * Copyright 2017-2023 Enedis
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
