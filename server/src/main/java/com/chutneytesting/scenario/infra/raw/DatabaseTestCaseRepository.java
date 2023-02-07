package com.chutneytesting.scenario.infra.raw;

import static com.chutneytesting.scenario.infra.jpa.ScenarioDao.fromTestCaseData;
import static com.google.common.base.Strings.isNullOrEmpty;
import static java.lang.Long.valueOf;
import static java.util.Optional.empty;
import static java.util.Optional.of;
import static java.util.stream.Collectors.toList;
import static org.apache.commons.lang3.StringUtils.isNumeric;

import com.chutneytesting.scenario.domain.gwt.GwtTestCase;
import com.chutneytesting.scenario.infra.jpa.ScenarioDao;
import com.chutneytesting.server.core.domain.scenario.AggregatedRepository;
import com.chutneytesting.server.core.domain.scenario.ScenarioNotFoundException;
import com.chutneytesting.server.core.domain.scenario.TestCase;
import com.chutneytesting.server.core.domain.scenario.TestCaseMetadata;
import com.google.common.collect.ImmutableMap;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.apache.commons.lang.StringEscapeUtils;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Repository
public class DatabaseTestCaseRepository implements AggregatedRepository<GwtTestCase> {

    private final NamedParameterJdbcTemplate uiNamedParameterJdbcTemplate;
    private final DatabaseTestCaseRepositoryDao jpa;

    public DatabaseTestCaseRepository(NamedParameterJdbcTemplate uiNamedParameterJdbcTemplate,
                                      DatabaseTestCaseRepositoryDao jpa) {

        this.uiNamedParameterJdbcTemplate = uiNamedParameterJdbcTemplate;
        this.jpa = jpa;
    }

    @Override
   //@Transactional(propagation = Propagation.NOT_SUPPORTED)
    public String save(GwtTestCase testCase) {
        TestCaseData testCaseData = TestCaseDataMapper.toDto(testCase);
        return doSave(testCaseData).toString();
    }

    @Override
    public Optional<GwtTestCase> findById(String scenarioId) {
        if(checkIdInput(scenarioId)) {
            return empty();
        }
        try {
            Optional<ScenarioDao> scenarioDao = jpa.findById(valueOf(scenarioId));
            return scenarioDao.map(ScenarioDao::toGwtTestCase);
        } catch (IncorrectResultSizeDataAccessException e) {
            return empty();
        }
    }

    @Override
    public Optional<TestCase> findExecutableById(String id) {
        if(checkIdInput(id)) {
            return empty();
        }
        Optional<GwtTestCase> byId = findById(id);
        if (byId.isPresent()) {
            return of(byId.get());
        } else {
            return empty();
        }
    }

    @Override
    public Optional<TestCaseMetadata> findMetadataById(String testCaseId) {
        return findById(testCaseId).map(t -> t.metadata());
    }

    @Override
    public List<TestCaseMetadata> findAll() {
        return jpa.findAll().stream().map(ScenarioDao::toTestCaseMetadata).collect(toList());
    }

    @Override
    @Transactional
    public void removeById(String scenarioId) {
        if(checkIdInput(scenarioId)) {
            return;
        }
        // TODO - Refactor - Use CampaignRepository up in callstack
        uiNamedParameterJdbcTemplate.update("DELETE FROM CAMPAIGN_EXECUTION_HISTORY WHERE SCENARIO_ID = :id", buildIdParameterMap(scenarioId));
        uiNamedParameterJdbcTemplate.update("DELETE FROM CAMPAIGN_SCENARIOS WHERE SCENARIO_ID = :id", buildIdParameterMap(scenarioId));
        jpa.deactivateScenario(valueOf(scenarioId));
    }

    @Override
    public Optional<Integer> lastVersion(String scenarioId) {
        if(checkIdInput(scenarioId)) {
            return empty();
        }
        try {
            return jpa.getLastVersion(valueOf(scenarioId));
        } catch (IncorrectResultSizeDataAccessException e) {
            return empty();
        }
    }

    @Override
    public List<TestCaseMetadata> search(String textFilter) {
        if (!textFilter.isEmpty()) {
            String[] words = StringEscapeUtils.escapeSql(textFilter).split("\\s");
            Specification<ScenarioDao> scenarioDaoSpecification = buildLikeSpecificationOnContent(words);
            List<ScenarioDao> all = jpa.findAll(scenarioDaoSpecification);
            return all.stream().map(ScenarioDao::toTestCaseMetadata).collect(Collectors.toList());
        } else {
            return findAll();
        }
    }

    private Specification<ScenarioDao> buildLikeSpecificationOnContent(String[] words) {
        Specification<ScenarioDao> scenarioDaoSpecification = null;
        for(String word : words) {
            Specification<ScenarioDao> wordSpecification = DatabaseTestCaseRepositoryDao.contentContains(word);
            if(scenarioDaoSpecification == null) {
                scenarioDaoSpecification = wordSpecification;
            } else {
                scenarioDaoSpecification = scenarioDaoSpecification.or(wordSpecification);
            }
        }
        return scenarioDaoSpecification;
    }

    private Long doSave(TestCaseData scenario) {
        try {
            return jpa.save(fromTestCaseData(scenario)).getId();
        } catch(ObjectOptimisticLockingFailureException e) {
            throw new ScenarioNotFoundException(scenario.id, scenario.version);
        }
    }

    private ImmutableMap<String, Object> buildIdParameterMap(String scenarioId) {
        return ImmutableMap.<String, Object>builder().put("id", scenarioId).build();
    }

    private boolean checkIdInput(String scenarioId) {
        return isNullOrEmpty(scenarioId) || !isNumeric(scenarioId);
    }
}
