package com.chutneytesting.admin.infra;

import static com.chutneytesting.ServerConfiguration.CONFIGURATION_FOLDER_SPRING_VALUE;
import static com.chutneytesting.tools.file.FileUtils.initFolder;
import static java.util.Optional.ofNullable;

import com.chutneytesting.admin.domain.Backup;
import com.chutneytesting.admin.domain.BackupNotFoundException;
import com.chutneytesting.admin.domain.BackupRepository;
import com.chutneytesting.agent.domain.explore.CurrentNetworkDescription;
import com.chutneytesting.component.scenario.infra.orient.OrientComponentDB;
import com.chutneytesting.environment.api.EmbeddedEnvironmentApi;
import com.chutneytesting.environment.api.EnvironmentApi;
import com.chutneytesting.environment.api.dto.EnvironmentDto;
import com.chutneytesting.jira.domain.JiraRepository;
import com.chutneytesting.server.core.domain.admin.Backupable;
import com.chutneytesting.server.core.domain.globalvar.GlobalvarRepository;
import com.chutneytesting.tools.Try;
import com.chutneytesting.tools.ZipUtils;
import com.chutneytesting.tools.file.FileUtils;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import java.io.BufferedOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.FileSystemUtils;

@Component
public class FileSystemBackupRepository implements BackupRepository {

    private static final Logger LOGGER = LoggerFactory.getLogger(FileSystemBackupRepository.class);

    static final Path ROOT_DIRECTORY_NAME = Paths.get("backups", "zip");
    static final String ENVIRONMENTS_BACKUP_NAME = "environments.zip";
    static final String AGENTS_BACKUP_NAME = "agents.zip";
    static final String GLOBAL_VARS_BACKUP_NAME = "globalvars.zip";
    static final String COMPONENTS_BACKUP_NAME = "orient.zip";
    static final String JIRA_BACKUP_NAME = "jiralinks.zip";

    private final Path backupsRootPath;

    private final OrientComponentDB orientComponentDB;
    private final EnvironmentApi embeddedEnvironmentApi;
    private final GlobalvarRepository globalvarRepository;
    private final CurrentNetworkDescription currentNetworkDescription;
    private final JiraRepository jiraRepository;

    private final ObjectMapper om = new ObjectMapper()
        .findAndRegisterModules()
        .disable(JsonGenerator.Feature.AUTO_CLOSE_TARGET)
        .enable(SerializationFeature.INDENT_OUTPUT)
        .setSerializationInclusion(JsonInclude.Include.NON_EMPTY);

    public FileSystemBackupRepository(@Value(CONFIGURATION_FOLDER_SPRING_VALUE) String backupsRootPath,
                                      OrientComponentDB orientComponentDB,
                                      EmbeddedEnvironmentApi embeddedEnvironmentApi,
                                      GlobalvarRepository globalvarRepository,
                                      CurrentNetworkDescription currentNetworkDescription, JiraRepository jiraRepository) {
        this.backupsRootPath = Paths.get(backupsRootPath).resolve(ROOT_DIRECTORY_NAME).toAbsolutePath();
        initFolder(this.backupsRootPath);

        this.orientComponentDB = orientComponentDB;
        this.embeddedEnvironmentApi = embeddedEnvironmentApi;
        this.globalvarRepository = globalvarRepository;
        this.currentNetworkDescription = currentNetworkDescription;
        this.jiraRepository = jiraRepository;
    }

    @Override
    public void getBackupData(String backupId, OutputStream outputStream) throws IOException {
        Path backupPath = backupsRootPath.resolve(backupId);
        try (ZipOutputStream zipOutPut = new ZipOutputStream(new BufferedOutputStream(outputStream, 4096))) {
            ZipUtils.compressDirectoryToZipfile(backupPath.getParent(), Paths.get(backupId), zipOutPut);
        } catch (FileNotFoundException fnfe) {
            throw new BackupNotFoundException(backupId);
        }
    }

    @Override
    public String save(Backup backup) {
        String backupId = backup.id();

        LOGGER.info("Backup [{}] initiating", backupId);
        Path backupPath = backupsRootPath.resolve(backupId);
        Try.exec(() -> Files.createDirectory(backupPath)).runtime();

        if (backup.environments) {
            backup(backupPath.resolve(ENVIRONMENTS_BACKUP_NAME), "environments", this::backupEnvironments);
        }

        if (backup.agentsNetwork) {
            backup(currentNetworkDescription, backupPath.resolve(AGENTS_BACKUP_NAME), "agents network");
        }

        if (backup.globalVars) {
            backup(globalvarRepository, backupPath.resolve(GLOBAL_VARS_BACKUP_NAME), "global vars");
        }

        if (backup.components) {
            backup(backupPath.resolve(COMPONENTS_BACKUP_NAME), "orient", this::backupOrient);
        }

        if (backup.jiraLinks) {
            backup(backupPath.resolve(JIRA_BACKUP_NAME), "jira links", this::backupJira);
        }

        LOGGER.info("Backup [{}] completed", backupId);
        return backup.id();
    }

    @Override
    public Backup read(String backupId) {
        Path backupPath = backupsRootPath.resolve(backupId);
        if (backupPath.toFile().exists()) {
            try {
                return new Backup(backupId,
                    backupPath.resolve(AGENTS_BACKUP_NAME).toFile().exists(),
                    backupPath.resolve(ENVIRONMENTS_BACKUP_NAME).toFile().exists(),
                    backupPath.resolve(COMPONENTS_BACKUP_NAME).toFile().exists(),
                    backupPath.resolve(GLOBAL_VARS_BACKUP_NAME).toFile().exists(),
                    backupPath.resolve(JIRA_BACKUP_NAME).toFile().exists()
                );
            } catch (RuntimeException re) {
                throw new BackupNotFoundException(backupId);
            }
        } else {
            throw new BackupNotFoundException(backupId);
        }
    }

    @Override
    public void delete(String backupId) {
        Path backupPath = backupsRootPath.resolve(backupId);
        if (Files.exists(backupPath)) {
            Try.exec(() -> FileSystemUtils.deleteRecursively(backupPath)).runtime();
            LOGGER.info("Backup [{}] deleted", backupId);
        } else {
            throw new BackupNotFoundException(backupId);
        }
    }

    @Override
    public List<Backup> list() {
        List<Backup> backups = new ArrayList<>();
        FileUtils.doOnListFiles(backupsRootPath, pathStream -> {
            pathStream.forEach(path -> {
                try {
                    backups.add(read(path.getFileName().toString()));
                } catch (BackupNotFoundException bnfe) {
                    LOGGER.warn("Ignoring unparsable backup [{}]", path.getFileName().toString(), bnfe);
                }
            });
            return Void.TYPE;
        });
        backups.sort(Comparator.comparing(b -> ((Backup) b).time).reversed());
        return backups;
    }

    private void backup(Backupable backupable, Path backupPath, String backupName) {
        backup(backupPath, backupName, backupable::backup);
    }

    private void backup(Path backupPath, String backupName, Consumer<OutputStream> backupInStream) {
        try (OutputStream outputStream = Files.newOutputStream(backupPath)) {
            backupInStream.accept(outputStream);
        } catch (Exception e) {
            LOGGER.error("Cannot backup [{}]", backupName, e);
        }
        LOGGER.info("Backup [{}] completed", backupName);
    }

    private void backupEnvironments(OutputStream outputStream) {
        try (ZipOutputStream zipOutPut = new ZipOutputStream(new BufferedOutputStream(outputStream, 4096))) {
            for (EnvironmentDto env : embeddedEnvironmentApi.listEnvironments()) {
                zipOutPut.putNextEntry(new ZipEntry(env.name + ".json"));
                om.writeValue(zipOutPut, env);
            }
        } catch (IOException ioe) {
            throw new UncheckedIOException(ioe);
        }
    }

    private void backupJira(OutputStream outputStream) {
        Optional<Path> folderPath = ofNullable(jiraRepository.getFolderPath());
        if (folderPath.isPresent()) {
            Path fp = folderPath.get();
            if (StringUtils.isNotBlank(fp.toString())) {
                try (ZipOutputStream zipOutPut = new ZipOutputStream(new BufferedOutputStream(outputStream, 4096))) {
                    ZipUtils.compressDirectoryToZipfile(fp.getParent(), fp.getFileName(), zipOutPut);
                } catch (IOException e) {
                    throw new UncheckedIOException(e);
                }
            }
        }
    }

    private void backupOrient(OutputStream outputStream) {
        orientComponentDB.backup(outputStream);
    }

}
