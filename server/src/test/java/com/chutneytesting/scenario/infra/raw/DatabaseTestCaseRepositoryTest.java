package com.chutneytesting.scenario.infra.raw;

import static com.chutneytesting.tools.WaitUtils.awaitDuring;
import static java.lang.Long.valueOf;
import static java.time.temporal.ChronoUnit.MILLIS;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.entry;
import static org.assertj.core.api.Assertions.within;

import com.chutneytesting.scenario.domain.gwt.GwtScenario;
import com.chutneytesting.scenario.domain.gwt.GwtStep;
import com.chutneytesting.scenario.domain.gwt.GwtTestCase;
import com.chutneytesting.scenario.domain.gwt.GwtTestCase.GwtTestCaseBuilder;
import com.chutneytesting.server.core.domain.scenario.ScenarioNotFoundException;
import com.chutneytesting.server.core.domain.scenario.TestCase;
import com.chutneytesting.server.core.domain.scenario.TestCaseMetadata;
import com.chutneytesting.server.core.domain.scenario.TestCaseMetadataImpl;
import com.chutneytesting.server.core.domain.scenario.TestCaseMetadataImpl.TestCaseMetadataBuilder;
import com.chutneytesting.server.core.domain.security.User;
import com.chutneytesting.tests.AbstractLocalDatabaseTest;
import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import javax.persistence.EntityManager;
import liquibase.exception.LiquibaseException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

@EnableJpaRepositories(basePackages = "com.chutneytesting.scenario.infra",
    includeFilters = {@ComponentScan.Filter(type = FilterType.REGEX, pattern = "^.*JpaRepository$")}
)
@ComponentScan(basePackages = {"com.chutneytesting.scenario.infra"})
public class DatabaseTestCaseRepositoryTest extends AbstractLocalDatabaseTest {

    private static final GwtTestCase GWT_TEST_CASE = GwtTestCase.builder()
        .withMetadata(TestCaseMetadataImpl.builder().build())
        .withExecutionParameters(Collections.emptyMap())
        .withScenario(
            GwtScenario.builder().withWhen(GwtStep.NONE).build()
        ).build();

    @Autowired
    private DatabaseTestCaseRepository repository;

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private PlatformTransactionManager transactionManager;

    private TransactionTemplate transactionTemplate;

    @BeforeEach
    void setUp() {
        transactionTemplate = new TransactionTemplate(transactionManager);
    }

    @Test
    public void should_generate_id_when_scenario_is_persisted() {
        // When: a scenarioTemplate is saved
        String scenarioID = repository.save(GWT_TEST_CASE);

        // Then: a non blank id is generated
        assertThat(scenarioID).isNotBlank();
    }

    @Test
    public void should_not_increase_sequence_at_restart() throws LiquibaseException {
        // Given
        repository.save(GWT_TEST_CASE);
        repository.save(GWT_TEST_CASE);
        repository.save(GWT_TEST_CASE);
        String scenarioId1 = repository.save(GWT_TEST_CASE);

        // When redo liquibase
        liquibaseUpdate();

        // Then
        String scenarioId2 = repository.save(GWT_TEST_CASE);
        assertThat(Integer.valueOf(scenarioId2)).isEqualTo(Integer.valueOf(scenarioId1) + 1);


        // When redo liquibase
        liquibaseUpdate();

        // Then
        String scenarioId3 = repository.save(GWT_TEST_CASE);
        assertThat(Integer.valueOf(scenarioId3)).isEqualTo(Integer.valueOf(scenarioId2) + 1);
    }

    @Test
    public void should_find_scenario_by_id() {
        // Given: a scenarioTemplate in the repository
        String scenarioID = repository.save(GWT_TEST_CASE);


        // When: we look for that scenarioTemplate
        Optional<GwtTestCase> optScena = repository.findById(scenarioID);

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

        String scenarioID = repository.save(aTestCase);

        // When: we look for that scenarioTemplate
        Optional<GwtTestCase> optScena = repository.findById(scenarioID);

        // Then: the scenarioTemplate is found
        assertThat(optScena).isPresent();


        GwtTestCase testCase = (GwtTestCase) optScena.get();
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
        Optional<GwtTestCase> optScenario = repository.findById("0");

        // Then: the scenarioTemplate is not found
        assertThat(optScenario).isEmpty();
    }

    @Test
    public void should_not_find_removed_standalone_scenario_but_still_exist_in_database() {
        // Given: a scenarioTemplate in the repository
        String scenarioID = repository.save(GWT_TEST_CASE);

        // When: the scenarioTemplate is removed
        repository.removeById(scenarioID);

        // Then: the scenarioTemplate is not found in the repository
        Optional<GwtTestCase> optScena = repository.findById(scenarioID);
        assertThat(optScena).isEmpty();

        List<Long> foundScenario = entityManager.createQuery("SELECT s.id FROM SCENARIO s WHERE s.id = :id")
            .setParameter("id", valueOf(scenarioID))
            .getResultList();

        assertThat(foundScenario).hasSize(1).containsExactly(valueOf(scenarioID));
    }

    @Test
    public void should_not_find_removed_scenario_used_in_campaign() {
        // Given: a scenarioTemplate in the repository with campaign association and existing execution
        String scenarioID = repository.save(GWT_TEST_CASE);
        createCampaignWithScenarioExecution(1L, scenarioID, 1L, 1L);

        // When: the scenarioTemplate is removed
        repository.removeById(scenarioID);

        // Then: the scenarioTemplate is not found in the repository
        Optional<GwtTestCase> optScena = repository.findById(scenarioID);
        assertThat(optScena).isEmpty();
    }

    @Test
    public void should_not_find_removed_scenario_metadata_when_findAll() {
        transactionTemplate.execute(status ->
            entityManager.createQuery("DELETE FROM SCENARIO s").executeUpdate()
        );

        // Given: 2 scenarios in the repository
        GwtTestCaseBuilder anotherScenario = GwtTestCase.builder().from(GWT_TEST_CASE)
            .withMetadata(
                TestCaseMetadataImpl.builder()
                    .withDescription("Will be kept")
                    .withTags(Collections.singletonList("T1"))
                    .build()
            );
        String anotherScenarioId = repository.save(anotherScenario.build());
        GwtTestCaseBuilder deletedScenario = GwtTestCase.builder().from(GWT_TEST_CASE)
            .withMetadata(
                TestCaseMetadataImpl.builder()
                    .withId(String.valueOf(Integer.valueOf(anotherScenarioId) + 1))
                    .withDescription("Will be deleted")
                    .withTags(Collections.singletonList("T2"))
                    .build()
            );
        String deletedScenarioId = repository.save(deletedScenario.build());

        // When
        repository.removeById(deletedScenarioId);

        // Then
        List<TestCaseMetadata> allIndexes = repository.findAll();

        assertThat(allIndexes).hasSize(1);
        assertThat(allIndexes.get(0).id()).isEqualTo(anotherScenarioId);
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
        final String scenarioId = repository.save(GWT_TEST_CASE);
        GwtTestCase modifiedTestCaseWithId = GwtTestCase.builder().from(modifiedTestCase).withMetadata(TestCaseMetadataBuilder.from(modifiedTestCase.metadata)
                .withId(scenarioId).build())
            .build();

        // When: the scenarioTemplate is updated in the repository
        repository.save(modifiedTestCaseWithId);

        // Then: the modified scenarioTemplate is found in the repository
        TestCase repositoryScenario = repository.findById(scenarioId).get();

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
        final String scenarioId = repository.save(GWT_TEST_CASE);
        final Integer newVersion = 4;
        GwtTestCase newTestCase = GwtTestCase.builder().from(GWT_TEST_CASE)
            .withMetadata(TestCaseMetadataImpl.builder().withId(scenarioId).withVersion(newVersion).build())
            .build();

        // When / Then
        assertThatThrownBy(() -> repository.save(newTestCase))
            .isInstanceOf(ScenarioNotFoundException.class)
            .hasMessageContainingAll(scenarioId, newVersion.toString());
    }

    @Test
    public void should_increment_version_and_update_date_on_each_update() {
        // Given
        Instant creationDate = GWT_TEST_CASE.metadata().creationDate();

        // When
        final String scenarioId = repository.save(GWT_TEST_CASE);

        // Then
        TestCase repositoryScenario = repository.findById(scenarioId).get();
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

            repository.save(newTestCase);

            // Then#
            TestCase readScenario = repository.findById(scenarioId).get();
            assertThat(readScenario.metadata().version()).isEqualTo(i + 1);
            assertThat(readScenario.metadata().updateDate()).isAfter(updateDate);

            updateDate = Instant.from(readScenario.metadata().updateDate());
        }
    }

    @Test
    public void should_get_last_version_from_id() {
        // ----- Unknown testcase
        // When / Then
        assertThat(repository.lastVersion("unknownId")).isNotNull();

        // ----- Multiple updates
        // Given first save
        final String scenarioId = repository.save(GWT_TEST_CASE);

        // When / Then
        assertThat(repository.lastVersion(scenarioId).get()).isEqualTo(1);

        // Given first update
        GwtTestCase newTestCase = GwtTestCase.builder().from(GWT_TEST_CASE)
            .withMetadata(TestCaseMetadataImpl.builder().withId(scenarioId).withVersion(1).build())
            .build();

        repository.save(newTestCase);

        // When / Then
        assertThat(repository.lastVersion(scenarioId).get()).isEqualTo(2);
    }

    @Test
    public void should_not_persist_default_author() {
        // When
        final String scenarioId = repository.save(GWT_TEST_CASE);

        //Then
        List<String> foundScenario = entityManager.createQuery("SELECT s.userId FROM SCENARIO s WHERE s.id = :id")
            .setParameter("id", valueOf(scenarioId))
            .getResultList();

        assertThat(foundScenario).hasSize(1);
        assertThat(foundScenario.get(0)).isNull();

        Optional<GwtTestCase> testCaseById = repository.findById(scenarioId);
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
        String scenarioID = repository.save(GWT_TEST_CASE);

        // When
        List<TestCaseMetadata> raw = repository.search("momos");
        // Then
        assertThat(raw).hasSize(1);
        assertThat(raw.get(0).id()).isEqualTo(scenarioID);

        // When
        List<TestCaseMetadata> raw2 = repository.search("curry");
        // Then
        assertThat(raw2).hasSize(0);
    }

}
