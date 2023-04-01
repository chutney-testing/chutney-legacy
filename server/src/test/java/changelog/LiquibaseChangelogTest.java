package changelog;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import util.AbstractLocalDatabaseTest;

@DisplayName("Liquibase changelog must be applied on empty base without error")
class LiquibaseChangelogTest {

    @Nested
    @DisplayName("H2")
    @ActiveProfiles("test-infra-h2")
    @TestPropertySource(properties = {"chutney.test-infra.init-liquibase=false"})
    class H2 extends AbstractLocalDatabaseTest {
        @Test
        void init_without_error() {
            assertDoesNotThrow(this::liquibaseUpdate);
        }
    }

    @Nested
    @DisplayName("SQLite")
    @ActiveProfiles("test-infra-sqlite")
    @TestPropertySource(properties = {"chutney.test-infra.init-liquibase=false"})
    class SQLite extends AbstractLocalDatabaseTest {
        @Test
        void init_without_error() {
            assertDoesNotThrow(this::liquibaseUpdate);
        }
    }

    @Nested
    @DisplayName("Postgres")
    @ActiveProfiles("test-infra-pgsql")
    @TestPropertySource(properties = {"chutney.test-infra.init-liquibase=false"})
    class Postgres extends AbstractLocalDatabaseTest {
        @Test
        void init_without_error() {
            assertDoesNotThrow(this::liquibaseUpdate);
        }
    }
}
