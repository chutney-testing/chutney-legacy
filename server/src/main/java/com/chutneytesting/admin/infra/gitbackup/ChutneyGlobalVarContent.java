package com.chutneytesting.admin.infra.gitbackup;

import static com.chutneytesting.admin.domain.gitbackup.ChutneyContentCategory.TEST_DATA;

import com.chutneytesting.admin.domain.gitbackup.ChutneyContent;
import com.chutneytesting.admin.domain.gitbackup.ChutneyContentCategory;
import com.chutneytesting.admin.domain.gitbackup.ChutneyContentProvider;
import com.chutneytesting.server.core.domain.globalvar.GlobalvarRepository;
import com.chutneytesting.tools.file.FileUtils;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;
import org.springframework.stereotype.Component;

@Component
public class ChutneyGlobalVarContent implements ChutneyContentProvider {

    private final GlobalvarRepository repository;

    public ChutneyGlobalVarContent(GlobalvarRepository repository) {
        this.repository = repository;
    }

    @Override
    public String provider() {
        return "global_var";
    }

    @Override
    public ChutneyContentCategory category() {
        return TEST_DATA;
    }

    @Override
    public Stream<ChutneyContent> getContent() {
        return repository.list().stream()
            .map(name ->
                ChutneyContent.builder()
                    .withProvider(provider())
                    .withCategory(category())
                    .withName(name)
                    .withContent(repository.getFileContent(name))
                    .build()
            );
    }

    @Override
    public void importDefaultFolder(Path workingDirectory) {
        importFolder(providerFolder(workingDirectory));
    }

    public void importFolder(Path folderPath) {
        List<Path> globalVars = FileUtils.listFiles(folderPath);
        globalVars.forEach(this::importFile);
    }

    public void importFile(Path filePath) {
        if (Files.exists(filePath)) {
            try {
                String content = new String(Files.readAllBytes(filePath));
                repository.saveFile(FileUtils.getNameWithoutExtension(filePath), content);
            } catch (IOException e) {
                throw new UnsupportedOperationException("Cannot read global var file : " + filePath, e);
            }
        }
    }

}
