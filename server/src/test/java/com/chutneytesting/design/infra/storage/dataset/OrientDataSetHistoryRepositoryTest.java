package com.chutneytesting.design.infra.storage.dataset;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.chutneytesting.design.domain.dataset.DataSet;
import com.chutneytesting.design.domain.dataset.DataSetMetaData;
import com.chutneytesting.design.domain.dataset.DataSetNotFoundException;
import com.chutneytesting.design.infra.storage.db.orient.OrientComponentDB;
import com.chutneytesting.tests.AbstractOrientDatabaseTest;
import com.orientechnologies.common.log.OLogManager;
import java.util.List;
import java.util.Optional;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.groovy.util.Maps;
import org.assertj.core.util.Lists;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

public class OrientDataSetHistoryRepositoryTest extends AbstractOrientDatabaseTest {

    private static OrientDataSetHistoryRepository sut;

    private static DataSet originalDataSet;

    @BeforeClass
    public static void setUp() {
        initComponentDB(DATABASE_NAME);
        sut = new OrientDataSetHistoryRepository(orientComponentDB);
        OLogManager.instance().setWarnEnabled(false);

        OrientDataSetRepository orientDataSetRepository = new OrientDataSetRepository(orientComponentDB);
        originalDataSet = orientDataSetRepository.findById(orientDataSetRepository.save(dataSet()));
    }

    @After
    public void after() {
        truncateCollection(DATABASE_NAME, OrientComponentDB.DATASET_HISTORY_CLASS);
    }

    @AfterClass
    public static void tearDown() {
        destroyDB(DATABASE_NAME);
    }

    @Test
    public void should_add_versions() {
        addVersionsAndAssert();
    }

    @Test
    public void should_not_add_version_for_identical_datasets() {
        DataSet originalCopy = DataSet.builder().fromDataSet(originalDataSet).build();
        Optional<Pair<String, Integer>> versionId = sut.addVersion(originalCopy, originalDataSet);
        assertThat(versionId).isEmpty();
    }

    @Test
    public void should_find_dataset_version() {
        List<DataSet> dataSets = addVersionsAndAssert();
        for (int i = 0; i < dataSets.size(); i++) {
            assertThat(sut.version(originalDataSet.id, i + 1)).isEqualTo(dataSets.get(i));
        }
    }

    @Test
    public void should_find_all_version_numbers() {
        addVersionsAndAssert();
        assertThat(sut.allVersionNumbers(originalDataSet.id)).containsExactly(1, 2, 3, 4, 5, 6);
    }

    @Test
    public void should_remove_dataset_history() {
        addVersionsAndAssert();
        assertThat(sut.lastVersion(originalDataSet.id)).isEqualTo(6);

        sut.removeHistory(originalDataSet.id);
        assertThatThrownBy(() -> sut.lastVersion(originalDataSet.id))
            .isInstanceOf(DataSetNotFoundException.class);
    }

    private List<DataSet> addVersionsAndAssert() {
        // First version
        Optional<Pair<String, Integer>> versionId = sut.addVersion(originalDataSet, null);
        assertThat(versionId).isNotEmpty();
        assertThat(versionId.get().getRight()).isEqualTo(1);
        assertThat(versionId.get().getLeft()).isNotBlank();

        // Second version - name change
        DataSet nameChangedDataSet = DataSet.builder()
            .fromDataSet(originalDataSet)
            .withMetaData(
                DataSetMetaData.builder()
                    .fromDataSetMetaData(originalDataSet.metadata)
                    .withCreationDate(null)
                    .withName("new name")
                    .build()
            )
            .build();
        versionId = sut.addVersion(nameChangedDataSet, originalDataSet);
        assertThat(versionId).isNotEmpty();
        assertThat(versionId.get().getRight()).isEqualTo(2);
        assertThat(versionId.get().getLeft()).isNotBlank();

        // Third version - unique values change
        DataSet uniqueValuesChangedDataSet = DataSet.builder()
            .fromDataSet(nameChangedDataSet)
            .withMetaData(
                DataSetMetaData.builder()
                    .fromDataSetMetaData(nameChangedDataSet.metadata)
                    .withCreationDate(null)
                    .build()
            )
            .withUniqueValues(Maps.of("uk1", "uv11", "uk22", "uv2"))
            .build();
        versionId = sut.addVersion(uniqueValuesChangedDataSet, nameChangedDataSet);
        assertThat(versionId).isNotEmpty();
        assertThat(versionId.get().getRight()).isEqualTo(3);
        assertThat(versionId.get().getLeft()).isNotBlank();

        // Fourth version - description change
        DataSet descriptionChangedDataSet = DataSet.builder()
            .fromDataSet(uniqueValuesChangedDataSet)
            .withMetaData(
                DataSetMetaData.builder()
                    .fromDataSetMetaData(uniqueValuesChangedDataSet.metadata)
                    .withCreationDate(null)
                    .withDescription("new Description")
                    .build()
            )
            .build();
        versionId = sut.addVersion(descriptionChangedDataSet, uniqueValuesChangedDataSet);
        assertThat(versionId).isNotEmpty();
        assertThat(versionId.get().getRight()).isEqualTo(4);
        assertThat(versionId.get().getLeft()).isNotBlank();

        // Fifth version - multiple values change
        DataSet multipleValuesChangedDataSet = DataSet.builder()
            .fromDataSet(descriptionChangedDataSet)
            .withMetaData(
                DataSetMetaData.builder()
                    .fromDataSetMetaData(descriptionChangedDataSet.metadata)
                    .withCreationDate(null)
                    .build()
            )
            .withMultipleValues(Lists.list(
                Maps.of("mk1", "mv11", "mk2", "mv21", "mk3", "mv31"),
                Maps.of("mk1", "new12", "mk2", "mv22", "mk3", "mv32"),
                Maps.of("mk1", "mv13", "mk2", "new23", "mk3", "mv33")
            ))
            .build();
        versionId = sut.addVersion(multipleValuesChangedDataSet, descriptionChangedDataSet);
        assertThat(versionId).isNotEmpty();
        assertThat(versionId.get().getRight()).isEqualTo(5);
        assertThat(versionId.get().getLeft()).isNotBlank();

        // Sixth version - tags change
        DataSet tagsChangedDataSet = DataSet.builder()
            .fromDataSet(multipleValuesChangedDataSet)
            .withMetaData(
                DataSetMetaData.builder()
                    .fromDataSetMetaData(multipleValuesChangedDataSet.metadata)
                    .withCreationDate(null)
                    .withTags(Lists.list("tag1", "tag4"))
                    .build()
            )
            .build();
        versionId = sut.addVersion(tagsChangedDataSet, multipleValuesChangedDataSet);
        assertThat(versionId).isNotEmpty();
        assertThat(versionId.get().getRight()).isEqualTo(6);
        assertThat(versionId.get().getLeft()).isNotBlank();

        return Lists.list(originalDataSet, nameChangedDataSet, uniqueValuesChangedDataSet, descriptionChangedDataSet, multipleValuesChangedDataSet, tagsChangedDataSet);
    }

    private static DataSet dataSet() {
        return DataSet.builder()
            .withMetaData(
                DataSetMetaData.builder()
                    .withName("name")
                    .withDescription("description")
                    .withTags(Lists.list("tag1", "tag2"))
                    .build()
            )
            .withUniqueValues(Maps.of("uk1", "uv1", "uk2", "uv2"))
            .withMultipleValues(Lists.list(
                Maps.of("mk1", "mv11", "mk2", "mv21"),
                Maps.of("mk1", "mv12", "mk2", "mv22"),
                Maps.of("mk1", "mv13", "mk2", "mv23")
            ))
            .build();
    }
}
