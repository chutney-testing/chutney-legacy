package com.chutneytesting.task.sql.core;

import static org.assertj.core.api.Assertions.assertThat;

import com.chutneytesting.task.TestTarget;
import com.chutneytesting.task.spi.injectable.Target;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabase;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;

public class SqlClientTest {

    private static String DB_NAME = "test";
    private Target sqlTarget = TestTarget.TestTargetBuilder.builder()
        .withTargetId("sql")
        .withUrl("jdbc:h2:mem:" + DB_NAME)
        .withSecurity("sa", "")
        .build();

    @SuppressWarnings("unused")
    private EmbeddedDatabase db;

    @BeforeEach
    public void setUp() {

        db = new EmbeddedDatabaseBuilder()
            .setName(DB_NAME)
            .setType(EmbeddedDatabaseType.H2)
            .setScriptEncoding("UTF-8")
            .ignoreFailedDrops(true)
            .addScripts("db/sql/create_db.sql", "db/sql/insert_users.sql")
            .build();
    }

    @Test
    public void should_return_headers_and_rows_on_select_query() throws SQLException {
        List<Object> firstTuple = Arrays.asList(1, "laitue", "laitue@fake.com");
        List<Object> secondTuple = Arrays.asList(2, "carotte", "kakarot@fake.db");
        List<Object> thirdTuple = Arrays.asList(3, "tomate", "null");

        SqlClient sqlClient = new DefaultSqlClientFactory().create(sqlTarget);
        Records records = sqlClient.execute("select * from users");

        assertThat(records.headers).containsOnly("ID", "NAME", "EMAIL");
        assertThat(records.rows).containsExactly(firstTuple, secondTuple, thirdTuple);
    }

    @Test
    public void should_return_affected_rows_on_update_queries() throws SQLException {
        SqlClient sqlClient = new DefaultSqlClientFactory().create(sqlTarget);
        Records records = sqlClient.execute("UPDATE USERS SET NAME = 'toto' WHERE ID = 1");

        assertThat(records.affectedRows).isEqualTo(1);
    }

    @Test
    public void should_return_count_on_count_queries() throws SQLException {

        List<Object> expectedTuple = Collections.singletonList(3L);

        SqlClient sqlClient = new DefaultSqlClientFactory().create(sqlTarget);
        Records records = sqlClient.execute("SELECT COUNT(*) as total FROM USERS");

        assertThat(records.rows).containsExactly(expectedTuple);
    }

    @Test
    public void should_print_records_as_list_of_mapped_values() throws SQLException {
        List<Map<String, Object>> listOfMaps = new ArrayList<>(2);
        Map<String, Object> firstTuple = new LinkedHashMap<>();
        firstTuple.put("ID", 1);
        firstTuple.put("NAME", "laitue");
        firstTuple.put("EMAIL", "laitue@fake.com");

        Map<String, Object> secondTuple = new LinkedHashMap<>();
        secondTuple.put("ID", 2);
        secondTuple.put("NAME", "carotte");
        secondTuple.put("EMAIL", "kakarot@fake.db");

        Map<String, Object> thirdTuple = new LinkedHashMap<>();
        thirdTuple.put("ID", 3);
        thirdTuple.put("NAME", "tomate");
        thirdTuple.put("EMAIL", "null");

        listOfMaps.add(firstTuple);
        listOfMaps.add(secondTuple);
        listOfMaps.add(thirdTuple);

        SqlClient sqlClient = new DefaultSqlClientFactory().create(sqlTarget);
        Records records = sqlClient.execute("select * from users");

        assertThat(records.toListOfMaps()).containsExactlyElementsOf(listOfMaps);
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
}
