package com.chutneytesting.agent.infra.storage;

import static java.util.Optional.of;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.io.Files;
import com.chutneytesting.tools.ThrowingRunnable;
import com.chutneytesting.tools.ZipUtils;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.util.Optional;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.zip.ZipOutputStream;
import com.chutneytesting.tools.ThrowingSupplier;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

@Component
@Primary
public class JsonFileAgentNetworkDao {

    private final ObjectMapper objectMapper;
    private final ReadWriteLock rwLock = new ReentrantReadWriteLock(false);
    private final File file;

    public JsonFileAgentNetworkDao(
        @Qualifier("persistenceObjectMapper") ObjectMapper objectMapper,
        @Value("${persistence.agentNetwork.file:conf/endpoints.json}") File file) {
        this.objectMapper = objectMapper;
        this.file = file;
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
}
