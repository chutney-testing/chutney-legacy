package com.chutneytesting.design.infra.storage.scenario.compose.orient;

import static com.chutneytesting.design.infra.storage.scenario.compose.orient.OrientComponentDB.GE_STEP_CLASS;
import static com.chutneytesting.design.infra.storage.scenario.compose.orient.OrientComponentDB.STEP_CLASS;
import static com.chutneytesting.design.infra.storage.scenario.compose.orient.OrientComponentDB.TESTCASE_CLASS;
import static com.chutneytesting.design.infra.storage.scenario.compose.orient.changelog.OrientChangelogExecutor.DBCHANGELOG_CLASS;
import static com.chutneytesting.design.infra.storage.scenario.compose.orient.changelog.OrientChangelogExecutor.filterFStepSchemaScripts;
import static org.assertj.core.api.Assertions.assertThat;

import com.chutneytesting.tests.OrientDatabaseHelperTest;
import com.orientechnologies.orient.core.db.ODatabaseSession;
import com.orientechnologies.orient.core.db.ODatabaseType;
import com.orientechnologies.orient.core.index.OIndex;
import com.orientechnologies.orient.core.metadata.schema.OClass;
import com.orientechnologies.orient.core.metadata.schema.OProperty;
import com.orientechnologies.orient.core.metadata.schema.OType;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class OrientComponentDBTest {

    private static final String DATABASE_NAME = "orient_component_db_test";
    private static final OrientDatabaseHelperTest orientDatabaseHelperTest = new OrientDatabaseHelperTest(DATABASE_NAME, ODatabaseType.PLOCAL);

    private static OrientComponentDB sut;

    @BeforeAll
    public static void setUp() {
        sut = orientDatabaseHelperTest.orientComponentDB;
    }

    @AfterAll
    public static void tearDown() {
        orientDatabaseHelperTest.destroyDB();
    }

    @Test
    public void should_get_a_connection_pool_when_requested() {
        assertThat(sut.dbPool()).isNotNull();
    }

    @Test
    public void should_FuncStep_class_exists_when_database_initialized() {
        try (ODatabaseSession dbSession = sut.dbPool().acquire()) {
            OClass fstepClass = dbSession.getClass(STEP_CLASS);
            OClass vertexClass = dbSession.getClass("V");
            assertThat(fstepClass).isNotNull();
            assertThat(fstepClass.getSuperClasses()).containsExactly(vertexClass);
        }
    }

    @Test
    public void should_Denote_class_exists_when_database_initialized() {
        try (ODatabaseSession dbSession = sut.dbPool().acquire()) {
            OClass fstepClass = dbSession.getClass(OrientComponentDB.GE_STEP_CLASS);
            OClass vertexClass = dbSession.getClass("E");
            assertThat(fstepClass).isNotNull();
            assertThat(fstepClass.getSuperClasses()).containsExactly(vertexClass);
        }
    }

    @Test
    public void should_FuncStep_class_name_property_exists_when_database_initialized() {
        try (ODatabaseSession dbSession = sut.dbPool().acquire()) {
            OClass fstepClass = dbSession.getClass(STEP_CLASS);
            final OProperty nameProperty = fstepClass.getProperty(OrientComponentDB.STEP_CLASS_PROPERTY_NAME);
            assertThat(nameProperty).isNotNull();
            assertThat(nameProperty.getType()).isEqualTo(OType.STRING);
            assertThat(nameProperty.isMandatory()).isTrue();
        }
    }

    @Test
    public void should_testCase_class_exists_when_database_initialized() {
        try (ODatabaseSession dbSession = sut.dbPool().acquire()) {
            OClass testCaseClass = dbSession.getClass(OrientComponentDB.TESTCASE_CLASS);
            OClass vertexClass = dbSession.getClass("V");
            assertThat(testCaseClass).isNotNull();
            assertThat(testCaseClass.getSuperClasses()).containsExactly(vertexClass);
        }
    }

    @Test
    public void should_FuncStep_class_name_property_indexes_exists_when_database_initialized() {
        try (ODatabaseSession dbSession = sut.dbPool().acquire()) {
            OClass fstepClass = dbSession.getClass(STEP_CLASS);

            assertThat(
                fstepClass.getProperty(OrientComponentDB.STEP_CLASS_PROPERTY_NAME).getAllIndexes().stream().map(OIndex::getName)
            ).containsExactlyInAnyOrder(
                OrientComponentDB.STEP_CLASS_INDEX_NAME
            );
        }
    }

    @Test
    public void should_ChangeLog_completed_when_database_initialized() {
        try (ODatabaseSession dbSession = sut.dbPool().acquire()) {
            OClass changeLogClass = dbSession.getClass(DBCHANGELOG_CLASS);
            assertThat(changeLogClass).isNotNull();
            assertThat(changeLogClass.getClusterIds()).hasSize(1);
            assertThat(changeLogClass.count()).isEqualTo(filterFStepSchemaScripts().count());
        }
    }

    @Test
    public void should_ChangeLog_completed_twice_with_no_change() {
        orientDatabaseHelperTest.changelogExecution.updateWithChangelog(sut.dbPool());
        should_ChangeLog_completed_when_database_initialized();
    }

    @Test
    public void should_backup_database_as_zip() throws IOException {
        // Given
        Path backup = Paths.get("./target/backup", "orient");
        Files.createDirectories(backup.getParent());
        Files.deleteIfExists(backup);

        try (OutputStream outputStream = Files.newOutputStream(Files.createFile(backup))) {
            // When
            sut.backup(outputStream);
        }

        // Then
        ZipFile zipFile = new ZipFile(backup.toString());
        List<String> entriesNames = zipFile.stream().map(ZipEntry::getName).collect(Collectors.toList());
        int dbchangelogCount = 0;
        int denoteCount = 0;
        int funcstepCount = 0;
        int testcaseCount = 0;
        for (String entryName : entriesNames) {
            if (entryName.startsWith(DBCHANGELOG_CLASS.toLowerCase())) dbchangelogCount++;
            if (entryName.startsWith(GE_STEP_CLASS.toLowerCase())) denoteCount++;
            if (entryName.startsWith(STEP_CLASS.toLowerCase())) funcstepCount++;
            if (entryName.startsWith(TESTCASE_CLASS.toLowerCase())) testcaseCount++;
        }
        assertThat(dbchangelogCount).isPositive();
        assertThat(denoteCount).isPositive();
        assertThat(funcstepCount).isPositive();
        assertThat(testcaseCount).isPositive();
    }

    @Test
    public void should_dataset_classes_exist_when_database_initialized() {
        try (ODatabaseSession dbSession = sut.dbPool().acquire()) {
            OClass dataSetClass = dbSession.getClass(OrientComponentDB.DATASET_CLASS);
            OClass dataSetHistoryClass = dbSession.getClass(OrientComponentDB.DATASET_HISTORY_CLASS);
            assertThat(dataSetClass).isNotNull();
            assertThat(dataSetHistoryClass).isNotNull();
        }
    }
}
