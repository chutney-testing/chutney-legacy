package com.chutneytesting.dataset.infra;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

import com.chutneytesting.dataset.domain.DataSetRepository;
import com.chutneytesting.server.core.domain.dataset.DataSet;
import com.chutneytesting.server.core.domain.dataset.DataSetAlreadyExistException;
import com.chutneytesting.tools.file.FileUtils;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

public class FileDatasetRepositoryTest {

    private static final String TMP_PATH = org.assertj.core.util.Files.temporaryFolderPath();
    private static final String STORE_PATH = TMP_PATH + File.separator + FileDatasetRepository.ROOT_DIRECTORY_NAME;
    private final DataSetRepository sut = new FileDatasetRepository(TMP_PATH);

    @AfterEach
    public void tearDown() throws IOException {
        FileUtils.cleanFolder(Paths.get(STORE_PATH));
    }

    @Test
    void should_create_and_update_dataset() {
        // Create
        DataSet dataset = DataSet.builder().withName("name").build();
        String id = sut.save(dataset);
        assertThat(Files.exists(Paths.get(STORE_PATH + File.separator + id + ".json"))).isTrue();

        // Update
        DataSet updatedDataset = DataSet.builder().withId(id).withDescription("new description").withName("name").build();
        sut.save(updatedDataset);
        DataSet toValid = sut.findById(id);
        assertThat(toValid.description).isEqualTo("new description");
    }

    @Test
    void should_not_save_new_dataset_already_exist() {
        DataSet dataset = DataSet.builder().withName("name").build();

        sut.save(dataset);

        DataSet newDataset = DataSet.builder().withDescription("Should no be saved").withName("name").build();

        assertThatThrownBy(() -> {
            sut.save(newDataset);
        }).isInstanceOf(DataSetAlreadyExistException.class);

    }
}
