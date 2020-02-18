package com.chutneytesting.instrument.infra.storage;

import com.chutneytesting.design.infra.storage.scenario.jdbc.ScenarioTagListMapper;
import com.chutneytesting.execution.domain.report.ServerReportStatus;
import com.chutneytesting.instrument.domain.Metrics;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class MetricsLoader implements ApplicationListener<ApplicationStartedEvent> {

    private final JdbcTemplate jdbcTemplate;

    MetricsLoader(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    void initialize(Metrics metrics) {
        scenarioTitleAndTags()
            .forEach(scenarioTitleAndTags -> {
                metrics.onNewScenario(scenarioTitleAndTags.title, scenarioTitleAndTags.tags);
            });

        jdbcTemplate.queryForList("SELECT s.TITLE, h.STATUS, h.DURATION FROM SCENARIO s "
            + "JOIN SCENARIO_EXECUTION_HISTORY h ON (s.ID = h.SCENARIO_ID) "
            + "LEFT OUTER JOIN SCENARIO_EXECUTION_HISTORY h2 ON (s.ID = h2.SCENARIO_ID AND "
            + "  (h.EXECUTION_TIME < h2.EXECUTION_TIME OR h.EXECUTION_TIME = h2.EXECUTION_TIME AND h.ID < h2.ID)) "
            + "WHERE h2.ID IS NULL"
        ).forEach(row -> {
            metrics.onExecutionEnded((String) row.get("TITLE"), ServerReportStatus.valueOf((String) row.get("STATUS")), ((Number) row.get("DURATION")).longValue());
        });
    }

    Set<ScenarioTitleAndTags> scenarioTitleAndTags() {
        return new HashSet<>(jdbcTemplate.query("SELECT TITLE, TAGS FROM SCENARIO", (rs, rowNum) -> new ScenarioTitleAndTags(rs.getString("TITLE"), ScenarioTagListMapper.tagsStringToList(rs.getString("TAGS")))));
    }

    public Map<String, Integer> scenarioCountsByTag() {
        Map<String, Integer> scenarioCountsByTag = new HashMap<>();
        for (ScenarioTitleAndTags scenarioTitleAndTags : scenarioTitleAndTags()) {
            for (String tag : scenarioTitleAndTags.tags) {
                if (!scenarioCountsByTag.containsKey(tag)) {
                    scenarioCountsByTag.put(tag, 0);
                }
                scenarioCountsByTag.compute(tag, (key, previousCount) -> previousCount + 1);
            }
        }
        return scenarioCountsByTag;
    }

    @Override
    public void onApplicationEvent(ApplicationStartedEvent event) {
        Metrics metrics = event.getApplicationContext().getBean(Metrics.class);
        initialize(metrics);
    }


    static class ScenarioTitleAndTags {
        final String title;
        final Set<String> tags;

        ScenarioTitleAndTags(String title, List<String> tags) {
            this.title = title;
            this.tags = new HashSet<>(tags);
        }
    }
}
