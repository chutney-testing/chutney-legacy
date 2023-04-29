package com.chutneytesting.scenario.infra.raw;

import static com.google.common.base.Strings.isNullOrEmpty;
import static java.lang.Long.valueOf;
import static java.util.Optional.empty;
import static java.util.Optional.ofNullable;
import static org.apache.commons.lang3.StringUtils.isNumeric;

import com.chutneytesting.execution.infra.storage.DatabaseExecutionJpaRepository;
import com.chutneytesting.execution.infra.storage.jpa.ScenarioExecution;
import com.chutneytesting.scenario.domain.gwt.GwtTestCase;
import com.chutneytesting.scenario.infra.jpa.Scenario;
import com.chutneytesting.server.core.domain.scenario.AggregatedRepository;
import com.chutneytesting.server.core.domain.scenario.ScenarioNotFoundException;
import com.chutneytesting.server.core.domain.scenario.TestCase;
import com.chutneytesting.server.core.domain.scenario.TestCaseMetadata;
import java.util.List;
import java.util.Optional;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public class DatabaseTestCaseRepository implements AggregatedRepository<GwtTestCase> {

    private final DatabaseTestCaseJpaRepository scenarioJpaRepository;
    private final DatabaseExecutionJpaRepository scenarioExecutionsJpaRepository;

    public DatabaseTestCaseRepository(
        DatabaseTestCaseJpaRepository jpa,
        DatabaseExecutionJpaRepository scenarioExecutionsJpaRepository
    ) {
        this.scenarioJpaRepository = jpa;
        this.scenarioExecutionsJpaRepository = scenarioExecutionsJpaRepository;
    }

    @Override
    public String save(GwtTestCase testCase) {
        try {
            return scenarioJpaRepository.save(Scenario.fromGwtTestCase(testCase)).id().toString();
        } catch (ObjectOptimisticLockingFailureException e) {
            throw new ScenarioNotFoundException(testCase.id(), testCase.metadata().version());
        }
    }

    @Override
    public Optional<GwtTestCase> findById(String scenarioId) {
        if (checkIdInput(scenarioId)) {
            return empty();
        }
        Optional<Scenario> scenarioDao = scenarioJpaRepository.findByIdAndActivated(valueOf(scenarioId), true)
            .filter(Scenario::activated);
        return scenarioDao.map(Scenario::toGwtTestCase);
    }

    @Override
    public Optional<TestCase> findExecutableById(String id) {
        return findById(id).map(TestCase.class::cast);
    }

    @Override
    public Optional<TestCaseMetadata> findMetadataById(String testCaseId) {
        return findById(testCaseId).map(GwtTestCase::metadata);
    }

    @Override
    public List<TestCaseMetadata> findAll() {
        return scenarioJpaRepository.findByActivatedTrue().stream()
            .map(Scenario::toTestCaseMetadata)
            .toList();
    }

    @Override
    @Transactional
    public void removeById(String scenarioId) {
        if (checkIdInput(scenarioId)) {
            return;
        }
        scenarioJpaRepository.findByIdAndActivated(valueOf(scenarioId), true)
            .ifPresent(scenarioJpa -> {
                List<ScenarioExecution> allExecutions = scenarioExecutionsJpaRepository.findAllByScenarioId(valueOf(scenarioId));
                allExecutions.forEach(e -> {
                    e.forCampaignExecution(null);
                    scenarioExecutionsJpaRepository.save(e);
                });
                scenarioJpa.deactivate();
                scenarioJpaRepository.save(scenarioJpa);
            });
    }

    @Override
    public Optional<Integer> lastVersion(String scenarioId) {
        if (checkIdInput(scenarioId)) {
            return empty();
        }
        try {
            return scenarioJpaRepository.getLastVersion(valueOf(scenarioId));
        } catch (IncorrectResultSizeDataAccessException e) {
            return empty();
        }
    }

    @Override
    public List<TestCaseMetadata> search(String textFilter) {
        if (!textFilter.isEmpty()) {
            String[] words = escapeSql(textFilter).split("\\s");
            Specification<Scenario> scenarioDaoSpecification = buildLikeSpecificationOnContent(words);
            List<Scenario> all = scenarioJpaRepository.findAll(scenarioDaoSpecification);
            return all.stream().map(Scenario::toTestCaseMetadata).toList();
        } else {
            return findAll();
        }
    }

    private static String escapeSql(String str) {
        if (str == null) {
            return null;
        }
        return str.replace("'", "''");
    }

    private Specification<Scenario> buildLikeSpecificationOnContent(String[] words) {
        Specification<Scenario> scenarioDaoSpecification = null;
        for (String word : words) {
            Specification<Scenario> wordSpecification = DatabaseTestCaseJpaRepository.contentContains(word);
            scenarioDaoSpecification = ofNullable(scenarioDaoSpecification)
                .map(s -> s.or(wordSpecification))
                .orElse(wordSpecification);
        }
        return scenarioDaoSpecification;
    }

    private boolean checkIdInput(String scenarioId) {
        return isNullOrEmpty(scenarioId) || !isNumeric(scenarioId);
    }
}
