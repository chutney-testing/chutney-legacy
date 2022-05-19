package com.chutneytesting.dataset.infra;

import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import com.chutneytesting.dataset.domain.DataSet;
import com.chutneytesting.dataset.domain.DataSetNotFoundException;
import com.chutneytesting.dataset.infra.OrientDataSetRepository;
import com.chutneytesting.scenario.infra.orient.OrientComponentDB;
import com.chutneytesting.tests.OrientDatabaseHelperTest;
import com.orientechnologies.common.log.OLogManager;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.assertj.core.api.Assertions;
import org.assertj.core.util.Lists;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class OrientDataSetRepositoryTest {

    private static final String DATABASE_NAME = "orient_dataset_test";
    private static final OrientDatabaseHelperTest orientDatabaseHelperTest = new OrientDatabaseHelperTest(DATABASE_NAME);

    private static OrientDataSetRepository sut;

    @BeforeAll
    public static void setUp() {
        sut = new OrientDataSetRepository(orientDatabaseHelperTest.orientComponentDB);
        OLogManager.instance().setWarnEnabled(false);
    }

    @AfterEach
    public void after() {
        orientDatabaseHelperTest.truncateCollection(OrientComponentDB.DATASET_CLASS);
    }

    @AfterAll
    public static void tearDown() {
        orientDatabaseHelperTest.destroyDB();
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
        Assertions.assertThat(foundDataSet).isNotNull();
        Assertions.assertThat(foundDataSet.id).isEqualTo(dataSet.id);
        Assertions.assertThat(foundDataSet.name).isEqualTo(dataSet.name);
        Assertions.assertThat(foundDataSet.description).isEqualTo(dataSet.description);
        Assertions.assertThat(foundDataSet.creationDate).isAfter(beforeCreation);
        Assertions.assertThat(foundDataSet.tags).isEqualTo(dataSet.tags);
        Assertions.assertThat(foundDataSet.constants).isEqualTo(dataSet.constants);
        Assertions.assertThat(foundDataSet.datatable).isEqualTo(dataSet.datatable);
    }

    @Test
    public void should_update_dataset_when_id_exists() {
        // Given
        DataSet dataSet = saveAndReload(
            fullDataSetBuilder().build()
        );

        List<Map<String, String>> datatable = Lists.list(
            Map.of("mk", "mv")
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
                .withDatatable(datatable)
                .build()
        );

        // Then
        Assertions.assertThat(foundUpdatedDataSet).isNotNull();
        Assertions.assertThat(foundUpdatedDataSet.id).isEqualTo(dataSet.id);
        Assertions.assertThat(foundUpdatedDataSet.name).isEqualTo(name);
        Assertions.assertThat(foundUpdatedDataSet.description).isEqualTo(description);
        Assertions.assertThat(foundUpdatedDataSet.creationDate).isEqualTo(dataSet.creationDate);
        Assertions.assertThat(foundUpdatedDataSet.tags).isEqualTo(tags);
        Assertions.assertThat(foundUpdatedDataSet.constants).isEqualTo(dataSet.constants);
        Assertions.assertThat(foundUpdatedDataSet.datatable).isEqualTo(datatable);
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
        Assertions.assertThat(removedDataSet).isEqualTo(dataSet);
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
        Assertions.assertThat(all).isEqualTo(expectedDataSets);
    }

    private DataSet saveAndReload(DataSet dataSet) {
        return sut.findById(sut.save(dataSet));
    }

    private DataSet.DataSetBuilder fullDataSetBuilder() {
        return DataSet.builder()
            .withName("name")
            .withDescription("description")
            .withTags(Lists.list("tag1", "tag2"))
            .withConstants(Map.of("uk1", "uv1", "uk2", "uv2"))
            .withDatatable(Lists.list(
                Map.of("mk1", "mv11", "mk2", "mv21"),
                Map.of("mk1", "mv12", "mk2", "mv22"),
                Map.of("mk1", "mv13", "mk2", "mv23")
            ));
    }
}
