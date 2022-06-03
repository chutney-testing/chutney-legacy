package com.chutneytesting.admin.infra;

import static com.chutneytesting.admin.infra.FileSystemBackupRepository.ROOT_DIRECTORY_NAME;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.chutneytesting.admin.domain.Backup;
import com.chutneytesting.admin.domain.BackupNotFoundException;
import com.chutneytesting.admin.domain.BackupRepository;
import com.chutneytesting.admin.domain.HomePageRepository;
import com.chutneytesting.agent.domain.explore.CurrentNetworkDescription;
import com.chutneytesting.environment.api.EmbeddedEnvironmentApi;
import com.chutneytesting.environment.api.dto.EnvironmentDto;
import com.chutneytesting.globalvar.domain.GlobalvarRepository;
import com.chutneytesting.jira.domain.JiraRepository;
import com.chutneytesting.scenario.infra.orient.OrientComponentDB;
import com.chutneytesting.tools.Try;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.util.FileSystemUtils;

public class FileSystemBackupRepositoryTest {

    private BackupRepository sut;
    private Path backupsRootPath;

    private final OrientComponentDB orientComponentDB = mock(OrientComponentDB.class);
    private final HomePageRepository homePageRepository = mock(HomePageRepository.class);
    private final EmbeddedEnvironmentApi environmentApi = mock(EmbeddedEnvironmentApi.class);
    private final GlobalvarRepository globalvarRepository = mock(GlobalvarRepository.class);
    private final CurrentNetworkDescription currentNetworkDescription = mock(CurrentNetworkDescription.class);
    private final JiraRepository jiraRepository = mock(JiraRepository.class);

    @BeforeEach
    public void before() {
        backupsRootPath = Try.exec(() -> Files.createTempDirectory(Paths.get("target"), "backups")).runtime();
        sut = new FileSystemBackupRepository(backupsRootPath.toString(), orientComponentDB, homePageRepository, environmentApi, globalvarRepository, currentNetworkDescription, jiraRepository);
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
        sut = new FileSystemBackupRepository(backupsRootPath.toString(), orientComponentDB, homePageRepository, environmentApi, globalvarRepository, currentNetworkDescription, jiraRepository);
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
        assertThat(backups).extracting(Backup::id).containsExactly(backup2Path.getFileName().toString(), backup1Path.getFileName().toString());
    }

    @ParameterizedTest
    @MethodSource("backupObjectsParameters")
    public void should_read_a_backup(boolean homePage, boolean agentsNetwork, boolean environments, boolean components, boolean globalVars) throws IOException {
        // Given
        Path backupPath = stubBackup(Backup.backupIdTimeFormatter.format(LocalDateTime.now().minus(2, ChronoUnit.DAYS)), homePage, agentsNetwork, environments, components, globalVars);
        LocalDateTime backupTimeId = LocalDateTime.parse(backupPath.getFileName().toString(), Backup.backupIdTimeFormatter);

        // When
        Backup backupRead = sut.read(backupPath.getFileName().toString());

        // Then
        assertThat(backupRead.time).isEqualTo(backupTimeId);
        assertThat(backupRead.homePage).isEqualTo(homePage);
        assertThat(backupRead.agentsNetwork).isEqualTo(agentsNetwork);
        assertThat(backupRead.environments).isEqualTo(environments);
        assertThat(backupRead.components).isEqualTo(components);
        assertThat(backupRead.globalVars).isEqualTo(globalVars);
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
        String backupStringId = sut.save(new Backup(false, false, false, false, true, false));

        // Then
        assertThat(backupsRootPath.resolve(ROOT_DIRECTORY_NAME).resolve(backupStringId).toFile().exists()).isTrue();
    }

    @ParameterizedTest
    @MethodSource("backupObjectsParameters")
    public void should_call_right_repository_backup_when_save(boolean homePage, boolean agentsNetwork, boolean environments, boolean components, boolean globalVars, boolean jiraLinks) {
        // When
        sut.save(new Backup(homePage, agentsNetwork, environments, components, globalVars, jiraLinks));

        // Then
        verify(homePageRepository, times(oneIfTrue(homePage))).backup(any());
        verify(currentNetworkDescription, times(oneIfTrue(agentsNetwork))).backup(any());
        verify(environmentApi, times(oneIfTrue(environments))).listEnvironments();
        verify(orientComponentDB, times(oneIfTrue(components))).backup(any());
        verify(globalvarRepository, times(oneIfTrue(globalVars))).backup(any());
        verify(jiraRepository, times(oneIfTrue(jiraLinks))).getFolderPath();
    }

    @Test
    public void should_delete_existing_backup() throws IOException {
        // Given
        Path backupPath = stubBackup(Backup.backupIdTimeFormatter.format(LocalDateTime.now().minus(2, ChronoUnit.DAYS)), true, false, true, false, true);

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

    @Test()
    public void should_backup_all_environments() throws IOException {
        // G
        when(environmentApi.listEnvironments())
            .thenReturn(Set.of(
                new EnvironmentDto("envA"),
                new EnvironmentDto("envB")
            ));

        Backup backup = new Backup(false, false, true, false, false, false);

        // W
        sut.save(backup);

        // T
        try (ZipFile zipFile = new ZipFile(backupsRootPath.resolve("backups")
            .resolve("zip")
            .resolve(backup.id())
            .resolve("environments.zip")
            .toFile()
        )) {
            ArrayList<? extends ZipEntry> list = Collections.list(zipFile.entries());

            assertThat(list).hasSize(2);
            assertThat(list).extracting("name")
                .containsExactlyInAnyOrder("envA.json", "envB.json");
        }
    }

    private Path stubBackup(String backupName) throws IOException {
        return stubBackup(backupName, false, false, false, false, false);
    }

    private Path stubBackup(String backupName, Boolean homePage, Boolean agentsNetwork, Boolean environments, Boolean components, Boolean globalVars) throws IOException {
        Path backupPath = Files.createDirectories(backupsRootPath.resolve(ROOT_DIRECTORY_NAME).resolve(backupName));
        if (homePage) Files.createFile(backupPath.resolve(FileSystemBackupRepository.HOME_PAGE_BACKUP_NAME));
        if (agentsNetwork) Files.createFile(backupPath.resolve(FileSystemBackupRepository.AGENTS_BACKUP_NAME));
        if (environments) Files.createFile(backupPath.resolve(FileSystemBackupRepository.ENVIRONMENTS_BACKUP_NAME));
        if (components) Files.createFile(backupPath.resolve(FileSystemBackupRepository.COMPONENTS_BACKUP_NAME));
        if (globalVars) Files.createFile(backupPath.resolve(FileSystemBackupRepository.GLOBAL_VARS_BACKUP_NAME));
        return backupPath;
    }

    private int oneIfTrue(boolean bool) {
        return bool ? 1 : 0;
    }

    @SuppressWarnings("unused")
    private static Object[] backupObjectsParameters() {
        return new Object[]{
            new Object[]{false, false, false, false, false, true},
            new Object[]{true, false, false, false, false, false},
            new Object[]{false, true, false, false, false, false},
            new Object[]{false, false, true, false, false, false},
            new Object[]{false, false, false, true, false, false},
            new Object[]{false, false, false, false, true, false},
            new Object[]{true, true, false, false, false, false},
            new Object[]{false, true, true, false, false, false},
            new Object[]{false, false, true, true, false, false},
            new Object[]{false, false, false, true, true, false},
            new Object[]{true, false, false, false, true, false},
            new Object[]{true, true, true, false, false, false},
            new Object[]{false, true, true, true, false, false},
            new Object[]{false, false, true, true, true, false},
            new Object[]{true, false, false, true, true, false},
            new Object[]{true, true, false, false, true, false},
            new Object[]{true, true, true, true, false, false},
            new Object[]{false, true, true, true, true, false},
            new Object[]{true, true, false, true, true, false},
            new Object[]{true, true, true, false, true, false},
            new Object[]{true, true, true, true, true, true}
        };
    }

}
