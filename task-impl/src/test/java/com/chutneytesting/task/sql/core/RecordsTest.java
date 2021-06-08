package com.chutneytesting.task.sql.core;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Map;
import org.apache.groovy.util.Maps;
import org.junit.jupiter.api.Test;

class RecordsTest {

    @Test
    void toListOfMaps_should_return_all_rows() {

        Records records = new Records(-1, List.of("First Name", "Name", "Address"), List.of(
            List.of("Henri", "Martin", "Paris"),
            List.of("Jean", "Dupont", "Bordeaux"),
            List.of("Charles", "Magne", "Chartres")
        ));

        List<Map<String, Object>> actual = records.toListOfMaps();

        assertThat(actual).hasSize(3);
        assertThat(actual.get(1).get("Name")).isEqualTo("Dupont");
        assertThat(actual.get(2).get("Address")).isEqualTo("Chartres");

    }

    @Test
    void toListOfMaps_should_return_limited_number_of_rows() {

        Records records = new Records(-1, List.of("First Name", "Name", "Address"), List.of(
            List.of("Henri", "Martin", "Paris"),
            List.of("Jean", "Dupont", "Bordeaux"),
            List.of("Charles", "Magne", "Chartres")
        ));

        List<Map<String, Object>> actual = records.toListOfMaps(2);

        assertThat(actual).hasSize(2);

    }

    @Test
    void toListOfMaps_should_not_out_bound() {

        Records records = new Records(-1, List.of("First Name", "Name", "Address"), List.of(
            List.of("Henri", "Martin", "Paris")
        ));

        List<Map<String, Object>> actual = records.toListOfMaps(2);

        assertThat(actual).hasSize(1);

    }

    @Test
    void toMatrix() {

        Records records = new Records(-1, List.of("First Name", "Name", "Address"), List.of(
            List.of("Henri", "Martin", "Paris"),
            List.of("Jean", "Dupont", "Bordeaux"),
            List.of("Charles", "Magne", "Chartres")
        ));

        Object[][] actual = records.toMatrix();

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
        List<String> headers = asList("lengthOf11", "Header is longer", "7");
        List<List<Object>> rows = singletonList(asList("12345678910", "16", "1234567"));

        Records sut = new Records(-1, headers, rows);

        Map<String, Integer> actual = sut.maximumColumnLength(1);

        assertThat(actual.get("lengthOf11")).isEqualTo(11);
        assertThat(actual.get("Header is longer")).isEqualTo(16);
        assertThat(actual.get("7")).isEqualTo(7);
    }

    @Test
    void column_length_should_be_calculated_from_limited_number_of_rows() {
        List<String> headers = asList("lengthOf11", "Header is longer", "7");
        List<List<Object>> rows = asList(
            asList("12345678910", "16", "1234567"),
            asList("veryveryvery long value", "16", "1234567")
        );

        Records sut = new Records(-1, headers, rows);

        Map<String, Integer> actual = sut.maximumColumnLength(1);

        assertThat(actual.get("lengthOf11")).isEqualTo(11);
        assertThat(actual.get("Header is longer")).isEqualTo(16);
        assertThat(actual.get("7")).isEqualTo(7);
    }

    @Test
    void should_print_formatted_headers() {
        List<String> headers = asList("lengthOf11", "2", "7");
        List<List<Object>> rows = singletonList(asList("12345678910", "12", "1234567"));

        Records sut = new Records(-1, headers, rows);

        String actual = sut.tableHeaders(sut.maximumColumnLength(1));

        assertThat(actual).isEqualTo(
            "| lengthOf11  | 2  | 7       |\n" +
            "------------------------------\n"
        );
    }

    @Test
    void should_print_formatted_rows() {
        List<String> headers = asList("lengthOf11", "Header is longer", "7");
        List<List<Object>> rows = singletonList(asList("12345678910", "42", "1234567"));
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
}
