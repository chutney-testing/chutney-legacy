package com.chutneytesting.admin.infra.storage;

import com.orientechnologies.orient.core.db.ODatabasePool;
import com.orientechnologies.orient.core.db.ODatabaseSession;
import com.orientechnologies.orient.core.exception.OCoreException;
import com.orientechnologies.orient.core.sql.executor.OResult;
import com.orientechnologies.orient.core.sql.executor.OResultSet;
import com.chutneytesting.admin.domain.DatabaseAdminService;
import com.chutneytesting.admin.domain.SqlResult;
import com.chutneytesting.admin.domain.SqlResult.Row;
import com.chutneytesting.admin.domain.SqlResult.Table;
import com.chutneytesting.design.infra.storage.scenario.compose.orient.OrientComponentDB;
import com.chutneytesting.design.infra.storage.scenario.compose.orient.OrientUtils;
import com.chutneytesting.tools.ImmutablePaginatedDto;
import com.chutneytesting.tools.PaginatedDto;
import com.chutneytesting.tools.PaginationRequestWrapperDto;
import com.chutneytesting.tools.SqlUtils;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

@Component("orientAdminService")
class OrientAdminService implements DatabaseAdminService {

    private final ODatabasePool componentDBPool;

    OrientAdminService(OrientComponentDB orientComponentDB) {
        this.componentDBPool = orientComponentDB.dbPool();
    }

    @Override
    public SqlResult execute(String query) {
        String cleanQuery = cleanQuery(query);
        boolean isSelect = isQuerySelect(query);

        String finalQuery = isSelect ? limit(cleanQuery, false) : cleanQuery;
        return executeQuery(finalQuery, isSelect);
    }

    @Override
    public PaginatedDto<SqlResult> paginate(PaginationRequestWrapperDto<String> paginationRequestWrapperDto) {
        SqlResult result;

        String query = paginationRequestWrapperDto.wrappedRequest().orElse("select expand(classes) from metadata:schema");
        String cleanQuery = cleanQuery(query);
        boolean isSelect = isQuerySelect(query);

        long totalCount = 0;
        if (isSelect) {
            String countQuery = SqlUtils.count(cleanQuery);
            try (ODatabaseSession dbSession = componentDBPool.acquire();
                 OResultSet rs = dbSession.command(countQuery)) {
                totalCount = OrientUtils.resultSetToCount(rs);
            } catch (OCoreException e) {
                result = SqlResult.error("Unable to execute statement[" + countQuery + "]: " + e.getMessage());
                return ImmutablePaginatedDto.<SqlResult>builder()
                    .totalCount(totalCount)
                    .addData(result)
                    .build();
            }
        }

        String finalQuery = isSelect ? limit(cleanQuery, true) : cleanQuery;
        result = executeQuery(
            finalQuery, isSelect, (paginationRequestWrapperDto.pageNumber() - 1) * paginationRequestWrapperDto.elementPerPage(), paginationRequestWrapperDto.elementPerPage());

        return ImmutablePaginatedDto.<SqlResult>builder()
            .totalCount(totalCount)
            .addData(result)
            .build();
    }

    @SuppressWarnings("ConfusingArgumentToVarargsMethod")
    private SqlResult executeQuery(String query, boolean isSelect, Integer...args) {
        SqlResult result;
        try (ODatabaseSession dbSession = componentDBPool.acquire()) {
            if (isSelect) {
                try (OResultSet rs = dbSession.command(query, args)) {
                    result = SqlResult.data(resultSetToTable(rs));
                }
            } else {
                try (OResultSet rs = dbSession.command(query)) {
                    result = resultSetToSqlResult(rs);
                }
            }
        } catch (OCoreException e) {
            result = SqlResult.error("Unable to execute statement[" + query + "]: " + e.getMessage());
        }

        return result;
    }

    private String limit(String query, boolean paginate) {
        if (query.replaceAll("\\R", " ").matches(".*(?i:limit).*[^)]")) {
            return query;
        }

        if (paginate) {
            return OrientUtils.addPaginationParameters(query);
        } else {
            return query + " LIMIT 20";
        }
    }

    private boolean isQuerySelect(String query) {
        return query.trim().toUpperCase().startsWith("SELECT");
    }

    private String cleanQuery(String query) {
        query = query.trim();
        if (query.endsWith(";")) {
            query = query.substring(0, query.length() - 1);
        }
        return query;
    }

    private Table resultSetToTable(OResultSet resultSet) {
        List<String> properties = new ArrayList<>();
        List<Row> rows = new ArrayList<>();
        while (resultSet.hasNext()) {
            OResult result = resultSet.next();
            if (properties.size() == 0) {
                properties.addAll(result.getPropertyNames());
            }
            List<String> values = new ArrayList<>();
            for (String propertyName : properties) {
                values.add(Optional.ofNullable(result.getProperty(propertyName)).orElse("").toString());
            }
            rows.add(new Row(values));
        }

        return new Table(properties, rows);
    }

    private SqlResult resultSetToSqlResult(OResultSet resultSet) {
        Optional<Integer> totalCount = Optional.empty();
        Optional<Table> table = Optional.empty();

        List<String> properties = new ArrayList<>();
        List<Row> rows = new ArrayList<>();
        if (resultSet.hasNext()) {
            OResult result = resultSet.next();

            if (result.hasProperty("count")) {
                totalCount = Optional.of(((Long) result.getProperty("count")).intValue());
                properties.addAll(
                    result.getPropertyNames().stream()
                        .filter(s -> !s.equalsIgnoreCase("count"))
                        .collect(Collectors.toList())
                );
            } else {
                properties.addAll(result.getPropertyNames());
            }

            List<String> values = new ArrayList<>();
            for (String propertyName : properties) {
                values.add(Optional.ofNullable(result.getProperty(propertyName)).orElse("").toString());
            }
            rows.add(new Row(values));

            if (! values.isEmpty()) {
                table = Optional.of(new Table(properties, rows));
            }
        }

        return new SqlResult(totalCount, Optional.empty(), table);
    }
}
