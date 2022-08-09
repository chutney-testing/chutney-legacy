package com.chutneytesting.security.infra;

import static com.chutneytesting.ServerConfiguration.CONFIGURATION_FOLDER_SPRING_VALUE;
import static com.chutneytesting.tools.file.FileUtils.initFolder;

import com.chutneytesting.security.api.AuthorizationMapper;
import com.chutneytesting.security.api.AuthorizationsDto;
import com.chutneytesting.security.domain.Authorizations;
import com.chutneytesting.server.core.domain.security.UserRoles;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

@Repository
public class JsonFileAuthorizations implements Authorizations {

    private static final String AUTHORIZATION_FILE_NAME = "authorization.json";
    private static final Path ROOT_DIRECTORY_NAME = Paths.get("roles");

    private final Path authorizationFilePath;

    private final ObjectMapper om = new ObjectMapper()
        .findAndRegisterModules()
        .enable(SerializationFeature.INDENT_OUTPUT)
        .setSerializationInclusion(JsonInclude.Include.ALWAYS);

    JsonFileAuthorizations(@Value(CONFIGURATION_FOLDER_SPRING_VALUE) String configFolderPath) throws UncheckedIOException {
        Path storeFolderPath = Paths.get(configFolderPath).resolve(ROOT_DIRECTORY_NAME);
        this.authorizationFilePath = storeFolderPath.resolve(AUTHORIZATION_FILE_NAME);

        initFolder(storeFolderPath);
        initAuthorizationFile();
    }

    @Override
    public UserRoles read() {
        try {
            byte[] bytes = Files.readAllBytes(authorizationFilePath);
            try {
                return AuthorizationMapper.fromDto(om.readValue(bytes, AuthorizationsDto.class));
            } catch (IOException e) {
                throw new UnsupportedOperationException("Cannot deserialize authorization file : " + authorizationFilePath, e);
            }
        } catch (IOException e) {
            throw new UnsupportedOperationException("Cannot read authorization file : " + authorizationFilePath, e);
        }
    }

    @Override
    public void save(UserRoles userRoles) {
        try {
            byte[] bytes = om.writeValueAsBytes(AuthorizationMapper.toDto(userRoles));
            try {
                Files.write(authorizationFilePath, bytes);
            } catch (IOException e) {
                throw new UnsupportedOperationException("Cannot write authorization file : " + authorizationFilePath, e);
            }
        } catch (IOException e) {
            throw new IllegalArgumentException("Cannot serialize authorizations", e);
        }
    }

    private void initAuthorizationFile() {
        if (Files.notExists(this.authorizationFilePath)) {
            save(UserRoles.builder().build());
        }
    }
}
