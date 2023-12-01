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

package com.chutneytesting.environment.infra;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.chutneytesting.environment.api.EmbeddedEnvironmentApi;
import com.chutneytesting.environment.api.dto.EnvironmentDto;
import com.chutneytesting.tools.Try;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class EnvironmentBackupRepositoryTest {

    private final EmbeddedEnvironmentApi environmentApi = mock(EmbeddedEnvironmentApi.class);

    private EnvironmentBackupRepository sut;

    @BeforeEach
    private void init() {
        sut = new EnvironmentBackupRepository(environmentApi);
    }


    @Test()
    public void should_backup_all_environments_in_file_system() throws IOException {
        // Given
        when(environmentApi.listEnvironments())
            .thenReturn(Set.of(
                new EnvironmentDto("envA"),
                new EnvironmentDto("envB")
            ));
        Path backupsRootPath = Try.exec(() -> Files.createTempDirectory(Paths.get("target"), "backups")).runtime();
        Path backupPath = backupsRootPath.resolve(sut.name() + "zip");
        OutputStream out = createFileOutputStream(backupPath);


        // When
        sut.backup(out);

        // Then
        try (ZipFile zipFile = new ZipFile(backupsRootPath
            .resolve(sut.name() + "zip")
            .toFile()
        )) {
            ArrayList<? extends ZipEntry> list = Collections.list(zipFile.entries());

            assertThat(list).hasSize(2);
            assertThat(list).extracting("name")
                .containsExactlyInAnyOrder("envA.json", "envB.json");
        }
    }

    private OutputStream createFileOutputStream(Path path) {
        try {
            return Files.newOutputStream(path);

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
