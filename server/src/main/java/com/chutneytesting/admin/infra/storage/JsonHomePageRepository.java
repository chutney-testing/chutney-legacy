package com.chutneytesting.admin.infra.storage;

import static com.chutneytesting.ServerConfiguration.CONFIGURATION_FOLDER_SPRING_VALUE;
import static com.chutneytesting.tools.file.FileUtils.initFolder;

import com.chutneytesting.admin.domain.HomePage;
import com.chutneytesting.admin.domain.HomePageRepository;
import com.chutneytesting.tools.ZipUtils;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.zip.ZipOutputStream;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

@Repository
public class JsonHomePageRepository implements HomePageRepository {

    static final Path ROOT_DIRECTORY_NAME = Paths.get("homepage");
    static final String HOME_PAGE_NAME = "home-page.json";
    private final Path homePageContent;

    private final ObjectMapper objectMapper = new ObjectMapper()
        .findAndRegisterModules()
        .enable(SerializationFeature.INDENT_OUTPUT)
        .setSerializationInclusion(JsonInclude.Include.NON_EMPTY);
    private final ReadWriteLock rwLock;

    public JsonHomePageRepository(@Value(CONFIGURATION_FOLDER_SPRING_VALUE) String storeFolderPath)
        throws UncheckedIOException {

        this.rwLock = new ReentrantReadWriteLock(true);
        Path dir = Paths.get(storeFolderPath).resolve(ROOT_DIRECTORY_NAME).toAbsolutePath();
        initFolder(dir);
        this.homePageContent = dir.resolve(HOME_PAGE_NAME);
    }

    public HomePage load() {
        if (!Files.exists(homePageContent)) {
            return new HomePage("NO CONTENT");
        }

        final Lock readLock;
        (readLock = rwLock.readLock()).lock();
        try (InputStream is = Files.newInputStream(homePageContent)) {
            return objectMapper.readValue(is, HomePage.class);
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        } finally {
            readLock.unlock();
        }
    }

    public HomePage save(HomePage homePage) throws UncheckedIOException {
        final Lock writeLock;
        (writeLock = rwLock.writeLock()).lock();
        try (OutputStream os = Files.newOutputStream(homePageContent)) {
            objectMapper.writeValue(os, homePage);
            return load();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        } finally {
            writeLock.unlock();
        }
    }

    @Override
    public void backup(OutputStream outputStream) {
        try (ZipOutputStream zipOutPut = new ZipOutputStream(new BufferedOutputStream(outputStream, 4096))) {
            ZipUtils.compressFile(this.homePageContent.toFile(), this.homePageContent.getFileName().toString(), zipOutPut);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
