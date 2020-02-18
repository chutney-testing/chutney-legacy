package com.chutneytesting.admin.domain;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import org.junit.Test;

public class BackupTest {


    @Test(expected = IllegalArgumentException.class)
    public void should_throw_exception_when_instanciate_with_no_backups() {
        new Backup(false, false, false, false, false);
    }

    @Test(expected = RuntimeException.class)
    public void should_throw_exception_when_instanciate_with_unparsable_id() {
        new Backup("unparsableId", false, false, false, false, false);
    }

    @Test
    public void should_set_time_when_instanciate() {
        LocalDateTime now = LocalDateTime.now().minusSeconds(2L).truncatedTo(ChronoUnit.SECONDS);
        Backup backup = new Backup(false, false, false, false, true);
        assertThat(backup.time).isNotNull();
        assertThat(now).isBefore(backup.time);
    }

    @Test
    public void should_generate_string_id_from_time() {
        Backup backup = new Backup(false, false, false, false, true);
        assertThat(backup.id()).isNotBlank();
    }
}
