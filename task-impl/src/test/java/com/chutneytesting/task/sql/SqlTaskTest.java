package com.chutneytesting.task.sql;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

import com.chutneytesting.task.TestTarget;
import com.chutneytesting.task.TestTasksConfiguration;
import com.chutneytesting.task.spi.Task;
import com.chutneytesting.task.spi.TaskExecutionResult;
import com.chutneytesting.task.spi.injectable.Logger;
import com.chutneytesting.task.spi.injectable.Target;
import com.chutneytesting.task.spi.injectable.TasksConfiguration;
import com.chutneytesting.task.sql.core.Records;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;

public class SqlTaskTest {

    private static final String DB_NAME = "test_" + SqlTaskTest.class;

    private final Target sqlTarget = TestTarget.TestTargetBuilder.builder()
        .withTargetId("sql")
        .withUrl("jdbc:h2:mem:" + DB_NAME)
        .withSecurity("sa", "")
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
    public void testExecute() {
        Object[] firstTuple = {1, "laitue", "laitue@fake.com"};
        Object[] secondTuple = {2, "carotte", "kakarot@fake.db"};
        Object[] thirdTuple = {3, "tomate", "null"};

        TasksConfiguration configuration = new TestTasksConfiguration();

        Task task = new SqlTask(sqlTarget, logger, configuration, Collections.singletonList("select * from users"), 2);
        TaskExecutionResult result = task.execute();

        List<Records> recordResult = (List<Records>) result.outputs.get("recordResult");
        assertThat(result.status).isEqualTo(TaskExecutionResult.Status.Success);
        assertThat(Arrays.stream(recordResult.get(0).toMatrix()).toArray()).containsExactly(firstTuple, secondTuple, thirdTuple);
        verify(logger).info(eq("| ID | NAME    | EMAIL           |\n" +
                                     "----------------------------------\n" +
                                     "| 1  | laitue  | laitue@fake.com |\n" +
                                     "| 2  | carotte | kakarot@fake.db |\n"));
    }
}
