package com.chutneytesting.design.infra.storage.dataset;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import com.chutneytesting.design.domain.dataset.DataSet;
import com.chutneytesting.design.domain.dataset.DataSetMetaData;
import com.chutneytesting.design.domain.dataset.DataSetNotFoundException;
import com.chutneytesting.design.infra.storage.db.orient.OrientComponentDB;
import com.chutneytesting.tests.AbstractOrientDatabaseTest;
import com.orientechnologies.common.log.OLogManager;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.apache.groovy.util.Maps;
import org.assertj.core.util.Lists;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

public class OrientDataSetRepositoryTest extends AbstractOrientDatabaseTest {

    private static OrientDataSetRepository sut;

    @BeforeClass
    public static void setUp() {
        initComponentDB(DATABASE_NAME);
        sut = new OrientDataSetRepository(orientComponentDB);
        OLogManager.instance().setWarnEnabled(false);
    }

    @After
    public void after() {
        truncateCollection(DATABASE_NAME, OrientComponentDB.DATASET_CLASS);
    }

    @AfterClass
    public static void tearDown() {
        destroyDB(DATABASE_NAME);
    }

    @Test
    public void should_find_saved_dataset_when_search_by_id() {
        // Given
        Instant beforeCreation = Instant.now().minusSeconds(1);
        DataSet.DataSetBuilder dataSetBuilder = fullDataSetBuilder();

        DataSet dataSet = dataSetBuilder
            .withId(sut.save(dataSetBuilder.build()))
            .build();

        // When
        DataSet foundDataSet = sut.findById(dataSet.id);

        // Then
        assertThat(foundDataSet).isNotNull();
        assertThat(foundDataSet.id).isEqualTo(dataSet.id);
        assertThat(foundDataSet.metadata.name).isEqualTo(dataSet.metadata.name);
        assertThat(foundDataSet.metadata.description).isEqualTo(dataSet.metadata.description);
        assertThat(foundDataSet.metadata.creationDate).isAfter(beforeCreation);
        assertThat(foundDataSet.metadata.tags).isEqualTo(dataSet.metadata.tags);
        assertThat(foundDataSet.uniqueValues).isEqualTo(dataSet.uniqueValues);
        assertThat(foundDataSet.multipleValues).isEqualTo(dataSet.multipleValues);
    }

    @Test
    public void should_update_dataset_when_id_exists() {
        // Given
        DataSet dataSet = saveAndReload(
            fullDataSetBuilder().build()
        );

        List<Map<String, String>> multipleValues = Lists.list(
            Maps.of("mk", "mv")
        );
        List<String> tags = Lists.list("T1");
        String description = "another description";
        String name = "another name";

        // When
        DataSet foundUpdatedDataSet = saveAndReload(
            DataSet.builder()
                .fromDataSet(dataSet)
                .withMetaData(
                    DataSetMetaData.builder()
                        .fromDataSetMetaData(dataSet.metadata)
                        .withName(name)
                        .withDescription(description)
                        .withTags(tags)
                        .build()
                )
                .withMultipleValues(multipleValues)
                .build()
        );

        // Then
        assertThat(foundUpdatedDataSet).isNotNull();
        assertThat(foundUpdatedDataSet.id).isEqualTo(dataSet.id);
        assertThat(foundUpdatedDataSet.metadata.name).isEqualTo(name);
        assertThat(foundUpdatedDataSet.metadata.description).isEqualTo(description);
        assertThat(foundUpdatedDataSet.metadata.creationDate).isEqualTo(dataSet.metadata.creationDate);
        assertThat(foundUpdatedDataSet.metadata.tags).isEqualTo(tags);
        assertThat(foundUpdatedDataSet.uniqueValues).isEqualTo(dataSet.uniqueValues);
        assertThat(foundUpdatedDataSet.multipleValues).isEqualTo(multipleValues);
    }

    @Test
    public void should_remove_dataset_when_id_exists() {
        // Given
        DataSet dataSet = saveAndReload(
            fullDataSetBuilder().build()
        );

        // When
        DataSet removedDataSet = sut.removeById(dataSet.id);

        // Then
        assertThat(removedDataSet).isEqualTo(dataSet);
        assertThatExceptionOfType(DataSetNotFoundException.class)
            .isThrownBy(() -> sut.findById(removedDataSet.id));
    }

    @Test
    public void should_throw_exception_when_dataset_not_found() {
        String unknownDataSetId = "666:5";

        assertThatExceptionOfType(DataSetNotFoundException.class)
            .isThrownBy(() -> sut.findById(unknownDataSetId));

        assertThatExceptionOfType(DataSetNotFoundException.class)
            .isThrownBy(() -> sut.removeById(unknownDataSetId));
    }

    @Test
    public void should_find_all_datasets() {
        // Given
        Map<String, DataSetMetaData> expectedDataSets =
            IntStream.range(0, 10)
                .mapToObj(i -> saveAndReload(fullDataSetBuilder().build()))
                .collect(Collectors.toMap(d -> d.id, d -> d.metadata));

        // When
        Map<String, DataSetMetaData> all = sut.findAll();

        // Then
        assertThat(all).isEqualTo(expectedDataSets);
    }

    private DataSet saveAndReload(DataSet dataSet) {
        return sut.findById(sut.save(dataSet));
    }

    private DataSet.DataSetBuilder fullDataSetBuilder() {
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
            ));
    }
}
