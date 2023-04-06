package changelog;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.TestPropertySource;
import util.infra.AbstractLocalDatabaseTest;
import util.infra.EnableH2TestInfra;
import util.infra.EnablePostgreSQLTestInfra;
import util.infra.EnableSQLiteTestInfra;

@DisplayName("Liquibase changelog must be applied without error")
class LiquibaseChangelogTest {

    @Nested
    @DisplayName("On a fresh new database")
    class FirstCreation {
        @Nested
        @EnableH2TestInfra
        @TestPropertySource(properties = {"chutney.test-infra.init-liquibase=false"})
        class H2 extends AbstractLocalDatabaseTest {
            @Test
            void init_without_error() {
                assertDoesNotThrow(this::liquibaseUpdate);
            }
        }

        @Nested
        @EnableSQLiteTestInfra
        @TestPropertySource(properties = {"chutney.test-infra.init-liquibase=false"})
        class SQLite extends AbstractLocalDatabaseTest {
            @Test
            void init_without_error() {
                assertDoesNotThrow(this::liquibaseUpdate);
            }
        }

        @Nested
        @EnablePostgreSQLTestInfra
        @TestPropertySource(properties = {"chutney.test-infra.init-liquibase=false"})
        class Postgres extends AbstractLocalDatabaseTest {
            @Test
            void init_without_error() {
                assertDoesNotThrow(this::liquibaseUpdate);
            }
        }
    }

    @Nested
    @DisplayName("On a database with data before sqlite compatible migration")
    class WithDataToMigrate {
        @Nested
        @EnableH2TestInfra
        @TestPropertySource(properties = {"chutney.test-infra.init-context=test"})
        class H2 extends AbstractLocalDatabaseTest {
            @Test
            void init_without_error() {
            }
        }

        @Nested
        @EnablePostgreSQLTestInfra
        @TestPropertySource(properties = {"chutney.test-infra.init-context=test"})
        class Postgres extends AbstractLocalDatabaseTest {
            @Test
            void init_without_error() {
            }
        }
    }
}
