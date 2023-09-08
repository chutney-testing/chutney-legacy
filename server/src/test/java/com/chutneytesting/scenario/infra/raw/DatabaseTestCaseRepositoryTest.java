package com.chutneytesting.scenario.infra.raw;

import static java.lang.Integer.parseInt;
import static java.lang.Long.valueOf;
import static java.time.temporal.ChronoUnit.MILLIS;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.entry;
import static org.assertj.core.api.Assertions.within;
import static util.WaitUtils.awaitDuring;

import com.chutneytesting.campaign.infra.CampaignExecutionDBRepository;
import com.chutneytesting.campaign.infra.jpa.CampaignEntity;
import com.chutneytesting.execution.infra.storage.jpa.ScenarioExecutionEntity;
import com.chutneytesting.scenario.domain.gwt.GwtScenario;
import com.chutneytesting.scenario.domain.gwt.GwtStep;
import com.chutneytesting.scenario.domain.gwt.GwtTestCase;
import com.chutneytesting.scenario.domain.gwt.GwtTestCase.GwtTestCaseBuilder;
import com.chutneytesting.scenario.infra.jpa.Scenario;
import com.chutneytesting.server.core.domain.execution.report.ServerReportStatus;
import com.chutneytesting.server.core.domain.scenario.ScenarioNotFoundException;
import com.chutneytesting.server.core.domain.scenario.TestCase;
import com.chutneytesting.server.core.domain.scenario.TestCaseMetadata;
import com.chutneytesting.server.core.domain.scenario.TestCaseMetadataImpl;
import com.chutneytesting.server.core.domain.scenario.TestCaseMetadataImpl.TestCaseMetadataBuilder;
import com.chutneytesting.server.core.domain.security.User;
import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import util.infra.AbstractLocalDatabaseTest;
import util.infra.EnableH2MemTestInfra;
import util.infra.EnablePostgreSQLTestInfra;
import util.infra.EnableSQLiteTestInfra;

public class DatabaseTestCaseRepositoryTest {

    @Nested
    @EnableH2MemTestInfra
    class H2 extends AllTests {
    }

    @Nested
    @EnableSQLiteTestInfra
    class SQLite extends AllTests {
    }

    @Nested
    @EnablePostgreSQLTestInfra
    class PostreSQL extends AllTests {
    }

    abstract class AllTests extends AbstractLocalDatabaseTest {
        @Autowired
        private DatabaseTestCaseRepository sut;
        @Autowired
        private CampaignExecutionDBRepository campaignExecutionDBRepository;

        private static final GwtTestCase GWT_TEST_CASE = GwtTestCase.builder()
            .withMetadata(TestCaseMetadataImpl.builder().build())
            .withExecutionParameters(Collections.emptyMap())
            .withScenario(
                GwtScenario.builder().withWhen(GwtStep.NONE).build()
            ).build();

        @AfterEach
        void afterEach() {
            clearTables();
        }

        @Test
        public void should_generate_id_when_scenario_is_persisted() {
            // When: a scenarioTemplate is saved
            String scenarioID = sut.save(GWT_TEST_CASE);

            // Then: a non blank id is generated
            assertThat(scenarioID).isNotBlank();
        }

        @Test
        public void should_find_scenario_by_id() {
            // Given: a scenarioTemplate in the repository
            String scenarioID = sut.save(GWT_TEST_CASE);


            // When: we look for that scenarioTemplate
            Optional<GwtTestCase> optScena = sut.findById(scenarioID);

            // Then: the scenarioTemplate is found
            assertThat(optScena).isPresent();
        }

        @Test
        public void should_retrieve_all_data_of_saved_testCase() {
            // Given: a scenarioTemplate in the repository
            Instant creationTime = Instant.now().truncatedTo(MILLIS);
            GwtTestCase aTestCase = GwtTestCase.builder()
                .withMetadata(TestCaseMetadataImpl.builder()
                    .withTitle("A Purpose")
                    .withDescription("Description")
                    .withTags(Collections.singletonList("TAG"))
                    .withCreationDate(creationTime)
                    .withAuthor("author")
                    .build())
                .withScenario(GwtScenario.builder().withWhen(GwtStep.NONE).build())
                .withExecutionParameters(Collections.singletonMap("aKey", "aValue"))
                .build();

            String scenarioID = sut.save(aTestCase);

            // When: we look for that scenarioTemplate
            Optional<GwtTestCase> foundScenario = sut.findById(scenarioID);

            // Then: the scenarioTemplate is found
            assertThat(foundScenario).isPresent();

            GwtTestCase testCase = foundScenario.get();
            assertThat(testCase.metadata().title()).isEqualTo("A Purpose");
            assertThat(testCase.metadata().description()).isEqualTo("Description");
            assertThat(testCase.metadata().tags()).containsExactly("TAG");
            assertThat(testCase.metadata().creationDate()).isEqualTo(creationTime);
            assertThat(testCase.scenario.when.implementation).isEmpty();
            assertThat(testCase.executionParameters()).containsOnly(entry("aKey", "aValue"));
            assertThat(testCase.metadata().author()).isEqualTo("author");
            assertThat(testCase.metadata().updateDate()).isEqualTo(creationTime);
            assertThat(testCase.metadata().version()).isEqualTo(1);
        }

        @Test
        public void should_not_find_scenario_if_id_is_not_present() {
            // When: we look for a not existed scenarioTemplate
            Optional<GwtTestCase> optScenario = sut.findById("0");

            // Then: the scenarioTemplate is not found
            assertThat(optScenario).isEmpty();
        }

        @Test
        public void should_not_find_removed_standalone_scenario_but_still_exist_in_database() {
            // Given: a scenarioTemplate in the repository
            String scenarioID = sut.save(GWT_TEST_CASE);

            // When: the scenarioTemplate is removed
            sut.removeById(scenarioID);

            // Then: the scenarioTemplate is not found in the repository
            Optional<GwtTestCase> removedScenario = sut.findById(scenarioID);
            assertThat(removedScenario).isEmpty();

            List<Long> foundScenario = entityManager.createQuery("SELECT s.id FROM SCENARIO s WHERE s.id = :id", Long.class)
                .setParameter("id", valueOf(scenarioID))
                .getResultList();

            assertThat(foundScenario).containsExactly(valueOf(scenarioID));
        }

        @Test
        public void should_not_find_removed_scenario_used_in_campaign() {
            // Given: a scenarioTemplate in the repository with campaign association and existing execution
            Scenario scenario = givenScenario();
            CampaignEntity campaign = givenCampaign(scenario);

            ScenarioExecutionEntity scenarioExecution = givenScenarioExecution(scenario.getId(), ServerReportStatus.NOT_EXECUTED);

            // When: the scenarioTemplate is removed
            sut.removeById(scenario.getId().toString());

            // Then: the scenarioTemplate is not found in the repository
            Optional<GwtTestCase> noScenario = sut.findById(scenario.getId().toString());
            assertThat(noScenario).isEmpty();

            Number executionsCount = (Number) entityManager.createNativeQuery(
                "SELECT count(*) as count FROM SCENARIO_EXECUTIONS WHERE SCENARIO_ID = '" + scenario.getId() + "'").getSingleResult();
            assertThat(executionsCount.intValue()).isOne();

            Number campaignAssociationCount = (Number) entityManager.createNativeQuery(
                "SELECT count(*) as count FROM CAMPAIGN_SCENARIOS WHERE SCENARIO_ID = '" + scenario.getId() + "'").getSingleResult();
            assertThat(campaignAssociationCount.intValue()).isZero();
        }

        @Test
        public void should_not_find_removed_scenario_metadata_when_findAll() {
            // Given: 2 scenarios in the repository
            GwtTestCaseBuilder keptScenario = GwtTestCase.builder().from(GWT_TEST_CASE)
                .withMetadata(
                    TestCaseMetadataImpl.builder()
                        .withDescription("Will be kept")
                        .withTags(Collections.singletonList("T1"))
                        .build()
                );
            String keptScenarioId = sut.save(keptScenario.build());
            GwtTestCaseBuilder deletedScenario = GwtTestCase.builder().from(GWT_TEST_CASE)
                .withMetadata(
                    TestCaseMetadataImpl.builder()
                        .withId(String.valueOf(parseInt(keptScenarioId) + 1))
                        .withDescription("Will be deleted")
                        .withTags(Collections.singletonList("T2"))
                        .build()
                );
            String deletedScenarioId = sut.save(deletedScenario.build());

            // When
            sut.removeById(deletedScenarioId);

            // Then
            List<TestCaseMetadata> allIndexes = sut.findAll();

            assertThat(allIndexes).extracting("id")
                .doesNotContain(deletedScenarioId)
                .contains(keptScenarioId);
        }

        public static Object[] parametersForShould_update_scenario_fields() {
            TestCaseMetadataImpl.TestCaseMetadataBuilder metaBuilder = TestCaseMetadataImpl.builder();

            return new Object[]{
                new Object[]{
                    "Modified title", GwtTestCase.builder().from(GWT_TEST_CASE).withMetadata(metaBuilder.withTitle("New Title").build()).build()
                },
                new Object[]{
                    "Modified content", GwtTestCase.builder().from(GWT_TEST_CASE).withScenario(GwtScenario.builder().withWhen(GwtStep.NONE).build()).build()
                },
                new Object[]{
                    "Modified description", GwtTestCase.builder().from(GWT_TEST_CASE).withMetadata(metaBuilder.withDescription("New desc").build()).build()
                },
                new Object[]{
                    "Modified tags", GwtTestCase.builder().from(GWT_TEST_CASE).withMetadata(metaBuilder.withTags(Arrays.asList("Modif T1", "Modif T2")).build()).build()
                },
                new Object[]{
                    "Modified dataSet", GwtTestCase.builder().from(GWT_TEST_CASE).withExecutionParameters(Collections.singletonMap("aKey", "aValue")).build()
                },
                new Object[]{
                    "Modified author", GwtTestCase.builder().from(GWT_TEST_CASE).withMetadata(metaBuilder.withAuthor("newAuthor").build()).build()
                }
            };
        }

        @ParameterizedTest
        @MethodSource("parametersForShould_update_scenario_fields")
        public void should_update_scenario_fields_but_creation_date(String testName, GwtTestCase modifiedTestCase) {
            // Given: an existing scenarioTemplate in the repository
            final String scenarioId = sut.save(GWT_TEST_CASE);
            GwtTestCase modifiedTestCaseWithId = GwtTestCase.builder().from(modifiedTestCase).withMetadata(TestCaseMetadataBuilder.from(modifiedTestCase.metadata)
                    .withId(scenarioId).build())
                .build();

            // When: the scenarioTemplate is updated in the repository
            sut.save(modifiedTestCaseWithId);

            // Then: the modified scenarioTemplate is found in the repository
            TestCase repositoryScenario = sut.findById(scenarioId).orElseThrow();

            assertThat(repositoryScenario)
                .as(testName)
                .usingRecursiveComparison().ignoringFields("metadata.version", "metadata.updateDate", "metadata.creationDate")
                .isEqualTo(modifiedTestCaseWithId);

            assertThat(repositoryScenario.metadata().creationDate())
                .as(testName)
                .isCloseTo(GWT_TEST_CASE.metadata.creationDate(), within(1, MILLIS));
        }

        @Test
        public void should_throw_exception_when_updating_wrong_scenario_version() {
            // Given
            final String scenarioId = sut.save(GWT_TEST_CASE);
            final Integer newVersion = 4;
            GwtTestCase newTestCase = GwtTestCase.builder().from(GWT_TEST_CASE)
                .withMetadata(TestCaseMetadataImpl.builder().withId(scenarioId).withVersion(newVersion).build())
                .build();

            // When / Then
            assertThatThrownBy(() -> sut.save(newTestCase))
                .isInstanceOf(ScenarioNotFoundException.class)
                .hasMessageContainingAll(scenarioId, newVersion.toString());
        }

        @Test
        public void should_save_scenario_without_given_id() {
            // Given
            final String scenarioId = sut.save(GWT_TEST_CASE);
            GwtTestCase testCase = GwtTestCase.builder().from(GWT_TEST_CASE)
                .withMetadata(TestCaseMetadataImpl.builder().build())
                .build();

            // When
            final String savedScenarioId = sut.save(testCase);

            // Then
            assertThat(scenarioId).isNotEqualTo(savedScenarioId);
            assertThat(Long.parseLong(scenarioId)).isEqualTo(Long.parseLong(savedScenarioId) - 1);
        }

        @Test
        public void should_save_scenario_with_given_id() {
            // Given
            final String scenarioId = sut.save(GWT_TEST_CASE);
            final String newScenarioId = "12345";
            GwtTestCase testCase = GwtTestCase.builder().from(GWT_TEST_CASE)
                .withMetadata(TestCaseMetadataImpl.builder().withId(newScenarioId).build())
                .build();

            // When
            final String savedScenarioId = sut.save(testCase);

            // Then
            assertThat(scenarioId).isNotEqualTo(savedScenarioId);
            assertThat(savedScenarioId + 1).isNotEqualTo(scenarioId); // Make sure there is no autoincrement
            assertThat(savedScenarioId).isEqualTo(newScenarioId);
        }

        @Test
        public void should_update_scenario_without_modifying_id() {
            // Given
            final String scenarioId = sut.save(GWT_TEST_CASE);
            String title = "New title";
            GwtTestCase testCase = GwtTestCase.builder().from(GWT_TEST_CASE)
                .withMetadata(TestCaseMetadataImpl.builder().withId(scenarioId).withTitle(title).build())
                .build();

            // When
            final String savedScenarioId = sut.save(testCase);

            // Then
            assertThat(scenarioId).isEqualTo(savedScenarioId);
            assertThat(sut.findById(scenarioId).orElseThrow(NoSuchElementException::new).metadata.title).isEqualTo(title);
        }

        @Test
        public void should_increment_version_and_update_date_on_each_update() {
            // Given
            Instant creationDate = GWT_TEST_CASE.metadata().creationDate();

            // When
            final String scenarioId = sut.save(GWT_TEST_CASE);

            // Then
            TestCase repositoryScenario = sut.findById(scenarioId).orElseThrow();
            assertThat(repositoryScenario.metadata().version()).isEqualTo(1);
            assertThat(repositoryScenario.metadata().updateDate().truncatedTo(MILLIS)).isEqualTo(creationDate.truncatedTo(MILLIS));
            Instant updateDate = repositoryScenario.metadata().updateDate();

            for (int i = 1; i < 10; i++) {
                awaitDuring(10, MILLISECONDS);

                // When
                GwtTestCase newTestCase = GwtTestCase.builder().from(GWT_TEST_CASE)
                    .withMetadata(TestCaseMetadataImpl.builder()
                        .withId(scenarioId)
                        .withVersion(i)
                        .build())
                    .build();

                sut.save(newTestCase);

                // Then#
                TestCase readScenario = sut.findById(scenarioId).orElseThrow();
                assertThat(readScenario.metadata().version()).isEqualTo(i + 1);
                assertThat(readScenario.metadata().updateDate()).isAfter(updateDate);

                updateDate = Instant.from(readScenario.metadata().updateDate());
            }
        }

        @Test
        public void should_get_last_version_from_id() {
            // ----- Unknown testcase
            // When / Then
            assertThat(sut.lastVersion("unknownId")).isNotNull();

            // ----- Multiple updates
            // Given first save
            final String scenarioId = sut.save(GWT_TEST_CASE);

            // When / Then
            assertThat(sut.lastVersion(scenarioId).orElseThrow()).isEqualTo(1);

            // Given first update
            GwtTestCase newTestCase = GwtTestCase.builder().from(GWT_TEST_CASE)
                .withMetadata(TestCaseMetadataImpl.builder().withId(scenarioId).withVersion(1).build())
                .build();

            sut.save(newTestCase);

            // When / Then
            assertThat(sut.lastVersion(scenarioId).orElseThrow()).isEqualTo(2);
        }

        @Test
        public void should_not_persist_default_author() {
            // When
            final String scenarioId = sut.save(GWT_TEST_CASE);

            //Then
            List<String> foundScenario = entityManager.createQuery("SELECT s.userId FROM SCENARIO s WHERE s.id = :id", String.class)
                .setParameter("id", valueOf(scenarioId))
                .getResultList();

            assertThat(foundScenario).hasSize(1).containsOnlyNulls();

            Optional<GwtTestCase> testCaseById = sut.findById(scenarioId);
            assertThat(testCaseById).hasValueSatisfying(tc -> assertThat(tc.metadata().author()).isEqualTo(User.ANONYMOUS.id));
        }

        @Test
        public void should_search_scenario() {
            GwtTestCase GWT_TEST_CASE = GwtTestCase.builder()
                .withMetadata(TestCaseMetadataImpl.builder()
                    .build())
                .withExecutionParameters(Collections.emptyMap())
                .withScenario(
                    GwtScenario.builder()
                        .withGivens(List.of(GwtStep.builder().withDescription("chutney of momos").build()))
                        .withWhen(GwtStep.NONE).build()
                ).build();
            // Given
            String scenarioID = sut.save(GWT_TEST_CASE);

            // When
            List<TestCaseMetadata> raw = sut.search("momos");
            // Then
            assertThat(raw).hasSize(1).extracting("id").containsExactly(scenarioID);

            // When
            List<TestCaseMetadata> raw2 = sut.search("curry");
            // Then
            assertThat(raw2).isEmpty();
        }

        @Test
        public void should_split_in_3_words_when_search_without_nested_quote() {
            // When
            List<String> words = sut.getWordsToSearchWithQuotes("toto tutu tata");

            // Then
            assertThat(words).isEqualTo(List.of("toto", "tutu", "tata"));
        }

        @Test
        public void should_return_empty_list_when_only_with_nested_quote() {
            // When
            List<String> words = sut.getWordsToSearchWithQuotes("\"\"");

            // Then
            assertThat(words).isEqualTo(List.of());
        }

        @Test
        public void should_not_split_quoted_words() {
            // When
            List<String> words = sut.getWordsToSearchWithQuotes("\"toto titi tutu tata\"");

            // Then
            assertThat(words).isEqualTo(List.of("toto titi tutu tata"));
        }

        @Test
        public void should_split_in_3_words_when_search_with_nested_quote() {
            // When
            List<String> words = sut.getWordsToSearchWithQuotes("\"toto titi\" tutu tata");

            // Then
            assertThat(words).isEqualTo(List.of("toto titi", "tutu", "tata"));
        }

        @Test
        public void should_split_in_3_words_when_search_with_multiple_nested_quote() {
            // When
            List<String> words = sut.getWordsToSearchWithQuotes("\"toto tutu\" \"titi tata\" baba");

            // Then
            assertThat(words).isEqualTo(List.of("toto tutu", "titi tata", "baba"));
        }

        @Test
        public void should_split_in_3_words_when_search_with_1_nested_quote() {
            // When
            List<String> words = sut.getWordsToSearchWithQuotes("toto titi\" tutu tata");

            // Then
            assertThat(words).isEqualTo(List.of("toto", "titi\"", "tutu", "tata"));
        }
    }
}
