package com.chutneytesting.task.sql;

import com.chutneytesting.task.spi.Task;
import com.chutneytesting.task.spi.TaskExecutionResult;
import com.chutneytesting.task.spi.injectable.Input;
import com.chutneytesting.task.spi.injectable.Logger;
import com.chutneytesting.task.spi.injectable.Target;
import com.chutneytesting.task.sql.core.DefaultSqlClientFactory;
import com.chutneytesting.task.sql.core.Records;
import com.chutneytesting.task.sql.core.SqlClient;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

public class SqlTask implements Task {

    private final Target target;
    private final Logger logger;
    private final List<String> statements;

    private final DefaultSqlClientFactory clientFactory = new DefaultSqlClientFactory();
    private final int NB_LOGGED_ROWS = 10;

    public SqlTask(Target target, Logger logger, @Input("statements") List<String> statements) {
        this.target = target;
        this.logger = logger;
        this.statements = statements;
    }

    @Override
    public TaskExecutionResult execute() {
        SqlClient sqlClient = clientFactory.create(target);
        try {
            List<Records> records = new ArrayList<>();
            Map<String, List<Records>> outputs = new HashMap<>();
            AtomicBoolean failure = new AtomicBoolean(false);
            statements.forEach(statement -> {
                try {
                    Records result = sqlClient.execute(statement);
                    records.add(result);
                    logger.info(result.printable(NB_LOGGED_ROWS));
                } catch (SQLException e) {
                    logger.error(e.getMessage() + " for " + statement + "; Vendor error code: " + e.getErrorCode());
                    records.add(sqlClient.emptyRecords());
                    failure.set(true);
                } catch (Exception e) {
                    logger.error(e.getMessage());
                    records.add(sqlClient.emptyRecords());
                    failure.set(true);
                }
            });
            outputs.put("recordResult", records);
            return failure.get() ? TaskExecutionResult.ko(outputs) : TaskExecutionResult.ok(outputs);
        } finally {
            if (sqlClient != null) {
                sqlClient.closeDatasource();
            }
        }
    }
}
