package com.chutneytesting.execution.infra.schedule;

import static org.assertj.core.api.Assertions.assertThat;

import com.chutneytesting.design.domain.campaign.Campaign;
import com.chutneytesting.tests.AbstractLocalDatabaseTest;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class DatabaseSchedulerRepositoryTest extends AbstractLocalDatabaseTest {

    private DatabaseSchedulerRepository schedulerRepo;
    private Clock clock;

    @BeforeEach
    public void setUp() {
        this.clock = Clock.fixed(LocalDate.now().atStartOfDay().toInstant(ZoneOffset.ofHours(1)), ZoneId.systemDefault());
        schedulerRepo = new DatabaseSchedulerRepository(namedParameterJdbcTemplate, clock);
    }

    @Test
    public void should_return_campaign_scheduled_between_last_30_minutes_and_now() {

        final LocalTime now = LocalTime.now(clock);
        createCampaign(42L, now.minusMinutes(1));
        createCampaign(43L, now.plusMinutes(1));
        createCampaign(44L, now.minusMinutes(31));
        createCampaign(45L, null);

        LocalDateTime thirtyMinuteAgo = LocalDateTime.now(clock).minusMinutes(30);
        final List<Long> campaignScheduledAfter = schedulerRepo.getCampaignScheduledAfter(thirtyMinuteAgo);

        assertThat(campaignScheduledAfter).containsExactly(42L);
    }

    private void createCampaign(Long id, LocalTime scheduleTime) {
        final Map<String, Object> parameters = new HashMap<>();
        parameters.put("campaignId", id);
        parameters.put("scheduledTime", scheduleTime != null ? scheduleTime.format(Campaign.formatter) : null);

        final String sql = "insert into campaign (id, title, description, schedule_time) values (:campaignId, 'test campaign', 'description', :scheduledTime)";
        namedParameterJdbcTemplate.update(sql, parameters);
    }
}
