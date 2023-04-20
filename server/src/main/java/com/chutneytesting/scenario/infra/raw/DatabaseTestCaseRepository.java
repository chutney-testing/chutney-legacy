package com.chutneytesting.scenario.infra.raw;

import static com.google.common.base.Strings.isNullOrEmpty;
import static java.lang.Long.valueOf;
import static java.util.Optional.empty;
import static java.util.Optional.ofNullable;
import static org.apache.commons.lang3.StringUtils.isNumeric;

import com.chutneytesting.scenario.domain.gwt.GwtTestCase;
import com.chutneytesting.scenario.infra.jpa.Scenario;
import com.chutneytesting.server.core.domain.scenario.AggregatedRepository;
import com.chutneytesting.server.core.domain.scenario.ScenarioNotFoundException;
import com.chutneytesting.server.core.domain.scenario.TestCase;
import com.chutneytesting.server.core.domain.scenario.TestCaseMetadata;
import com.google.common.collect.ImmutableMap;
import java.util.List;
import java.util.Optional;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public class DatabaseTestCaseRepository implements AggregatedRepository<GwtTestCase> {

    private final NamedParameterJdbcTemplate uiNamedParameterJdbcTemplate;
    private final DatabaseTestCaseJpaRepository scenarioJpaRepository;

    public DatabaseTestCaseRepository(NamedParameterJdbcTemplate uiNamedParameterJdbcTemplate,
                                      DatabaseTestCaseJpaRepository jpa) {

        this.uiNamedParameterJdbcTemplate = uiNamedParameterJdbcTemplate;
        this.scenarioJpaRepository = jpa;
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
                // TODO - Refactor - Use CampaignRepository up in callstack
                uiNamedParameterJdbcTemplate.update("DELETE FROM CAMPAIGN_EXECUTION_HISTORY WHERE SCENARIO_ID = :id", buildIdParameterMap(scenarioId));
                scenarioJpa.campaigns().clear();
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

    private ImmutableMap<String, Object> buildIdParameterMap(String scenarioId) {
        return ImmutableMap.<String, Object>builder().put("id", scenarioId).build();
    }

    private boolean checkIdInput(String scenarioId) {
        return isNullOrEmpty(scenarioId) || !isNumeric(scenarioId);
    }
}
