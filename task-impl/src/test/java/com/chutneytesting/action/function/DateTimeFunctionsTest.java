package com.chutneytesting.action.function;


import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.time.temporal.Temporal;
import java.time.temporal.TemporalAmount;
import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;

@DisplayName("Date and Time Functions")
public class DateTimeFunctionsTest {

    @Nested
    @DisplayName("date function should parse to Temporal instance corresponding to the given format")
    class DateFunction {
        @Test
        @DisplayName("Instant if no format")
        void should_parse_to_Instant_when_no_format_provided() {
            Instant i = (Instant) DateTimeFunctions.date("2018-01-15T14:38:21Z");
            assertThat(1516027101L).isEqualTo(i.getEpochSecond());
        }

        @Test
        @DisplayName("LocalDate")
        void should_parse_to_LocalDate_when_date_format_provided() {
            Temporal i = DateTimeFunctions.date("2018-01-15", "yyyy-MM-dd");
            assertThat(LocalDate.class).isEqualTo(i.getClass());
            assertThat(1515970800L).isEqualTo(((LocalDate) i).atStartOfDay(ZoneId.of("Europe/Paris")).toEpochSecond());
        }

        @Test
        @DisplayName("ZonedDateTime")
        void should_parse_to_ZonedDateTime_when_zoned_date_time_format_provided() {
            Temporal i = DateTimeFunctions.date("2018-01-15T14:38:21+0200", "yyyy-MM-dd'T'HH:mm:ssx");
            assertThat(ZonedDateTime.class).isEqualTo(i.getClass());
            assertThat(1516019901L).isEqualTo(((ZonedDateTime) i).toEpochSecond());
        }

        @Test
        @DisplayName("LocalDateTime")
        void should_parse_to_LocalDateTime_when_date_time_format_provided() {
            Temporal i = DateTimeFunctions.date("2018-01-15T14:38:21", "yyyy-MM-dd'T'HH:mm:ss");
            assertThat(LocalDateTime.class).isEqualTo(i.getClass());
            assertThat(1516019901L).isEqualTo(((LocalDateTime) i).toEpochSecond(ZoneOffset.ofHours(2)));
        }
    }

    @Test
    @DisplayName("now function should get current time as ZonedDateTime")
    void should_get_current_time_as_ZonedDateTime() {
        ZonedDateTime now = DateTimeFunctions.now();
        assertThat(System.currentTimeMillis()).isGreaterThanOrEqualTo(Instant.from(now).toEpochMilli());
    }

    @Nested
    @DisplayName("dateFormatter functions should build a DateTimeFormatter")
    class DateFormatterFunctions {
        @Test
        @DisplayName("from pattern")
        void should_build_DateTimeFormatter_from_pattern() {
            assertDoesNotThrow(() -> DateTimeFunctions.dateFormatter("yyyy"));
        }

        @Test
        @DisplayName("from pattern and locale")
        void should_build_DateTimeFormatter_from_pattern_and_locale() {
            assertDoesNotThrow(() -> DateTimeFunctions.dateFormatterWithLocale("yyyy", "fr_FR"));
        }

        @ParameterizedTest(name = "{0}")
        @MethodSource("com.chutneytesting.action.function.DateTimeFunctionsTest#isoDateFormatter")
        @DisplayName("from ISO type")
        void should_build_DateTimeFormatter_from_iso_type(String type, DateTimeFormatter expectedIsoDTF) {
            assertSame(expectedIsoDTF, DateTimeFunctions.isoDateFormatter(type));
        }
    }

    @Nested
    @DisplayName("timeAmount function")
    class TimeAmountFunction {
        @Nested
        @DisplayName("should parse a TimeAmount")
        class ValidParse {
            @Test
            @DisplayName("from Chutney Duration format")
            void should_parse_a_TimeAmount_from_a_chutney_duration() {
                TemporalAmount ta = DateTimeFunctions.timeAmount("2 ms");
                assertEquals(2, ((Duration) ta).toMillis());
            }

            @Test
            @DisplayName("from Java time Duration format")
            void should_parse_a_TimeAmount_from_java_time_duration() {
                TemporalAmount ta = DateTimeFunctions.timeAmount("P4DT2H5M3.123456789S");
                assertInstanceOf(Duration.class, ta);
                Duration d = (Duration) ta;
                assertEquals(353103, d.getSeconds());
                assertEquals(123456789, d.getNano());
            }

            @Test
            @DisplayName("from Java time Period format")
            void should_parse_a_TimeAmount_from_java_time_period() {
                TemporalAmount ta = DateTimeFunctions.timeAmount("P2Y3M6W5D");
                assertInstanceOf(Period.class, ta);
                Period p = (Period) ta;
                assertEquals(2, p.getYears());
                assertEquals(3, p.getMonths());
                assertEquals(47, p.getDays());
            }
        }

        @Test
        void should_throw_IllegalArgument_when_text_not_parsable() {
            assertThrows(
                IllegalArgumentException.class,
                () -> DateTimeFunctions.timeAmount("unparsable time amount text")
            );
        }
    }

    @Nested
    @DisplayName("timeUnit function")
    class TimeUnitFunction {
        @Nested
        @DisplayName("should parse a ChronoUnit")
        class ValidParse {
            @Test
            @DisplayName("from Chutney DurationUnit format")
            void should_parse_a_TimeAmount_from_a_chutney_duration_unit() {
                ChronoUnit cu = DateTimeFunctions.timeUnit("days");
                assertEquals(ChronoUnit.DAYS, cu);
            }

            @ParameterizedTest(name = "{0}")
            @EnumSource(ChronoUnit.class)
            @DisplayName("from Java time ChronoUnit enum case insensitive")
            void should_parse_a_TimeAmount_from_java_time_ChronoUnit_enum(ChronoUnit cu) {
                boolean randBool = (int) (Math.random() * 10) % 2 == 0;
                ChronoUnit tu = DateTimeFunctions.timeUnit(randBool ? cu.name() : cu.name().toLowerCase());
                assertEquals(cu, tu);
            }
        }

        @Test
        void should_throw_IllegalArgument_when_text_not_parsable() {
            assertThrows(
                IllegalArgumentException.class,
                () -> DateTimeFunctions.timeUnit("unparsable time unit text")
            );
        }
    }

    private static Stream<Arguments> isoDateFormatter() {
        return Stream.of(
            Arguments.of("INSTANT", DateTimeFormatter.ISO_INSTANT),
            Arguments.of("ZONED_DATE_TIME", DateTimeFormatter.ISO_ZONED_DATE_TIME),
            Arguments.of("DATE_TIME", DateTimeFormatter.ISO_DATE_TIME),
            Arguments.of("DATE", DateTimeFormatter.ISO_DATE),
            Arguments.of("TIME", DateTimeFormatter.ISO_TIME),
            Arguments.of("LOCAL_DATE_TIME", DateTimeFormatter.ISO_LOCAL_DATE_TIME),
            Arguments.of("LOCAL_DATE", DateTimeFormatter.ISO_LOCAL_DATE),
            Arguments.of("LOCAL_TIME", DateTimeFormatter.ISO_LOCAL_TIME),
            Arguments.of("OFFSET_DATE_TIME", DateTimeFormatter.ISO_OFFSET_DATE_TIME),
            Arguments.of("OFFSET_DATE", DateTimeFormatter.ISO_OFFSET_DATE),
            Arguments.of("OFFSET_TIME", DateTimeFormatter.ISO_OFFSET_TIME),
            Arguments.of("ORDINAL_DATE", DateTimeFormatter.ISO_ORDINAL_DATE),
            Arguments.of("ISO_WEEK_DATE", DateTimeFormatter.ISO_WEEK_DATE),
            Arguments.of("BASIC_DATE", DateTimeFormatter.BASIC_ISO_DATE),
            Arguments.of("RFC_DATE_TIME", DateTimeFormatter.RFC_1123_DATE_TIME)
        );
    }
}
