package com.chutneytesting.admin.infra;


import static com.chutneytesting.server.core.domain.admin.SqlResult.Row;
import static com.chutneytesting.server.core.domain.admin.SqlResult.data;
import static com.chutneytesting.server.core.domain.admin.SqlResult.error;
import static com.chutneytesting.server.core.domain.admin.SqlResult.updatedRows;

import com.chutneytesting.server.core.domain.admin.DatabaseAdminService;
import com.chutneytesting.server.core.domain.admin.SqlResult;
import com.chutneytesting.server.core.domain.admin.SqlResult.Table;
import com.chutneytesting.server.core.domain.tools.ImmutablePaginatedDto;
import com.chutneytesting.server.core.domain.tools.PaginatedDto;
import com.chutneytesting.server.core.domain.tools.PaginationRequestWrapperDto;
import com.chutneytesting.server.core.domain.tools.SqlUtils;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component("jdbcAdminService")
class DatabaseAdminServiceImpl implements DatabaseAdminService {

    private static final Logger LOGGER = LoggerFactory.getLogger(DatabaseAdminServiceImpl.class);

    private final DataSource dataSource;

    DatabaseAdminServiceImpl(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public SqlResult execute(String query) {
        String cleanQuery = cleanQuery(query);
        boolean isSelect = isQuerySelect(query);

        String finalQuery = isSelect ? limit(cleanQuery, false) : cleanQuery;
        LOGGER.debug("Executing query {}", finalQuery);
        return executeQuery(finalQuery);
    }

    @Override
    public PaginatedDto<SqlResult> paginate(PaginationRequestWrapperDto<String> paginationRequestWrapperDto) {
        SqlResult result;
        String query = paginationRequestWrapperDto.wrappedRequest().orElse("select 1");
        String cleanQuery = cleanQuery(query);
        boolean isQuerySelect = isQuerySelect(cleanQuery);

        long totalCount = 0;
        if (isQuerySelect) {
            String countQuery = SqlUtils.count(cleanQuery);
            try (Connection connection = dataSource.getConnection();
                 Statement statement = connection.createStatement()) {
                boolean dataSelected = statement.execute(countQuery);
                if (dataSelected) {
                    try (ResultSet rs = statement.getResultSet()) {
                        rs.next();
                        totalCount = rs.getLong(1);
                    }
                }
            } catch (SQLException e) {
                result = error("Unable to execute statement[" + countQuery + "]: " + e.getMessage());
                return ImmutablePaginatedDto.<SqlResult>builder()
                    .totalCount(totalCount)
                    .addData(result)
                    .build();
            }
        }

        if (isQuerySelect) {
            result = executeQuery(
                limit(cleanQuery, true),
                paginationRequestWrapperDto.elementPerPage(),
                (paginationRequestWrapperDto.pageNumber() -1) * paginationRequestWrapperDto.elementPerPage()

            );
        } else {
            result = executeQuery(cleanQuery);
        }

        return ImmutablePaginatedDto.<SqlResult>builder()
            .totalCount(totalCount)
            .addData(result)
            .build();
    }

    private SqlResult executeQuery(String query, Integer...args) {
        SqlResult result;
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {
            if (args != null && args.length == 2) {
                statement.setInt(1, args[0]);
                statement.setInt(2, args[1]);
            }
            boolean dataSelected = statement.execute();
            if (dataSelected) {
                try (ResultSet rs = statement.getResultSet()) {
                    result = data(resultSetToTable(rs));
                }
            } else {
                result = updatedRows(statement.getUpdateCount());
            }
        } catch (SQLException e) {
            result = error("Unable to execute statement[" + query + "]: " + e.getMessage());
        }
        return result;
    }

    private String limit(String query, boolean paginate) {
        if (query.replaceAll("\\R", " ").matches(".*(?i:limit).*[^)]")) {
            return query;
        }

        if (paginate) {
            return query + " LIMIT ? OFFSET ?";
        } else {
            return query + " LIMIT 20";
        }
    }

    private boolean isQuerySelect(String query) {
        String queryUpper = query.toUpperCase();
        return queryUpper.startsWith("SELECT");
    }

    private String cleanQuery(String query) {
        query = query.trim();
        if (query.endsWith(";")) {
            query = query.substring(0, query.length() - 1);
        }
        return query;
    }

    private Table resultSetToTable(ResultSet rs) throws SQLException {
        ResultSetMetaData metaData = rs.getMetaData();
        List<String> columNames = getColumnNames(metaData);

        List<Row> rows = new ArrayList<>();
        while (rs.next()) {
            List<String> values = new ArrayList<>();
            for (String columName : columNames) {
                values.add(rs.getString(columName));
            }
            rows.add(new Row(values));
        }

        return new Table(columNames, rows);
    }

    private List<String> getColumnNames(ResultSetMetaData metaData) throws SQLException {
        int columnCount = metaData.getColumnCount();
        List<String> columNames = new ArrayList<>();
        for (int i = 1; i <= columnCount; i++) {
            columNames.add(metaData.getColumnName(i));
        }
        return columNames;
    }
}
