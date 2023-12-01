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

package com.chutneytesting.jira.infra;

import static java.util.Optional.ofNullable;

import com.chutneytesting.jira.domain.JiraRepository;
import com.chutneytesting.server.core.domain.admin.Backupable;
import com.chutneytesting.server.core.domain.tools.ZipUtils;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.nio.file.Path;
import java.util.Optional;
import java.util.zip.ZipOutputStream;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

@Component
public class JiraBackupRepository implements Backupable {
    private final JiraRepository jiraRepository;

    public JiraBackupRepository(JiraRepository jiraRepository) {
        this.jiraRepository = jiraRepository;
    }

    @Override
    public void backup(OutputStream outputStream) {
        Optional<Path> folderPath = ofNullable(jiraRepository.getFolderPath());
        if (folderPath.isPresent()) {
            Path fp = folderPath.get();
            if (StringUtils.isNotBlank(fp.toString())) {
                try (ZipOutputStream zipOutPut = new ZipOutputStream(new BufferedOutputStream(outputStream, 4096))) {
                    ZipUtils.compressDirectoryToZipfile(fp.getParent(), fp.getFileName(), zipOutPut);
                } catch (IOException e) {
                    throw new UncheckedIOException(e);
                }
            }
        }
    }

    @Override
    public String name() {
        return "jiralinks";
    }
}
