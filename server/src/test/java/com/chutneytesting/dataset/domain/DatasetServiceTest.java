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

import static java.util.Optional.of;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.chutneytesting.scenario.domain.gwt.GwtScenario;
import com.chutneytesting.scenario.domain.gwt.GwtTestCase;
import com.chutneytesting.server.core.domain.dataset.DataSet;
import com.chutneytesting.server.core.domain.scenario.AggregatedRepository;
import com.chutneytesting.server.core.domain.scenario.TestCaseMetadataImpl;
import java.util.List;
import org.junit.jupiter.api.Test;

class DatasetServiceTest {

    private final DataSetRepository datasetRepository = mock(DataSetRepository.class);
    private final AggregatedRepository<GwtTestCase> testCaseRepository = mock(AggregatedRepository.class);

    DatasetService sut = new DatasetService(datasetRepository, testCaseRepository);

    @Test
    public void should_sort_dataset_by_name() {
        // Given
        DataSet firstDataset = DataSet.builder().withName("A").build();
        DataSet secondDataset = DataSet.builder().withName("B").build();
        DataSet thirdDataset = DataSet.builder().withName("C").build();

        when(datasetRepository.findAll())
            .thenReturn(List.of(thirdDataset, secondDataset, firstDataset));

        // When
        List<DataSet> actual = sut.findAll();

        // Then
        assertThat(actual).containsExactly(firstDataset, secondDataset, thirdDataset);
    }

    @Test
    public void should_update_dataset_reference_in_scenarios_on_rename() {
        String oldId = "old_id";
        String newId = "new_id";

        TestCaseMetadataImpl metadata = TestCaseMetadataImpl.builder().withDefaultDataset(oldId).build();
        when(testCaseRepository.findAll()).thenReturn(List.of(metadata));

        GwtTestCase testCase = GwtTestCase.builder().withMetadata(metadata).withScenario(mock(GwtScenario.class)).build();
        when(testCaseRepository.findById(any())).thenReturn(of(testCase));

        GwtTestCase expected = GwtTestCase.builder().from(testCase).withMetadata(
            TestCaseMetadataImpl.TestCaseMetadataBuilder.from(metadata).withDefaultDataset(newId).build()
        ).build();

        sut.update(of(oldId), DataSet.builder().withId(newId).withName(newId).build());

        verify(testCaseRepository, times(1)).save(expected);
    }

    @Test
    void should_prevent_deletion_of_used_dataset() {
        // TODO - check if dataset is in used using a count
    }

    @Test
    public void should_return_dataset_with_id_after_save() {
        // Given
        DataSet dataset = DataSet.builder().withName("A").build();

        when(datasetRepository.save(any()))
            .thenReturn("newId");

        // When
        DataSet persistedDataset = sut.save(dataset);

        // Then
        assertThat(persistedDataset.id).isEqualTo("newId");
    }
}
