package com.chutneytesting.design.infra.storage.dataset;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import com.chutneytesting.design.domain.dataset.DataSet;
import com.chutneytesting.design.domain.dataset.DataSetNotFoundException;
import com.chutneytesting.design.infra.storage.scenario.compose.orient.OrientComponentDB;
import com.chutneytesting.tests.AbstractOrientDatabaseTest;
import com.orientechnologies.common.log.OLogManager;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.apache.groovy.util.Maps;
import org.assertj.core.util.Lists;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class OrientDataSetRepositoryTest extends AbstractOrientDatabaseTest {

    private static OrientDataSetRepository sut;

    @BeforeAll
    public static void setUp() {
        initComponentDB(DATABASE_NAME);
        sut = new OrientDataSetRepository(orientComponentDB);
        OLogManager.instance().setWarnEnabled(false);
    }

    @AfterEach
    public void after() {
        truncateCollection(DATABASE_NAME, OrientComponentDB.DATASET_CLASS);
    }

    @AfterAll
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
        assertThat(foundDataSet.name).isEqualTo(dataSet.name);
        assertThat(foundDataSet.description).isEqualTo(dataSet.description);
        assertThat(foundDataSet.creationDate).isAfter(beforeCreation);
        assertThat(foundDataSet.tags).isEqualTo(dataSet.tags);
        assertThat(foundDataSet.constants).isEqualTo(dataSet.constants);
        assertThat(foundDataSet.datatable).isEqualTo(dataSet.datatable);
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
                .withName(name)
                .withDescription(description)
                .withTags(tags)
                .withDatatable(multipleValues)
                .build()
        );

        // Then
        assertThat(foundUpdatedDataSet).isNotNull();
        assertThat(foundUpdatedDataSet.id).isEqualTo(dataSet.id);
        assertThat(foundUpdatedDataSet.name).isEqualTo(name);
        assertThat(foundUpdatedDataSet.description).isEqualTo(description);
        assertThat(foundUpdatedDataSet.creationDate).isEqualTo(dataSet.creationDate);
        assertThat(foundUpdatedDataSet.tags).isEqualTo(tags);
        assertThat(foundUpdatedDataSet.constants).isEqualTo(dataSet.constants);
        assertThat(foundUpdatedDataSet.datatable).isEqualTo(multipleValues);
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
        List<DataSet> expectedDataSets =
            IntStream.range(0, 10)
                .mapToObj(i -> saveAndReload(fullDataSetBuilder().build()))
                .map(ds -> DataSet.builder().fromDataSet(ds).withConstants(null).withDatatable(null).build())
                .collect(Collectors.toList());

        // When
        List<DataSet> all = sut.findAll();

        // Then
        assertThat(all).isEqualTo(expectedDataSets);
    }

    private DataSet saveAndReload(DataSet dataSet) {
        return sut.findById(sut.save(dataSet));
    }

    private DataSet.DataSetBuilder fullDataSetBuilder() {
        return DataSet.builder()
            .withName("name")
            .withDescription("description")
            .withTags(Lists.list("tag1", "tag2"))
            .withConstants(Maps.of("uk1", "uv1", "uk2", "uv2"))
            .withDatatable(Lists.list(
                Maps.of("mk1", "mv11", "mk2", "mv21"),
                Maps.of("mk1", "mv12", "mk2", "mv22"),
                Maps.of("mk1", "mv13", "mk2", "mv23")
            ));
    }
}
