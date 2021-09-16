package com.chutneytesting.admin.infra.gitbackup;

import static com.chutneytesting.admin.domain.gitbackup.ChutneyContentCategory.CONF;

import com.chutneytesting.admin.domain.gitbackup.ChutneyContent;
import com.chutneytesting.admin.domain.gitbackup.ChutneyContentCategory;
import com.chutneytesting.admin.domain.gitbackup.ChutneyContentProvider;
import com.chutneytesting.design.domain.plugins.linkifier.Linkifier;
import com.chutneytesting.design.infra.storage.plugins.linkifier.LinkifierFileRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Component
public class ChutneyLinkifierPluginContent implements ChutneyContentProvider {

    private final LinkifierFileRepository linkifiers;
    private final ObjectMapper mapper;

    public ChutneyLinkifierPluginContent(LinkifierFileRepository linkifiers, @Qualifier("gitObjectMapper") ObjectMapper mapper) {
        this.linkifiers = linkifiers;
        this.mapper = mapper;
    }

    @Override
    public String provider() {
        return "linkifiers";
    }

    @Override
    public ChutneyContentCategory category() {
        return CONF;
    }

    @Override
    public Stream<ChutneyContent> getContent() {
        try {
            return Stream.of(
                ChutneyContent.builder()
                    .withProvider(provider())
                    .withCategory(category())
                    .withName("linkifiers")
                    .withFormat("json")
                    .withContent(mapper.writeValueAsString(linkifiers.getAll()))
                    .build()
            );
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    @Override
    public void importDefaultFolder(Path workingDirectory) {
        Path filePath = providerFolder(workingDirectory).resolve(provider() + ".json");
        importFile(filePath);
    }

    public void importFile(Path filePath) {
        if (Files.exists(filePath)) {
            try {
                byte[] bytes = Files.readAllBytes(filePath);
                List<Linkifier> content = mapper.readValue(bytes, new TypeReference<>() {});
                content.forEach(this.linkifiers::add);
            } catch (IOException e) {
                throw new UnsupportedOperationException("Cannot read configuration file: " + filePath, e);
            }
        }
    }
}
