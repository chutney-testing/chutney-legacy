package com.chutneytesting.dataset.infra;

import static org.assertj.core.api.Assertions.assertThat;

import com.chutneytesting.dataset.domain.DataSetRepository;
import com.chutneytesting.server.core.domain.dataset.DataSet;
import com.chutneytesting.tools.file.FileUtils;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

public class FileDatasetRepositoryTest {

    private static final String TMP_PATH = org.assertj.core.util.Files.temporaryFolderPath();
    private static final String STORE_PATH = TMP_PATH + "/" + FileDatasetRepository.ROOT_DIRECTORY_NAME;
    private final DataSetRepository sut = new FileDatasetRepository(TMP_PATH);

    @AfterEach
    public void tearDown() throws IOException {
        FileUtils.cleanFolder(Paths.get(STORE_PATH));
    }

    @Test
    void should_save_dataset() {
        DataSet dataset = DataSet.builder().build();

        String actualFilePath = sut.save(dataset);

        assertThat(Files.exists(Paths.get(actualFilePath))).isTrue();
    }
}
