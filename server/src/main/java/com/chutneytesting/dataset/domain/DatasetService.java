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

package com.chutneytesting.dataset.domain;

import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toList;

import com.chutneytesting.scenario.domain.gwt.GwtTestCase;
import com.chutneytesting.server.core.domain.dataset.DataSet;
import com.chutneytesting.server.core.domain.dataset.DataSetNotFoundException;
import com.chutneytesting.server.core.domain.scenario.AggregatedRepository;
import com.chutneytesting.server.core.domain.scenario.TestCaseMetadataImpl;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Component;

@Component
public class DatasetService {

    private final DataSetRepository datasetRepository;
    private final AggregatedRepository<GwtTestCase> testCaseRepository;

    public DatasetService(DataSetRepository dataSetRepository, AggregatedRepository<GwtTestCase> testCaseRepository) {
        this.datasetRepository = dataSetRepository;
        this.testCaseRepository = testCaseRepository;
    }

    public DataSet findById(String id) {
        return datasetRepository.findById(id);
    }

    public List<DataSet> findAll() {
        return datasetRepository.findAll()
            .stream()
            .sorted(DataSet.datasetComparator)
            .collect(toList());
    }

    public DataSet save(DataSet dataset) {
        String id = datasetRepository.save(dataset);
        return DataSet.builder().fromDataSet(dataset).withId(id).build();
    }

    public DataSet update(Optional<String> oldId, DataSet dataset) {
        return ofNullable(dataset.id)
            .map(id -> {
                String newId = datasetRepository.save(dataset);
                oldId.ifPresent(old -> {
                    if (!dataset.id.equals(newId)) {
                        updateScenarios(old, newId);
                        datasetRepository.removeById(old);
                    }
                });
                return DataSet.builder().fromDataSet(dataset).withId(newId).build();
            })
            .orElseThrow(() -> new DataSetNotFoundException(null));
    }

    private void updateScenarios(String oldId, String newId) {
        testCaseRepository.findAll().stream()
            .filter(m -> oldId.equals(m.defaultDataset()))
            .map(m -> testCaseRepository.findById(m.id()))
            .forEach(o -> o.ifPresent(
                tc -> testCaseRepository.save(
                    GwtTestCase.builder()
                        .from(tc)
                        .withMetadata(TestCaseMetadataImpl.TestCaseMetadataBuilder.from(tc.metadata).withDefaultDataset(newId).build())
                        .build()
                ))
            );
    }

    public void remove(String datasetName) {
        datasetRepository.removeById(datasetName);
    }

}
