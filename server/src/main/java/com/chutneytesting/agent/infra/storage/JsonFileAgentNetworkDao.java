package com.chutneytesting.agent.infra.storage;

import static com.chutneytesting.ServerConfiguration.CONFIGURATION_FOLDER_SPRING_VALUE;
import static com.chutneytesting.tools.file.FileUtils.initFolder;
import static java.util.Optional.of;

import com.chutneytesting.tools.ThrowingRunnable;
import com.chutneytesting.tools.ThrowingSupplier;
import com.chutneytesting.tools.ZipUtils;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.google.common.io.Files;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.zip.ZipOutputStream;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

@Component
@Primary
public class JsonFileAgentNetworkDao {

    static final Path ROOT_DIRECTORY_NAME = Paths.get("agents");
    static final String AGENTS_FILE_NAME = "endpoints.json";
    private final ObjectMapper objectMapper = buildObjectMapper();
    private final ReadWriteLock rwLock = new ReentrantReadWriteLock(false);
    private final File file;

    public JsonFileAgentNetworkDao(@Value(CONFIGURATION_FOLDER_SPRING_VALUE) String storeFolderPath) {
        Path dir = Paths.get(storeFolderPath).resolve(ROOT_DIRECTORY_NAME).toAbsolutePath();
        initFolder(dir);
        this.file = dir.resolve(AGENTS_FILE_NAME).toFile();
        file.delete(); // TODO keep/refresh network configuration on restart
    }

    public Optional<AgentNetworkForJsonFile> read() {
        return executeWithLocking(rwLock.readLock(), () -> {
            if (!file.exists()) return Optional.empty();
            return of(objectMapper.readValue(file, AgentNetworkForJsonFile.class));
        });
    }

    public void save(AgentNetworkForJsonFile agentEndpointsConfiguration) {
        executeWithLocking(rwLock.writeLock(), (ThrowingRunnable) () -> {
            Files.createParentDirs(file);
            objectMapper.writeValue(file, agentEndpointsConfiguration);
        });
    }

    public void backup(OutputStream outputStream) {
        try (ZipOutputStream zipOutPut = new ZipOutputStream(new BufferedOutputStream(outputStream, 4096))) {
            ZipUtils.compressFile(this.file, this.file.getName(), zipOutPut);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private <T> T executeWithLocking(Lock lock, ThrowingSupplier<T, ? extends Exception> supplier) {
        lock.lock();
        try {
            return supplier.unsafeGet();
        } finally {
            lock.unlock();
        }
    }

    private ObjectMapper buildObjectMapper() {
        ObjectMapper objectMapper = new ObjectMapper()
            .findAndRegisterModules()
            .enable(SerializationFeature.INDENT_OUTPUT);

        return objectMapper.setVisibility(
            objectMapper.getSerializationConfig()
                .getDefaultVisibilityChecker()
                .withFieldVisibility(JsonAutoDetect.Visibility.ANY)
                .withGetterVisibility(JsonAutoDetect.Visibility.NONE)
                .withSetterVisibility(JsonAutoDetect.Visibility.NONE)
                .withCreatorVisibility(JsonAutoDetect.Visibility.NONE)
        );
    }
}
