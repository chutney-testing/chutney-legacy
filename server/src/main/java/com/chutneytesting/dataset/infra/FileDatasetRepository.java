package com.chutneytesting.dataset.infra;

import static com.chutneytesting.ServerConfiguration.CONFIGURATION_FOLDER_SPRING_VALUE;
import static com.chutneytesting.dataset.infra.DatasetMapper.fromDto;
import static com.chutneytesting.dataset.infra.DatasetMapper.toDto;
import static com.chutneytesting.tools.file.FileUtils.createFile;
import static com.chutneytesting.tools.file.FileUtils.initFolder;

import com.chutneytesting.dataset.domain.DataSetRepository;
import com.chutneytesting.server.core.domain.dataset.DataSet;
import com.chutneytesting.tools.file.FileUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class FileDatasetRepository implements DataSetRepository {

    private static final String FILE_EXTENSION = ".json";

    static final Path ROOT_DIRECTORY_NAME = Paths.get("dataset");

    private final Path storeFolderPath;

    private final ObjectMapper objectMapper = new ObjectMapper()
        .findAndRegisterModules()
        .enable(SerializationFeature.INDENT_OUTPUT);

    FileDatasetRepository(@Value(CONFIGURATION_FOLDER_SPRING_VALUE) String storeFolderPath) throws UncheckedIOException {
        this.storeFolderPath = Paths.get(storeFolderPath).resolve(ROOT_DIRECTORY_NAME);
        initFolder(this.storeFolderPath);
    }

    @Override
    public String save(DataSet dataSet) {
        DatasetDto dto = toDto(dataSet);
        Path file = this.storeFolderPath.resolve(dto.id + FILE_EXTENSION);
        createFile(file);
        try {
            String jsonContent = objectMapper.writeValueAsString(dto);
            FileUtils.writeContent(file, jsonContent);
        } catch (IOException e) {
            throw new UncheckedIOException("Cannot save " + file.toUri(), e);
        }
        return dto.id;
    }

    @Override
    public DataSet findById(String fileName) {
        if (null == fileName || fileName.isBlank()) {
            return DataSet.builder().build();
        }

        Path file = this.storeFolderPath.resolve(fileName + FILE_EXTENSION);
        String content = FileUtils.readContent(file);
        try {
            BasicFileAttributes attr = Files.readAttributes(file, BasicFileAttributes.class);
            return fromDto(objectMapper.readValue(content, DatasetDto.class), attr.creationTime().toInstant());
        } catch (IOException e) {
            throw new UncheckedIOException("Cannot read " + file.toUri(), e);
        }
    }

    @Override
    public DataSet removeById(String fileName) {
        Path filePath = this.storeFolderPath.resolve(fileName + FILE_EXTENSION);
        FileUtils.delete(filePath);
        return null;
    }

    @Override
    public List<DataSet> findAll() {
        return FileUtils.doOnListFiles(storeFolderPath, (pathStream) ->
            pathStream
                .filter(Files::isRegularFile)
                .map(FileUtils::getNameWithoutExtension)
                .sorted(Comparator.naturalOrder())
                .map(this::findById)
                .collect(Collectors.toList())
        );
    }

}
