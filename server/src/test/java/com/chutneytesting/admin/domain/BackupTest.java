package com.chutneytesting.admin.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import org.junit.jupiter.api.Test;

public class BackupTest {


    @Test
    public void should_throw_exception_when_instantiate_with_no_backups() {
        assertThatThrownBy(() -> new Backup(false, false, false, false, false))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    public void should_throw_exception_when_instantiate_with_unparsable_id() {
        assertThatThrownBy(() -> new Backup("unparsableId", false, false, false, false, false))
            .isInstanceOf(RuntimeException.class);
    }

    @Test
    public void should_set_time_when_instantiate() {
        LocalDateTime now = LocalDateTime.now().minusSeconds(2L).truncatedTo(ChronoUnit.SECONDS);
        Backup backup = new Backup(false, false, false, true, false);
        assertThat(backup.time).isNotNull();
        assertThat(now).isBefore(backup.time);
    }

    @Test
    public void should_generate_string_id_from_time() {
        Backup backup = new Backup(false, false, false, true, false);
        assertThat(backup.id()).isNotBlank();
    }
}
