package changelog;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import com.chutneytesting.campaign.infra.jpa.Campaign;
import com.chutneytesting.campaign.infra.jpa.CampaignParameter;
import com.chutneytesting.scenario.infra.jpa.Scenario;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.TestPropertySource;
import util.infra.AbstractLocalDatabaseTest;
import util.infra.EnableH2FileTestInfra;
import util.infra.EnableH2MemTestInfra;
import util.infra.EnablePostgreSQLTestInfra;
import util.infra.EnableSQLiteTestInfra;

@DisplayName("Liquibase changelog")
class LiquibaseChangelogTest {

    @Nested
    @DisplayName("On a fresh new database")
    @TestPropertySource(properties = {"chutney.test-infra.liquibase.run=false"})
    class FreshDB {
        @Nested
        @EnableH2MemTestInfra
        class H2 extends AbstractLocalDatabaseTest {
            @RepeatedTest(2)
            @DisplayName("Must be applied without error")
            void init_without_error() {
                assertDoesNotThrow(this::liquibaseUpdate);
            }

            @Test
            @DisplayName("Set scenario sequence without holes")
            void set_scenario_sequence_value_correctly() {
                // Given
                assertDoesNotThrow(this::liquibaseUpdate);
                givenScenario();
                givenScenario();
                givenScenario();
                Scenario s1 = givenScenario();

                // When redo liquibase
                assertDoesNotThrow(this::liquibaseUpdate);

                // Then
                Scenario s2 = givenScenario();
                assertThat(s2.id()).isEqualTo(s1.id() + 1);

                // When redo liquibase
                assertDoesNotThrow(this::liquibaseUpdate);

                // Then
                Scenario s3 = givenScenario();
                assertThat(s3.id()).isEqualTo(s2.id() + 1);
            }
        }

        @Nested
        @EnableSQLiteTestInfra
        class SQLite extends AbstractLocalDatabaseTest {
            @RepeatedTest(2)
            @DisplayName("Must be applied without error")
            void init_without_error() {
                assertDoesNotThrow(this::liquibaseUpdate);
            }
        }

        @Nested
        @EnablePostgreSQLTestInfra
        class Postgres extends AbstractLocalDatabaseTest {
            @RepeatedTest(2)
            @DisplayName("Must be applied without error")
            void init_without_error() {
                assertDoesNotThrow(this::liquibaseUpdate);
            }
        }
    }

    @Nested
    @DisplayName("On a database with data before sqlite compatibility migration")
    @TestPropertySource(properties = {"chutney.test-infra.liquibase.context=test"})
    class DataToMigrateDB {
        @Nested
        @EnableH2MemTestInfra
        class H2 extends AbstractLocalDatabaseTest {
            @Test
            @DisplayName("Must be applied without error")
            void init_without_error() {
            }
        }

        @Nested
        @EnablePostgreSQLTestInfra
        class Postgres extends AbstractLocalDatabaseTest {
            @Test
            @DisplayName("Must be applied without error")
            void init_without_error() {
            }

            @Test
            @DisplayName("Set scenario sequence correctly")
            void set_scenario_sequence_value_after_migration() {
                Scenario scenario = givenScenario();
                assertThat(scenario.id()).isEqualTo(3);
            }

            @Test
            @DisplayName("Set campaign sequences correctly")
            void set_campaign_sequence_value_after_migration() {
                Campaign campaign = transactionTemplate.execute(status -> {
                    Set<CampaignParameter> parameters = Set.of(
                        new CampaignParameter("param1", "val1")
                    );
                    Campaign c = new Campaign(null, "title", "", null, false, false, null, null, null, null, parameters);
                    entityManager.persist(c);
                    return c;
                });
                assertThat(campaign.id()).isEqualTo(2);
                assertThat(campaign.parameters()).hasSize(1).extracting("id").containsExactly(3L);
            }
        }
    }

    @Nested
    @DisplayName("On a 1.7.1 database without data")
    class Fresh171DB {
        @Nested
        @EnableH2FileTestInfra
        class H2 extends AbstractLocalDatabaseTest {
            @Test
            @DisplayName("Must be applied without error")
            void init_without_error() {
            }
        }
    }
}
