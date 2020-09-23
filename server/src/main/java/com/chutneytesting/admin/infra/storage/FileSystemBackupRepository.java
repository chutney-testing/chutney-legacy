package com.chutneytesting.admin.infra.storage;

import com.chutneytesting.admin.domain.Backup;
import com.chutneytesting.admin.domain.BackupNotFoundException;
import com.chutneytesting.admin.domain.BackupRepository;
import com.chutneytesting.admin.domain.Backupable;
import com.chutneytesting.admin.domain.HomePageRepository;
import com.chutneytesting.agent.domain.explore.CurrentNetworkDescription;
import com.chutneytesting.design.domain.environment.EnvironmentRepository;
import com.chutneytesting.design.domain.globalvar.GlobalvarRepository;
import com.chutneytesting.design.infra.storage.db.orient.OrientComponentDB;
import com.chutneytesting.tools.ZipUtils;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.zip.ZipOutputStream;
import com.chutneytesting.tools.Try;
import com.chutneytesting.tools.file.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.FileSystemUtils;

@Component
public class FileSystemBackupRepository implements BackupRepository {

    private static final Logger LOGGER = LoggerFactory.getLogger(FileSystemBackupRepository.class);

    static final String HOME_PAGE_BACKUP_NAME = "homepage.zip";
    static final String ENVIRONMENTS_BACKUP_NAME = "environments.zip";
    static final String AGENTS_BACKUP_NAME = "agents.zip";
    static final String GLOBAL_VARS_BACKUP_NAME = "globalvars.zip";
    static final String COMPONENTS_BACKUP_NAME = "orient.zip";

    private Path backupsRootPath;

    private final OrientComponentDB orientComponentDB;
    private final HomePageRepository homePageRepository;
    private final EnvironmentRepository environmentRepository;
    private final GlobalvarRepository globalvarRepository;
    private final CurrentNetworkDescription currentNetworkDescription;

    public FileSystemBackupRepository(OrientComponentDB orientComponentDB,
                                      HomePageRepository homePageRepository,
                                      EnvironmentRepository environmentRepository,
                                      GlobalvarRepository globalvarRepository,
                                      CurrentNetworkDescription currentNetworkDescription,
                                      @Value("${chutney.backups.root:backups}") String backupsRootPath) {
        this.backupsRootPath = Paths.get(backupsRootPath).toAbsolutePath();
        Try.exec(() -> Files.createDirectories(this.backupsRootPath)).runtime();

        this.orientComponentDB = orientComponentDB;
        this.homePageRepository = homePageRepository;
        this.environmentRepository = environmentRepository;
        this.globalvarRepository = globalvarRepository;
        this.currentNetworkDescription = currentNetworkDescription;
    }

    @Override
    public void getBackupData(String backupId, OutputStream outputStream) throws IOException {
        Path backupPath = backupsRootPath.resolve(backupId);
        try (ZipOutputStream zipOutPut = new ZipOutputStream(new BufferedOutputStream(outputStream, 4096))) {
            ZipUtils.compressDirectoryToZipfile(backupPath.getParent(), Paths.get(backupId), zipOutPut);
        }
    }

    @Override
    public String save(Backup backup) {
        String backupId = backup.id();

        LOGGER.info("Backup [{}] initiating", backupId);
        Path backupPath = backupsRootPath.resolve(backupId);
        Try.exec(() -> Files.createDirectory(backupPath)).runtime();

        if (backup.homePage) {
            backup(homePageRepository, backupPath.resolve(HOME_PAGE_BACKUP_NAME), "home page");
        }

        if (backup.environments) {
            backup(environmentRepository, backupPath.resolve(ENVIRONMENTS_BACKUP_NAME), "environments");
        }

        if (backup.agentsNetwork) {
            backup(currentNetworkDescription, backupPath.resolve(AGENTS_BACKUP_NAME), "agents network");
        }

        if (backup.globalVars) {
            backup(globalvarRepository, backupPath.resolve(GLOBAL_VARS_BACKUP_NAME), "global vars");
        }

        if (backup.components) {
            backup(orientComponentDB, backupPath.resolve(COMPONENTS_BACKUP_NAME), "orient");
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
                    backupPath.resolve(HOME_PAGE_BACKUP_NAME).toFile().exists(),
                    backupPath.resolve(AGENTS_BACKUP_NAME).toFile().exists(),
                    backupPath.resolve(ENVIRONMENTS_BACKUP_NAME).toFile().exists(),
                    backupPath.resolve(COMPONENTS_BACKUP_NAME).toFile().exists(),
                    backupPath.resolve(GLOBAL_VARS_BACKUP_NAME).toFile().exists()
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
        try (OutputStream outputStream = Files.newOutputStream(backupPath)) {
            backupable.backup(outputStream);
        } catch (IOException e) {
            LOGGER.error("Cannot backup [{}]", backupName, e);
        }
        LOGGER.info("Backup [{}] completed", backupName);
    }
}
