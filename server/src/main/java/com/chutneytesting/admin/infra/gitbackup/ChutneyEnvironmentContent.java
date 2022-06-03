package com.chutneytesting.admin.infra.gitbackup;

import static com.chutneytesting.admin.domain.gitbackup.ChutneyContentCategory.CONF;

import com.chutneytesting.admin.domain.gitbackup.ChutneyContent;
import com.chutneytesting.admin.domain.gitbackup.ChutneyContentCategory;
import com.chutneytesting.admin.domain.gitbackup.ChutneyContentProvider;
import com.chutneytesting.environment.api.EmbeddedEnvironmentApi;
import com.chutneytesting.environment.api.EnvironmentApi;
import com.chutneytesting.environment.api.dto.EnvironmentDto;
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
public class ChutneyEnvironmentContent implements ChutneyContentProvider {

    private final EnvironmentApi environmentApi;
    private final ObjectMapper mapper;

    public ChutneyEnvironmentContent(@Qualifier("environmentEmbeddedApplication") EmbeddedEnvironmentApi environmentApi, @Qualifier("gitObjectMapper") ObjectMapper mapper) {
        this.environmentApi = environmentApi;
        this.mapper = mapper;
    }

    @Override
    public String provider() {
        return "env";
    }

    @Override
    public ChutneyContentCategory category() {
        return CONF;
    }

    @Override
    public Stream<ChutneyContent> getContent() {
        return environmentApi.listEnvironments().stream()
            .map(env -> {
                ChutneyContent.ChutneyContentBuilder builder = ChutneyContent.builder()
                    .withProvider(provider())
                    .withCategory(category())
                    .withName(env.name);
                try {
                    builder
                        .withFormat("json")
                        .withContent(mapper.writeValueAsString(env));
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
                    EnvironmentDto env = mapper.readValue(bytes, EnvironmentDto.class);
                    environmentApi.createEnvironment(env, true);
                } catch (IOException e) {
                    throw new UnsupportedOperationException("Cannot deserialize environment file : " + filePath, e);
                }
            } catch (IOException e) {
                throw new UnsupportedOperationException("Cannot read environment file : " + filePath, e);
            }
        }
    }
}
