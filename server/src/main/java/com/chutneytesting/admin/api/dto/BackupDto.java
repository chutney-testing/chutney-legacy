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

package com.chutneytesting.admin.api.dto;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class BackupDto {

    private final DateTimeFormatter backupIdTimeFormatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
    private final LocalDateTime time;

    private final List<String> Backupables;

    public BackupDto(LocalDateTime time, List<String> backupables) {
        this.time = time;
        Backupables = backupables;
    }

    public LocalDateTime getTime() {
        return time;
    }

    public List<String> getBackupables() {
        return Backupables;
    }

    public String getId() {
        return this.time.format(backupIdTimeFormatter);
    }
}
