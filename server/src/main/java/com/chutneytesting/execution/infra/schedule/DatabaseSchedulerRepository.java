package com.chutneytesting.execution.infra.schedule;

import com.chutneytesting.execution.domain.schedule.SchedulerRepository;
import com.google.common.collect.ImmutableMap;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class DatabaseSchedulerRepository implements SchedulerRepository {

    private final NamedParameterJdbcTemplate uiNamedParameterJdbcTemplate;
    private static final CampaignScheduledRowMapper campaignScheduledRowMapper = new CampaignScheduledRowMapper();

    public DatabaseSchedulerRepository(NamedParameterJdbcTemplate uiNamedParameterJdbcTemplate) {
        this.uiNamedParameterJdbcTemplate = uiNamedParameterJdbcTemplate;
    }

    @Override
    public List<Long> getCampaignScheduledAfter(LocalTime lastExecutionTime) {
        final String sql = "SELECT C.ID, C.SCHEDULE_TIME " +
            "FROM CAMPAIGN C WHERE C.SCHEDULE_TIME is not null";

        final List<Pair<Long, LocalTime>> allCampaigns = uiNamedParameterJdbcTemplate.query(sql
            , ImmutableMap.of()
            , campaignScheduledRowMapper);

        return allCampaigns.stream()
            .filter(p -> p.getRight() != null)
            .filter(p -> p.getRight().isAfter(lastExecutionTime) && p.getRight().isBefore(LocalTime.now()))
            .map(Pair::getLeft)
            .collect(Collectors.toList());
    }

    private static class CampaignScheduledRowMapper implements RowMapper<Pair<Long, LocalTime>> {
        @Override
        public Pair<Long, LocalTime> mapRow(ResultSet rs, int rowNum) throws SQLException {
            final Long id = rs.getLong("ID");
            final String scheduledTime = rs.getString("SCHEDULE_TIME");
            return Pair.of(id, LocalTime.parse(scheduledTime));
        }
    }
}
