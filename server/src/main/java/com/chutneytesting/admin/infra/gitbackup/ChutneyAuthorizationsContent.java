package com.chutneytesting.admin.infra.gitbackup;

import static com.chutneytesting.admin.domain.gitbackup.ChutneyContentCategory.CONF;

import com.chutneytesting.admin.domain.gitbackup.ChutneyContent;
import com.chutneytesting.admin.domain.gitbackup.ChutneyContentCategory;
import com.chutneytesting.admin.domain.gitbackup.ChutneyContentProvider;
import com.chutneytesting.security.api.AuthorizationMapper;
import com.chutneytesting.security.api.AuthorizationsDto;
import com.chutneytesting.security.infra.JsonFileAuthorizations;
import com.chutneytesting.server.core.domain.security.UserRoles;
import com.chutneytesting.tools.Try;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Component
public class ChutneyAuthorizationsContent implements ChutneyContentProvider {

    private final JsonFileAuthorizations jsonFileAuthorizations;
    private final ObjectMapper mapper;

    public ChutneyAuthorizationsContent(
        JsonFileAuthorizations jsonFileAuthorizations,
        @Qualifier("gitObjectMapper") ObjectMapper mapper
    ) {
        this.jsonFileAuthorizations = jsonFileAuthorizations;
        this.mapper = mapper;
    }

    @Override
    public String provider() {
        return "authorizations";
    }

    @Override
    public ChutneyContentCategory category() {
        return CONF;
    }

    @Override
    public Stream<ChutneyContent> getContent() {
        return Try.unsafe(() -> {
            String content = mapper.writeValueAsString(
                AuthorizationMapper.toDto(jsonFileAuthorizations.read())
            );
            return Stream.of(
                ChutneyContent.builder()
                    .withName(provider())
                    .withProvider(provider())
                    .withCategory(category())
                    .withFormat("json")
                    .withContent(content)
                    .build()
            );
        });
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
                try {
                    UserRoles userRoles = AuthorizationMapper.fromDto(mapper.readValue(bytes, AuthorizationsDto.class));
                    jsonFileAuthorizations.save(userRoles);
                } catch (IOException e) {
                    throw new UnsupportedOperationException("Cannot deserialize authorization file : " + filePath, e);
                }
            } catch (IOException e) {
                throw new UnsupportedOperationException("Cannot read authorization file : " + filePath, e);
            }
        }
    }
}
