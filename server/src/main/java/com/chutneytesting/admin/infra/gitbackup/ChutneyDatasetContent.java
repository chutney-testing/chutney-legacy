package com.chutneytesting.admin.infra.gitbackup;

import static com.chutneytesting.admin.domain.gitbackup.ChutneyContentCategory.TEST_DATA;

import com.chutneytesting.admin.domain.gitbackup.ChutneyContent;
import com.chutneytesting.admin.domain.gitbackup.ChutneyContentCategory;
import com.chutneytesting.admin.domain.gitbackup.ChutneyContentProvider;
import com.chutneytesting.design.domain.dataset.DataSet;
import com.chutneytesting.design.domain.dataset.DataSetRepository;
import com.chutneytesting.tools.file.FileUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Component
public class ChutneyDatasetContent implements ChutneyContentProvider {

    private final DataSetRepository repository;
    private final ObjectMapper mapper;

    public ChutneyDatasetContent(DataSetRepository repository, @Qualifier("gitObjectMapper") ObjectMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    public String provider() {
        return "dataset";
    }

    @Override
    public ChutneyContentCategory category() {
        return TEST_DATA;
    }

    @Override
    public Stream<ChutneyContent> getContent() {
        return repository.findAll().stream()
            .map(ds -> repository.findById(ds.id))
            .map(ds -> {
                ChutneyContent.ChutneyContentBuilder builder = ChutneyContent.builder()
                    .withProvider(provider())
                    .withCategory(category())
                    .withName(ds.name);
                try {
                    builder
                        .withContent(mapper.writeValueAsString(ds))
                        .withFormat("json");
                } catch (JsonProcessingException e) {
                    throw new RuntimeException(e.getMessage(), e);
                }
                return builder.build();
            });
    }

    @Override
    public void importDefaultFolder(Path workingDirectory) {
        importFolder(providerFolder(workingDirectory));
    }

    public void importFolder(Path folderPath) {
        List<Path> environments = FileUtils.listFiles(folderPath);
        environments.forEach(this::importFile);
    }

    public void importFile(Path filePath) {
        if (Files.exists(filePath)) {
            try {
                byte[] bytes = Files.readAllBytes(filePath);
                try {
                    DataSet ds = mapper.readValue(bytes, DataSet.class);
                    repository.save(ds);
                } catch (IOException e) {
                    throw new UnsupportedOperationException("Cannot deserialize dataset file : " + filePath, e);
                }
            } catch (IOException e) {
                throw new UnsupportedOperationException("Cannot read dataset file : " + filePath, e);
            }
        }
    }
}
