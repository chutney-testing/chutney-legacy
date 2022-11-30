package com.chutneytesting.action.sql;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.chutneytesting.action.TestActionsConfiguration;
import com.chutneytesting.action.TestTarget;
import com.chutneytesting.action.spi.Action;
import com.chutneytesting.action.spi.ActionExecutionResult;
import com.chutneytesting.action.spi.injectable.ActionsConfiguration;
import com.chutneytesting.action.spi.injectable.Logger;
import com.chutneytesting.action.spi.injectable.Target;
import com.chutneytesting.action.sql.core.Records;
import com.chutneytesting.action.sql.core.Row;
import com.chutneytesting.action.sql.core.Rows;
import com.google.common.collect.Lists;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;
import org.mockito.Mockito;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;

public class SqlActionTest {

    private static final String DB_NAME = "test_" + SqlActionTest.class;

    private final Target sqlTarget = TestTarget.TestTargetBuilder.builder()
        .withTargetId("sql")
        .withUrl("jdbc:h2:mem")
        .withProperty("jdbcUrl", "jdbc:h2:mem:" + DB_NAME)
        .withProperty("user", "sa")
        .build();

    private final Logger logger = Mockito.mock(Logger.class);

    @BeforeEach
    public void setUp() {
        new EmbeddedDatabaseBuilder()
            .setName(DB_NAME)
            .setType(EmbeddedDatabaseType.H2)
            .setScriptEncoding("UTF-8")
            .ignoreFailedDrops(true)
            .addScripts("db/sql/create_db.sql", "db/sql/insert_users.sql")
            .build();
    }

    @Test
    public void should_output_only_on_result_when_single_statement() {
        // Given
        ActionsConfiguration configuration = new TestActionsConfiguration();
        Action action = new SqlAction(sqlTarget, logger, configuration, Collections.singletonList("select * from users"), 2);

        // When
        ActionExecutionResult result = action.execute();

        // Then
        assertThat(result.status).isEqualTo(ActionExecutionResult.Status.Success);

        List<Records> recordResult = (List<Records>) result.outputs.get("recordResult");
        // assertThat(recordResult).as("Context should not contain multi-statements output").isNull(); // TODO - check after user migration

        Rows rows = (Rows) result.outputs.get("rows");
        Row firstRow = (Row) result.outputs.get("firstRow");

        assertThat(rows).isNotNull();
        assertThat(firstRow).isNotNull();

        verify(logger).info(eq(
            "| ID | NAME    | EMAIL           |\n" +
            "----------------------------------\n" +
            "| 1  | laitue  | laitue@fake.com |\n" +
            "| 2  | carotte | kakarot@fake.db |\n"));
    }

    @Test
    public void should_output_only_many_results_when_multi_statements() {
        // Given
        ActionsConfiguration configuration = new TestActionsConfiguration();
        Action action = new SqlAction(sqlTarget, logger, configuration, Lists.newArrayList("select * from users where id = 1", "select * from users where id = 2") , 2);

        // When
        ActionExecutionResult result = action.execute();

        // Then
        assertThat(result.status).isEqualTo(ActionExecutionResult.Status.Success);

        Rows rows = (Rows) result.outputs.get("rows");
        Row firstRow = (Row) result.outputs.get("firstRow");
        assertThat(rows).as("Context should only contain multi-statements output").isNull();
        assertThat(firstRow).as("Context should only contain multi-statements output").isNull();

        List<Records> recordResult = (List<Records>) result.outputs.get("recordResult");
        assertThat(recordResult).hasSize(2);

        verify(logger, times(2)).info(anyString());

        InOrder inOrder = Mockito.inOrder(logger);
        inOrder.verify(logger).info(eq(
            "| ID | NAME   | EMAIL           |\n" +
            "---------------------------------\n" +
            "| 1  | laitue | laitue@fake.com |\n")
        );
        inOrder.verify(logger).info(
            "| ID | NAME    | EMAIL           |\n" +
            "----------------------------------\n" +
            "| 2  | carotte | kakarot@fake.db |\n"
        );
    }
}
