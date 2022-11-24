package com.chutneytesting.action.sql;

import static com.chutneytesting.action.spi.validation.ActionValidatorsUtils.notEmptyListValidation;
import static com.chutneytesting.action.spi.validation.ActionValidatorsUtils.targetValidation;
import static com.chutneytesting.action.spi.validation.Validator.getErrorsFrom;
import static com.chutneytesting.action.spi.validation.Validator.of;
import static java.util.Optional.ofNullable;

import com.chutneytesting.action.spi.Action;
import com.chutneytesting.action.spi.ActionExecutionResult;
import com.chutneytesting.action.spi.injectable.Input;
import com.chutneytesting.action.spi.injectable.Logger;
import com.chutneytesting.action.spi.injectable.Target;
import com.chutneytesting.action.spi.injectable.ActionsConfiguration;
import com.chutneytesting.action.spi.validation.Validator;
import com.chutneytesting.action.sql.core.DefaultSqlClientFactory;
import com.chutneytesting.action.sql.core.Records;
import com.chutneytesting.action.sql.core.SqlClient;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import org.apache.commons.lang3.StringUtils;

public class SqlAction implements Action {

    public static final String CONFIGURABLE_NB_LOGGED_ROW = "chutney.actions.sql.nbLoggedRow";
    private static final Integer DEFAULT_NB_LOGGED_ROW = 30;

    private final Target target;
    private final Logger logger;
    private final List<String> statements;
    private final Integer nbLoggedRow;

    private final DefaultSqlClientFactory clientFactory = new DefaultSqlClientFactory();

    public SqlAction(Target target, Logger logger, ActionsConfiguration configuration, @Input("statements") List<String> statements, @Input("nbLoggedRow") Integer nbLoggedRow) {
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
    public ActionExecutionResult execute() {
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
            return failure.get() ? ActionExecutionResult.ko(outputs) : ActionExecutionResult.ok(outputs);
        } finally {
            if (sqlClient != null) {
                sqlClient.closeDatasource();
            }
        }
    }
}
