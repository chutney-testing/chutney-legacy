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
