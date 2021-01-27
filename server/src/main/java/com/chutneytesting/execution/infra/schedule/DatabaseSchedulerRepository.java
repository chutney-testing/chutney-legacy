package com.chutneytesting.execution.infra.schedule;

import com.chutneytesting.design.domain.campaign.Campaign;
import com.chutneytesting.execution.domain.schedule.SchedulerRepository;
import com.google.common.collect.ImmutableMap;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class DatabaseSchedulerRepository implements SchedulerRepository {

    private Clock clock;
    private final NamedParameterJdbcTemplate uiNamedParameterJdbcTemplate;
    private static final CampaignScheduledRowMapper campaignScheduledRowMapper = new CampaignScheduledRowMapper();

    public DatabaseSchedulerRepository(NamedParameterJdbcTemplate uiNamedParameterJdbcTemplate, Clock clock) {
        this.uiNamedParameterJdbcTemplate = uiNamedParameterJdbcTemplate;
        this.clock = clock;
    }

    @Override
    public List<Long> getCampaignScheduledAfter(LocalDateTime lastExecutionDateTime) {
        final String sql = "SELECT C.ID, C.SCHEDULE_TIME " +
            "FROM CAMPAIGN C WHERE C.SCHEDULE_TIME is not null";

        final List<Pair<Long, LocalTime>> allCampaigns = uiNamedParameterJdbcTemplate.query(sql
            , ImmutableMap.of()
            , campaignScheduledRowMapper);

        return allCampaigns.stream()
            .filter(p -> p.getRight() != null)
            .filter(p -> buildScheduleDateTime(p.getRight()).isAfter(lastExecutionDateTime) &&
                buildScheduleDateTime(p.getRight()).isBefore(LocalDateTime.now(clock)))
            .map(Pair::getLeft)
            .collect(Collectors.toList());
    }

    private LocalDateTime buildScheduleDateTime(LocalTime localTime) {
        LocalDateTime now = LocalDateTime.now(clock);
        LocalDate today = now.toLocalDate();
        LocalDate yesterday = today.minusDays(1);

        if (LocalDateTime.of(today, localTime).isBefore(now))
            return LocalDateTime.of(today, localTime);
        else
            return LocalDateTime.of(yesterday, localTime);
    }

    private static class CampaignScheduledRowMapper implements RowMapper<Pair<Long, LocalTime>> {
        @Override
        public Pair<Long, LocalTime> mapRow(ResultSet rs, int rowNum) throws SQLException {
            final Long id = rs.getLong("ID");
            final String scheduledTime = rs.getString("SCHEDULE_TIME");
            return Pair.of(id, LocalTime.parse(scheduledTime, Campaign.formatter));
        }
    }
}
