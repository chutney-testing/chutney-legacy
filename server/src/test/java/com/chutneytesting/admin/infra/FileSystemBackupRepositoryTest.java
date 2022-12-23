package com.chutneytesting.admin.infra;

import static com.chutneytesting.admin.infra.FileSystemBackupRepository.BACKUP_FILE_EXTENSION;
import static com.chutneytesting.admin.infra.FileSystemBackupRepository.ROOT_DIRECTORY_NAME;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.chutneytesting.admin.domain.Backup;
import com.chutneytesting.admin.domain.BackupNotFoundException;
import com.chutneytesting.admin.domain.BackupRepository;
import com.chutneytesting.server.core.domain.admin.Backupable;
import com.chutneytesting.tools.Try;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.util.FileSystemUtils;

public class FileSystemBackupRepositoryTest {

    private BackupRepository sut;
    private Path backupsRootPath;

    private final Backupable aBackupable = mock(Backupable.class);
    private final Backupable otherBackupable = mock(Backupable.class);

    @BeforeEach
    public void before() {
        backupsRootPath = Try.exec(() -> Files.createTempDirectory(Paths.get("target"), "backups")).runtime();
        sut = new FileSystemBackupRepository(backupsRootPath.toString(), List.of(aBackupable, otherBackupable));
    }

    @AfterEach
    public void after() {
        Try.exec(() -> FileSystemUtils.deleteRecursively(backupsRootPath)).runtime();
    }

    @Test
    public void should_create_backup_root_path_when_instantiate() throws IOException {
        // Given
        Files.deleteIfExists(backupsRootPath.resolve(ROOT_DIRECTORY_NAME));
        backupsRootPath = Files.createTempDirectory(Paths.get("target"), "freshNewBackups");
        // When
        sut = new FileSystemBackupRepository(backupsRootPath.toString(), List.of(aBackupable));
        // Then
        assertThat(backupsRootPath.resolve(ROOT_DIRECTORY_NAME).toFile().exists()).isTrue();
    }

    @Test
    public void should_list_all_existing_backups_ignoring_unparsable_ones() throws IOException {
        // Given
        LocalDateTime now = LocalDateTime.now();
        Path backup1Path = stubBackup(Backup.backupIdTimeFormatter.format(now.minus(2, ChronoUnit.DAYS)));
        Path backup2Path = stubBackup(Backup.backupIdTimeFormatter.format(now.minus(1, ChronoUnit.DAYS)));
        stubBackup("wrongBackupId");

        // When
        List<Backup> backups = sut.list();

        // Then
        assertThat(backups).hasSize(2);
        assertThat(backups).extracting(Backup::getId).containsExactly(backup2Path.getFileName().toString(), backup1Path.getFileName().toString());
    }

    @Test
    public void should_read_a_backup() throws IOException {
        // Given
        when(aBackupable.name()).thenReturn("aBackup");
        when(otherBackupable.name()).thenReturn("anOtherBackup");
        Path backupPath = stubBackup(Backup.backupIdTimeFormatter.format(LocalDateTime.now().minus(2, ChronoUnit.DAYS)), List.of(aBackupable, otherBackupable));
        LocalDateTime backupTimeId = LocalDateTime.parse(backupPath.getFileName().toString(), Backup.backupIdTimeFormatter);

        // When
        Backup backupRead = sut.read(backupPath.getFileName().toString());

        // Then
        assertThat(backupRead.time).isEqualTo(backupTimeId);
        assertThat(backupRead.backupables).containsExactly("aBackup", "anOtherBackup");
    }

    @Test()
    public void should_throw_exception_when_read_unknown_backup() {
        assertThatThrownBy(() -> sut.read("unknownBackupId"))
            .isInstanceOf(BackupNotFoundException.class);
    }

    @Test()
    public void should_throw_exception_when_read_unparsable_backup_id() throws IOException {
        // Given
        Path backupPath = stubBackup("unparsableBackupId");

        // When
        assertThatThrownBy(() -> sut.read(backupPath.getFileName().toString()))
            .isInstanceOf(BackupNotFoundException.class);
    }

    @Test
    public void should_save_backup_as_directory() {
        // When
        String backupStringId = sut.save(new Backup(Arrays.asList("a backupable")));

        // Then
        assertThat(backupsRootPath.resolve(ROOT_DIRECTORY_NAME).resolve(backupStringId).toFile().exists()).isTrue();
    }

    @Test
    public void should_call_right_repository_backup_when_save() {
        // Given
        when(aBackupable.name()).thenReturn("aBackup");
        when(otherBackupable.name()).thenReturn("anOtherBackup");

        // When
        sut.save(new Backup(List.of(aBackupable.name())));

        // Then
        verify(aBackupable, times(1)).backup(any());
        verify(otherBackupable, never()).backup(any());
    }

    @Test
    public void should_delete_existing_backup() throws IOException {
        // Given
        when(aBackupable.name()).thenReturn("aBackup");
        Path backupPath = stubBackup(Backup.backupIdTimeFormatter.format(LocalDateTime.now().minus(2, ChronoUnit.DAYS)), List.of(aBackupable));

        // When
        sut.delete(backupPath.getFileName().toString());

        // Then
        assertThat(backupPath.toFile().exists()).isFalse();
    }

    @Test()
    public void should_throw_exception_when_delete_unknown_backup() {
        assertThatThrownBy(() -> sut.delete("unknownBackupId"))
            .isInstanceOf(BackupNotFoundException.class);
    }

    private Path stubBackup(String backupName) throws IOException {
        return stubBackup(backupName, List.of());
    }

    private Path stubBackup(String backupName, List<Backupable> backupables) throws IOException {
        Path backupPath = Files.createDirectories(backupsRootPath.resolve(ROOT_DIRECTORY_NAME).resolve(backupName));
        backupables.stream().forEach(backupable -> createBackupFile(backupPath, backupable));
        return backupPath;
    }

    private void createBackupFile(Path backupPath, Backupable backupable) {
        try {
            Files.createFile(backupPath.resolve(backupable.name() + BACKUP_FILE_EXTENSION));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
