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

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;
import org.apache.commons.lang3.StringUtils;

public class Backup {

    public final static DateTimeFormatter backupIdTimeFormatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    public final LocalDateTime time;
    public final List<String> backupables;

    public Backup(List<String> backupables) {
        if (backupables == null || backupables.isEmpty()) {
            throw new IllegalArgumentException("Nothing to backup !!");
        }

        this.time = getLocalDateTime();
        this.backupables = backupables;
    }

    public Backup(String id, List<String> backupables) {
        this.time = toLocalDate(id);
        this.backupables = backupables;
    }

    private LocalDateTime toLocalDate(String id) {
        return StringUtils.isNotBlank(id) ? LocalDateTime.parse(id, backupIdTimeFormatter) : getLocalDateTime();
    }

    private LocalDateTime getLocalDateTime() {
        return LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);
    }

    public String getId() {
        return this.time.format(backupIdTimeFormatter);
    }
}
