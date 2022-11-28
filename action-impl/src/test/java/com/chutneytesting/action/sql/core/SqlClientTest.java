package com.chutneytesting.action.sql.core;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.chutneytesting.action.TestTarget;
import com.chutneytesting.action.spi.injectable.Target;
import com.chutneytesting.tools.ChutneyMemoryInfo;
import com.chutneytesting.tools.NotEnoughMemoryException;
import java.math.BigDecimal;
import java.sql.Date;
import java.sql.SQLException;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;

public class SqlClientTest {

    private static final String DB_NAME = "test_" + SqlClientTest.class;
    private final Target sqlTarget = TestTarget.TestTargetBuilder.builder()
        .withTargetId("sql")
        .withUrl("jdbc:h2:mem")
        .withProperty("jdbcUrl", "jdbc:h2:mem:" + DB_NAME)
        .withProperty("user", "sa")
        .build();

    @BeforeEach
    public void setUp() {
        new EmbeddedDatabaseBuilder()
            .setName(DB_NAME)
            .setType(EmbeddedDatabaseType.H2)
            .setScriptEncoding("UTF-8")
            .ignoreFailedDrops(true)
            .addScripts("db/sql/create_db.sql", "db/sql/insert_users.sql", "db/sql/insert_allsqltypes.sql")
            .build();
    }

    @Test
    public void should_return_headers_and_rows_on_select_query() throws SQLException {
        Column c0 = new Column("ID", 0);
        Column c1 = new Column("NAME", 1);
        Column c2 = new Column("EMAIL", 2);

        Row firstTuple = new Row(List.of(new Cell(c0, 1), new Cell(c1, "laitue"), new Cell(c2, "laitue@fake.com")));
        Row secondTuple = new Row(List.of(new Cell(c0, 2), new Cell(c1, "carotte"), new Cell(c2, "kakarot@fake.db")));
        Row thirdTuple = new Row(List.of(new Cell(c0, 3), new Cell(c1, "tomate"), new Cell(c2, "null")));

        SqlClient sqlClient = new DefaultSqlClientFactory().create(sqlTarget);
        Records actual = sqlClient.execute("select * from users");

        assertThat(actual.getHeaders()).containsOnly("ID", "NAME", "EMAIL");
        assertThat(actual.records).containsExactly(firstTuple, secondTuple, thirdTuple);
    }

    @Test
    public void should_return_affected_rows_on_update_queries() throws SQLException {
        SqlClient sqlClient = new DefaultSqlClientFactory().create(sqlTarget);
        Records records = sqlClient.execute("UPDATE USERS SET NAME = 'toto' WHERE ID = 1");

        assertThat(records.affectedRows).isEqualTo(1);
    }

    @Test
    public void should_return_count_on_count_queries() throws SQLException {
        Column c0 = new Column("TOTAL", 0);
        Row expectedTuple = new Row(Collections.singletonList(new Cell(c0, 3L)));

        SqlClient sqlClient = new DefaultSqlClientFactory().create(sqlTarget);
        Records actual = sqlClient.execute("SELECT COUNT(*) as total FROM USERS");

        assertThat(actual.records).containsExactly(expectedTuple);
    }

    @Test
    public void should_print_records_as_matrix() throws SQLException {
        Object[][] expectedMatrix = new Object[3][3];
        expectedMatrix[0][0] = 1;
        expectedMatrix[0][1] = "laitue";
        expectedMatrix[0][2] = "laitue@fake.com";

        expectedMatrix[1][0] = 2;
        expectedMatrix[1][1] = "carotte";
        expectedMatrix[1][2] = "kakarot@fake.db";

        expectedMatrix[2][0] = 3;
        expectedMatrix[2][1] = "tomate";
        expectedMatrix[2][2] = "null";

        SqlClient sqlClient = new DefaultSqlClientFactory().create(sqlTarget);
        Records records = sqlClient.execute("select * from users");

        assertThat(records.toMatrix()).isEqualTo(expectedMatrix);
    }

    @Test
    public void should_retrieve_columns_as_string_but_for_date_and_numeric_sql_datatypes() throws SQLException {
        SqlClient sqlClient = new DefaultSqlClientFactory().create(sqlTarget);
        Records actual = sqlClient.execute("select * from allsqltypes");

        Row onlyRecord = actual.rows().get(0);
        assertThat(onlyRecord.get("COL_BOOLEAN")).isInstanceOf(Boolean.class);
        assertThat(onlyRecord.get("COL_TINYINT")).isInstanceOf(Integer.class);
        assertThat(onlyRecord.get("COL_SMALLINT")).isInstanceOf(Integer.class);
        assertThat(onlyRecord.get("COL_MEDIUMINT")).isInstanceOf(Integer.class);
        assertThat(onlyRecord.get("COL_INTEGER")).isInstanceOf(Integer.class);
        assertThat(onlyRecord.get("COL_BIGINT")).isInstanceOf(Long.class);
        assertThat(onlyRecord.get("COL_FLOAT")).isInstanceOf(Float.class);
        assertThat(onlyRecord.get("COL_DOUBLE")).isInstanceOf(Double.class);
        assertThat(onlyRecord.get("COL_DECIMAL")).isInstanceOf(BigDecimal.class);
        assertThat(onlyRecord.get("COL_DECIMAL")).isInstanceOf(BigDecimal.class);
        assertThat(onlyRecord.get("COL_DATE")).isInstanceOf(Date.class);
        assertThat(onlyRecord.get("COL_TIME")).isInstanceOf(Time.class);
        assertThat(onlyRecord.get("COL_TIMESTAMP")).isInstanceOf(Timestamp.class);
        assertThat(onlyRecord.get("COL_CHAR")).isInstanceOf(String.class);
        assertThat(onlyRecord.get("COL_VARCHAR")).isInstanceOf(String.class);
        // INTERVAL SQL types : cf. SqlClient.StatementConverter#isJDBCDateType(Class)
        assertThat(onlyRecord.get("COL_INTERVAL_YEAR")).isInstanceOf(String.class);
        assertThat(onlyRecord.get("COL_INTERVAL_SECOND")).isInstanceOf(String.class);
    }

    @Test
    public void should_prevent_out_of_memory() {
        try (MockedStatic<ChutneyMemoryInfo> chutneyMemoryInfoMockedStatic = Mockito.mockStatic(ChutneyMemoryInfo.class);) {
            chutneyMemoryInfoMockedStatic.when(ChutneyMemoryInfo::hasEnoughAvailableMemory).thenReturn(true, true, false);
            chutneyMemoryInfoMockedStatic.when(ChutneyMemoryInfo::usedMemory).thenReturn(42L * 1024 * 1024);
            chutneyMemoryInfoMockedStatic.when(ChutneyMemoryInfo::maxMemory).thenReturn(1337L * 1024 * 1024);

            SqlClient sqlClient = new DefaultSqlClientFactory().create(sqlTarget);

            Exception exception = assertThrows(NotEnoughMemoryException.class, () -> {
                sqlClient.execute("select * from users");
            });
            assertThat(exception.getMessage()).isEqualTo("Running step was stopped to prevent application crash.42MB memory used of 1337MB max.\n" +
                "Current step may not be the cause.\n" +
                "Query fetched 2 rows");
        }
    }
}
