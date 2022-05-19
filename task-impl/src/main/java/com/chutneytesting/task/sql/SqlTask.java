package com.chutneytesting.task.sql;

import static com.chutneytesting.task.spi.validation.TaskValidatorsUtils.notEmptyListValidation;
import static com.chutneytesting.task.spi.validation.TaskValidatorsUtils.targetValidation;
import static com.chutneytesting.task.spi.validation.Validator.getErrorsFrom;
import static com.chutneytesting.task.spi.validation.Validator.of;
import static java.util.Optional.ofNullable;

import com.chutneytesting.task.spi.Task;
import com.chutneytesting.task.spi.TaskExecutionResult;
import com.chutneytesting.task.spi.injectable.Input;
import com.chutneytesting.task.spi.injectable.Logger;
import com.chutneytesting.task.spi.injectable.Target;
import com.chutneytesting.task.spi.injectable.TasksConfiguration;
import com.chutneytesting.task.spi.validation.Validator;
import com.chutneytesting.task.sql.core.DefaultSqlClientFactory;
import com.chutneytesting.task.sql.core.Records;
import com.chutneytesting.task.sql.core.SqlClient;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import org.apache.commons.lang3.StringUtils;

public class SqlTask implements Task {

    public static final String CONFIGURABLE_NB_LOGGED_ROW = "chutney.tasks.sql.nbLoggedRow";
    private static final Integer DEFAULT_NB_LOGGED_ROW = 30;

    private final Target target;
    private final Logger logger;
    private final List<String> statements;
    private final Integer nbLoggedRow;

    private final DefaultSqlClientFactory clientFactory = new DefaultSqlClientFactory();

    public SqlTask(Target target, Logger logger, TasksConfiguration configuration, @Input("statements") List<String> statements, @Input("nbLoggedRow") Integer nbLoggedRow) {
        this.target = target;
        this.logger = logger;
        this.statements = statements;
        this.nbLoggedRow = ofNullable(nbLoggedRow)
            .orElse(configuration.getInteger(CONFIGURABLE_NB_LOGGED_ROW, DEFAULT_NB_LOGGED_ROW));
    }

    @Override
    public List<String> validateInputs() {
        Validator<Target> targetPropertiesValidation = of(target)
            .validate(t -> target.property("jdbcUrl").orElse(""), StringUtils::isNotBlank, "Missing Target property 'jdbcUrl'");
        return getErrorsFrom(
            targetPropertiesValidation,
            targetValidation(target),
            notEmptyListValidation(statements, "statements")
        );
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
                    logger.info(result.printable(nbLoggedRow));
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
