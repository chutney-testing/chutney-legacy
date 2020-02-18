package com.chutneytesting.task.function;


import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.temporal.Temporal;
import org.junit.Test;

public class DateFunctionTest {

    @Test
    public void shouldParseToInstantWhenNoFormatterProvided() throws Exception {
        Instant i = (Instant) DateFunction.date("2018-01-15T14:38:21Z");
        assertThat(1516027101L).isEqualTo(i.getEpochSecond());
    }

    @Test
    public void shouldParseToLocaDateWhenDateFormatterProvided() throws Exception {
        Temporal i = DateFunction.date("2018-01-15", "yyyy-MM-dd");
        assertThat(LocalDate.class).isEqualTo(i.getClass());
        assertThat(1515970800L).isEqualTo(((LocalDate)i).atStartOfDay(ZoneId.of("Europe/Paris")).toEpochSecond());
    }

    @Test
    public void shouldParseToZonedDateTimeWhenDateFormatterProvided() throws Exception {
        Temporal i = DateFunction.date("2018-01-15T14:38:21+0200", "yyyy-MM-dd'T'HH:mm:ssx");
        assertThat(ZonedDateTime.class).isEqualTo(i.getClass());
        assertThat(1516019901L).isEqualTo(((ZonedDateTime)i).toEpochSecond());
    }

    @Test
    public void shouldParseToLocalDateTimeWhenDateFormatterProvided() throws Exception {
        Temporal i = DateFunction.date("2018-01-15T14:38:21", "yyyy-MM-dd'T'HH:mm:ss");
        assertThat(LocalDateTime.class).isEqualTo(i.getClass());
        assertThat(1516019901L).isEqualTo(((LocalDateTime)i).toEpochSecond(ZoneOffset.ofHours(2)));
    }
}
