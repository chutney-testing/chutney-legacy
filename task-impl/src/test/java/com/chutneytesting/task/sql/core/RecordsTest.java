package com.chutneytesting.task.sql.core;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Map;
import org.apache.groovy.util.Maps;
import org.junit.jupiter.api.Test;

public class RecordsTest {


    @Test
    void getHeaders() {

        Column c0 = new Column("Test", 0);
        Column c1 = new Column("Letter", 1);
        Column c2 = new Column("Letter", 2);
        Column c3 = new Column("Number", 3);
        Column c4 = new Column("Number", 4);

        Records actual = new Records(-1, List.of(c0, c1, c2, c3, c4), emptyList());

        assertThat(actual.headers).isEqualTo(List.of("Test", "Letter", "Letter", "Number", "Number"));
    }

    @Test
    void getRows() {
        Column fake = new Column("fake", 0);

        List<Row> records = List.of(
            new Row(List.of(new Cell(fake, "A"), new Cell(fake, "B"))),
            new Row(List.of(new Cell(fake, "AA"), new Cell(fake, "BB")))
        );

        Records actual = new Records(-1, emptyList(), records);

        assertThat(actual.rows.get(0)).isEqualTo(List.of("A", "B"));
        assertThat(actual.rows.get(1)).isEqualTo(List.of("AA", "BB"));

    }

    @Test
    void toListOfMaps_should_return_all_rows() {

        Column c0 = new Column("First Name", 0);
        Column c1 = new Column("Name", 1);
        Column c2 = new Column("Address", 2);

        List<Row> records = List.of(
            new Row(List.of(new Cell(c0, "Henri"), new Cell(c1, "Martin"), new Cell(c2, "Paris"))),
            new Row(List.of(new Cell(c0, "Jean"), new Cell(c1, "Dupont"), new Cell(c2, "Bordeaux"))),
            new Row(List.of(new Cell(c0, "Charles"), new Cell(c1, "Magne"), new Cell(c2, "Chartres")))
        );

        Records sut = new Records(-1, List.of(c0, c1, c2), records);

        List<Map<String, Object>> actual = sut.toListOfMaps();

        assertThat(actual).hasSize(3);
        assertThat(actual.get(1).get("Name")).isEqualTo("Dupont");
        assertThat(actual.get(2).get("Address")).isEqualTo("Chartres");

    }

    @Test
    void toListOfMaps_should_return_limited_number_of_rows() {

        Column c0 = new Column("First Name", 0);
        Column c1 = new Column("Name", 1);
        Column c2 = new Column("Address", 2);

        List<Row> records = List.of(
            new Row(List.of(new Cell(c0, "Henri"), new Cell(c1, "Martin"), new Cell(c2, "Paris"))),
            new Row(List.of(new Cell(c0, "Jean"), new Cell(c1, "Dupont"), new Cell(c2, "Bordeaux"))),
            new Row(List.of(new Cell(c0, "Charles"), new Cell(c1, "Magne"), new Cell(c2, "Chartres")))
        );

        Records sut = new Records(-1, List.of(c0, c1, c2), records);

        List<Map<String, Object>> actual = sut.toListOfMaps(2);

        assertThat(actual).hasSize(2);

    }

    @Test
    void toListOfMaps_should_not_out_bound() {

        Column c0 = new Column("First Name", 0);
        Column c1 = new Column("Name", 1);
        Column c2 = new Column("Address", 2);

        List<Row> records = List.of(
            new Row(List.of(new Cell(c0, "Henri"), new Cell(c1, "Martin"), new Cell(c2, "Paris")))
        );

        Records sut = new Records(-1, List.of(c0, c1, c2), records);

        List<Map<String, Object>> actual = sut.toListOfMaps(2);

        assertThat(actual).hasSize(1);

    }

    @Test
    void toMatrix() {

        Column c0 = new Column("First Name", 0);
        Column c1 = new Column("Name", 1);
        Column c2 = new Column("Address", 2);

        List<Row> records = List.of(
            new Row(List.of(new Cell(c0, "Henri"), new Cell(c1, "Martin"), new Cell(c2, "Paris"))),
            new Row(List.of(new Cell(c0, "Jean"), new Cell(c1, "Dupont"), new Cell(c2, "Bordeaux"))),
            new Row(List.of(new Cell(c0, "Charles"), new Cell(c1, "Magne"), new Cell(c2, "Chartres")))
        );

        Records sut = new Records(-1, List.of(c0, c1, c2), records);

        Object[][] actual = sut.toMatrix();

        assertThat(actual[1][1]).isEqualTo("Dupont");
        assertThat(actual[2][2]).isEqualTo("Chartres");

    }

    @Test
    void should_print_nothing_without_headers() {
        Records records = new Records(-1, emptyList(), emptyList());
        String actual = records.tableHeaders(records.maximumColumnLength(0));
        assertThat(actual).isEmpty();
    }


    @Test
    void column_length_should_equal_header_or_value_max_length() {
        Column c0 = new Column("lengthOf11", 0);
        Column c1 = new Column("Header is longer", 1);
        Column c2 = new Column("7", 2);

        List<Column> headers = asList(c0, c1, c2);
        List<Row> rows = singletonList(new Row(List.of(new Cell(c0, "12345678910"), new Cell(c1,"16"), new Cell(c2,"1234567"))));

        Records sut = new Records(-1, headers, rows);

        Map<Column, Integer> actual = sut.maximumColumnLength(1);

        assertThat(actual.get(c0)).isEqualTo(11);
        assertThat(actual.get(c1)).isEqualTo(16);
        assertThat(actual.get(c2)).isEqualTo(7);
    }

    @Test
    void column_length_should_be_calculated_from_limited_number_of_rows() {
        Column c0 = new Column("lengthOf11", 0);
        Column c1 = new Column("Header is longer", 1);
        Column c2 = new Column("same", 2);
        Column c3 = new Column("same", 3);

        List<Column> headers = asList(c0, c1, c2, c3);
        List<Row> rows = asList(
            new Row(List.of(new Cell(c0, "12345678910"), new Cell(c1,"16"), new Cell(c2,"123"), new Cell(c3, "ABC"))),
            new Row(List.of(new Cell(c0, "veryveryvery long value"), new Cell(c1,"42"), new Cell(c2,"123"), new Cell(c3, "ABC")))
        );

        Records sut = new Records(-1, headers, rows);

        Map<Column, Integer> actual = sut.maximumColumnLength(1);

        assertThat(actual.get(c0)).isEqualTo(11);
        assertThat(actual.get(c1)).isEqualTo(16);
        assertThat(actual.get(c2)).isEqualTo(4);
    }

    @Test
    void should_print_formatted_headers() {
        Column c0 = new Column("lengthOf11", 0);
        Column c1 = new Column("2", 1);
        Column c2 = new Column("7", 2);

        List<Column> headers = asList(c0, c1, c2);
        List<Row> rows = singletonList(new Row(List.of(new Cell(c0, "12345678910"), new Cell(c1,"12"), new Cell(c2,"1234567"))));
        Records sut = new Records(-1, headers, rows);

        String actual = sut.tableHeaders(sut.maximumColumnLength(1));

        assertThat(actual).isEqualTo(
            "| lengthOf11  | 2  | 7       |\n" +
            "------------------------------\n"
        );
    }

    @Test
    void should_print_formatted_rows() {
        Column c0 = new Column("lengthOf11", 0);
        Column c1 = new Column("2", 1);
        Column c2 = new Column("7", 2);

        List<Column> headers = asList(c0, c1, c2);

        List<Row> rows = singletonList(new Row(List.of(new Cell(c0, "12345678910"), new Cell(c1,"42"), new Cell(c2,"1234567"))));
        Map<String, Integer> maxLength = Maps.of(
            "lengthOf11", 11,
            "Header is longer", 16,
            "7", 7
        );
        Map<String, Object> row = Maps.of(
            "lengthOf11", "12345678910",
            "Header is longer", "42",
            "7", "1234567"
        );

        Records sut = new Records(-1, headers, rows);

        String actual = sut.rowAsString(row, maxLength);

        assertThat(actual).isEqualTo("| 12345678910 | 42               | 1234567 |\n");
    }

    @Test
    void should_accept_duplicated_headers() {

        Column c0 = new Column("X", 0);
        Column c1 = new Column("X", 1);
        Column c2 = new Column("X", 2);

        Records records = new Records(-1, List.of(c0, c1, c2), List.of(
            new Row(List.of(new Cell(c0, "A"), new Cell(c1,"B"), new Cell(c2,"C"))),
            new Row(List.of(new Cell(c0, "D"), new Cell(c1,"E"), new Cell(c2,"F")))
        ));

        String actual = records.printable(2);

        assertThat(actual).isEqualTo(
            "| X | X | X |\n" +
            "-------------\n" +
            "| A | B | C |\n" +
            "| D | E | F |\n"
        );
    }
}
