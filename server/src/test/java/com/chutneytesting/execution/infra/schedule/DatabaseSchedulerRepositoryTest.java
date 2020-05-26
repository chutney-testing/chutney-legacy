package com.chutneytesting.execution.infra.schedule;

import static org.assertj.core.api.Assertions.assertThat;

import com.chutneytesting.tests.AbstractLocalDatabaseTest;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class DatabaseSchedulerRepositoryTest extends AbstractLocalDatabaseTest {

    private DatabaseSchedulerRepository schedulerRepo;

    @BeforeEach
    public void setUp() {
        schedulerRepo = new DatabaseSchedulerRepository(namedParameterJdbcTemplate);
    }

    @Test
    public void should_return_campaign_scheduled_between_last_30_minutes_and_now() {

        final LocalTime now = LocalTime.now();
        createCampaign(42L, now.minusMinutes(1));
        createCampaign(43L, now.plusMinutes(1));
        createCampaign(44L, now.minusMinutes(31));
        createCampaign(45L, null);

        LocalTime thirtyMinuteAgo = now.minusMinutes(30);
        final List<Long> campaignScheduledAfter = schedulerRepo.getCampaignScheduledAfter(thirtyMinuteAgo);

        assertThat(campaignScheduledAfter).containsExactly(42L);
    }

    private void createCampaign(Long id, LocalTime scheduleTime) {
        final Map<String, Object> parameters = new HashMap<>();
        parameters.put("campaignId", id);
        parameters.put("scheduledTime", scheduleTime != null ? scheduleTime.toString() : null);

        final String sql = "insert into campaign (id, title, description, schedule_time) values (:campaignId, 'test campaign', 'description', :scheduledTime)";
        namedParameterJdbcTemplate.update(sql, parameters);
    }
}
