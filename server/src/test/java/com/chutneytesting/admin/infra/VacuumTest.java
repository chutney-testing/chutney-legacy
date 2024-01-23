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

package com.chutneytesting.admin.infra;

import static java.util.stream.Collectors.joining;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import com.chutneytesting.admin.domain.DBVacuum;
import java.io.File;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Isolated;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;
import org.springframework.test.context.ContextConfiguration;
import util.infra.AbstractLocalDatabaseTest;
import util.infra.EnableH2MemTestInfra;
import util.infra.EnablePostgreSQLTestInfra;
import util.infra.EnableSQLiteTestInfra;

@Isolated
public class VacuumTest {

    @Nested
    @EnableSQLiteTestInfra
    class SQLite extends AllTests {

        @Test
        void compact_db_size() {
            // Given
            dbInsertReports(100, _1Mo);
            File dbFile = dbFile(dataSourceProperties);
            double dbInitSizeBytes = dbSizeBytes();
            long dbInitSizeLength = dbFile.length();
            dbDeleteHalfReports(100);
            assertThat(dbSizeBytes()).isEqualTo(dbInitSizeBytes);
            assertThat(dbFile.length()).isEqualTo(dbInitSizeLength);

            // When
            sut.vacuum();

            // Then
            assertThat(dbSizeBytes()).isLessThan(dbInitSizeBytes);
            assertThat(dbFile.length()).isLessThan(dbInitSizeLength);
        }

        @Test
        void compact_db_size_while_updating() throws InterruptedException {
            // Given
            dbInsertReports(100, _1Mo);
            Runnable updatingRunnable = () ->
                IntStream.range(101, 251).forEach(i -> {
                    namedParameterJdbcTemplate.update(
                        "insert into scenario_executions_reports (scenario_execution_id, report, version) values (:id, :report, :version)",
                        Map.of("id", i, "report", _1Mo, "version", 1)
                    );
                    try {
                        TimeUnit.MILLISECONDS.sleep(5);
                    } catch (InterruptedException e) {
                        fail("Updating thread interrupted", e);
                    }
                });
            dbDeleteHalfReports(100);

            // When / Then
            CompletableFuture<Void> dbUpdating = CompletableFuture.runAsync(updatingRunnable);
            TimeUnit.SECONDS.sleep(1);
            assertDoesNotThrow(() -> {
                sut.vacuum();
                dbUpdating.get();
            });
        }

        private File dbFile(DataSourceProperties dsProperties) {
            String jdbcUrl = dsProperties.getUrl();
            int a = jdbcUrl.indexOf(":");
            int b = jdbcUrl.indexOf(":", a + 1);
            File dbFile = new File(jdbcUrl.substring(b + 1));
            assertThat(dbFile).exists().isFile();
            return dbFile;
        }

        private double dbSizeBytes() {
            Object r = entityManager.createNativeQuery("select page_size * page_count from pragma_page_count(), pragma_page_size()").getSingleResult();
            if (r instanceof Integer ri) {
                return ri.doubleValue();
            }
            if (r instanceof Long rl) {
                return rl.doubleValue();
            }
            throw new RuntimeException("dbSizeBytes not integer nor long value.");
        }

        static Stream<Arguments> lock_db_for_max_time_source() {
            return Stream.of(
                Arguments.of(1000, 30),
                Arguments.of(2000, 30),
                Arguments.of(3000, 60),
                Arguments.of(4000, 60),
                Arguments.of(5000, 90),
                Arguments.of(6000, 90),
                Arguments.of(7000, 120),
                Arguments.of(8000, 120),
                Arguments.of(9000, 150),
                Arguments.of(10000, 180)
            );
        }

        /**
         * Keep it for example.
         * Note that the DB is not fresh nor new at each execution, so the size is roughly false.
         */
        @Disabled
        @ParameterizedTest
        @MethodSource("lock_db_for_max_time_source")
        void lock_db_for_max_time(int nbMo, long maxSeconds) {
            // Given
            dbInsertReports(nbMo, _1Mo);
            File dbFile = dbFile(dataSourceProperties);
            double dbInitSizeBytes = dbSizeBytes();
            long dbInitSizeLength = dbFile.length();

            // When
            dbDeleteHalfReports(nbMo);
            await().atMost(maxSeconds, TimeUnit.SECONDS).untilAsserted(() -> {
                sut.vacuum();

                // Then
                assertThat(dbSizeBytes()).isLessThan(dbInitSizeBytes);
                assertThat(dbFile.length()).isLessThan(dbInitSizeLength);
            });
        }
    }

    @Nested
    @EnableH2MemTestInfra
    class H2 extends AllTests {
        @Test
        void is_not_supported() {
            Assertions.assertThatThrownBy(() -> sut.vacuum())
                .isInstanceOf(UnsupportedOperationException.class);
        }
    }

    @Nested
    @EnablePostgreSQLTestInfra
    class PostGreSQL extends AllTests {
        @Test
        void is_not_supported() {
            Assertions.assertThatThrownBy(() -> sut.vacuum())
                .isInstanceOf(UnsupportedOperationException.class);
        }
    }

    @ContextConfiguration(classes = AllTests.VacuumConfiguration.class)
    abstract class AllTests extends AbstractLocalDatabaseTest {

        @Configuration
        @ComponentScan(
            basePackages = {
                "com.chutneytesting.admin.infra"
            },
            includeFilters = {@ComponentScan.Filter(type = FilterType.REGEX, pattern = "^.*Vacuum$")}
        )
        static class VacuumConfiguration {
        }

        static final String _1Ko = """
                Lorem ipsum dolor sit amet, consectetur adipiscing elit. Cras eu nisl vestibulum, mattis mauris lobortis, eleifend mauris. Morbi commodo in metus ut ultricies. Nullam ac velit et risus pulvinar bibendum ut id lorem. Morbi hendrerit eleifend ante, vitae dignissim nulla lacinia aliquam. Donec mattis ut tellus sit amet pretium. Donec varius tincidunt dolor a ultrices. Cras ac nisl id orci vulputate varius vitae at ante. Aenean non ante non mi hendrerit varius in scelerisque ligula. Ut fermentum sed sapien sit amet mollis. Phasellus convallis quam in nibh ultricies, a vestibulum risus finibus.

                Duis aliquam magna at ipsum sagittis, at congue sem gravida. Nunc vitae porttitor mi. Aliquam elementum vehicula laoreet. Pellentesque ut massa erat. Vestibulum et vestibulum neque, id viverra est. Aliquam tincidunt imperdiet tristique. Nullam non turpis lacus. Vivamus pulvinar erat risus, eget dictum velit varius vitae. Praesent sodales enim at magna egestas viverra. In laoreet sapien porttitor pharetra interdum. Nam donec.
            """.stripIndent();

        static final String _1Mo = IntStream.range(0, 1024).mapToObj(i -> _1Ko).collect(joining());

        @Autowired
        protected DBVacuum sut;
        @Autowired
        protected DataSourceProperties dataSourceProperties;

        @AfterEach
        void afterEach() {
            clearTables();
        }

        @SuppressWarnings("unchecked")
        protected void dbInsertReports(int nbReports, String report) {
            namedParameterJdbcTemplate.batchUpdate(
                "insert into scenario_executions_reports (scenario_execution_id, report, version) values (:id, :report, :version)",
                IntStream.range(0, nbReports)
                    .mapToObj(i -> Map.of("id", i, "report", report, "version", 1))
                    .toArray(Map[]::new)
            );
        }

        @SuppressWarnings("unchecked")
        protected void dbDeleteHalfReports(int nbReports) {
            namedParameterJdbcTemplate.batchUpdate(
                "delete from scenario_executions_reports where scenario_execution_id = :id",
                IntStream.range(0, nbReports / 2)
                    .mapToObj(i -> Map.of("id", 2 * i))
                    .toArray(Map[]::new)
            );
        }
    }
}
