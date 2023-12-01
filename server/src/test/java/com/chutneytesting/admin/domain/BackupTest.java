/*
 * Copyright 2017-2023 Enedis
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.chutneytesting.admin.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

public class BackupTest {


    @Test
    public void should_throw_exception_when_instantiate_with_no_backups() {
        assertThatThrownBy(() -> new Backup(List.of()))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    public void should_throw_exception_when_instantiate_with_null_backups() {
        assertThatThrownBy(() -> new Backup(new ArrayList<>()))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    public void should_throw_exception_when_instantiate_with_unparsable_id() {
        assertThatThrownBy(() -> new Backup("unparsableId", List.of("a backupable")))
            .isInstanceOf(RuntimeException.class);
    }

    @Test
    public void should_set_time_when_instantiate() {
        LocalDateTime now = LocalDateTime.now().minusSeconds(2L).truncatedTo(ChronoUnit.SECONDS);
        Backup backup = new Backup(List.of("a backupable"));
        assertThat(backup.time).isNotNull();
        assertThat(now).isBefore(backup.time);
    }

    @Test
    public void should_generate_string_id_from_time() {
        Backup backup = new Backup(List.of("a backupable"));
        assertThat(backup.getId()).isNotBlank();
    }
}
