package com.chutneytesting.design.infra.storage.scenario.compose.orient;

import com.orientechnologies.orient.core.config.OGlobalConfiguration;
import com.orientechnologies.orient.core.db.ODatabasePool;
import com.orientechnologies.orient.core.db.ODatabaseSession;
import com.orientechnologies.orient.core.db.ODatabaseType;
import com.orientechnologies.orient.core.db.OrientDB;
import com.orientechnologies.orient.core.db.OrientDBConfig;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class OrientDBManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(OrientDBManager.class);

    private OrientConfigurationProperties orientConfigurationProperties;
    private OrientDB orientDB;
    private Map<String,ODatabasePool> dbPools = new HashMap<>();

    public OrientDBManager(OrientConfigurationProperties orientConfigurationProperties) {
        this.orientConfigurationProperties = orientConfigurationProperties;
    }

    @PostConstruct
    public void init() {
        openEmbeddedOrient();
    }

    @PreDestroy
    public void destroy() {
        closePools();
        closeEmbeddedOrient();
    }

    public void createOrientDB(String dbName, ODatabaseType dbtype) {
        if (orientDB.createIfNotExists(dbName, dbtype)) {
            LOGGER.info("Database created : {} {}", dbName, dbtype);
        } else {
            LOGGER.warn("Database already exists : {}. Type {} cannot be garanteed...", dbName, dbtype);
        }
        ODatabasePool dbPool = new ODatabasePool(orientDB, dbName, "admin", "admin", contextConfiguration());
        dbPools.put(dbName, dbPool);
    }

    /**
     * Backup database with given name as zil file into given stream.
     * Be aware that the stream will be closed after this method call.
     */
    public void backupOrientDB(String dbName, OutputStream outputStream) throws UncheckedIOException {
        ODatabasePool oDatabasePool = dbPools.get(dbName);
        Optional.ofNullable(oDatabasePool).ifPresent(pool -> {
            try (ODatabaseSession dbSession = pool.acquire()) {
                dbSession.backup(outputStream, Collections.emptyMap(), null, LOGGER::debug, 9, 1024);
            } catch (IOException e) {
                LOGGER.error("Backup database [{}] failed : ", dbName, e);
                throw new UncheckedIOException(e);
            }
        });
    }

    public ODatabasePool getDBPool(String dbName) {
        return dbPools.get(dbName);
    }

    public void dropOrientDB(String dbName) {
        if (dbPools.containsKey(dbName)) {
            dbPools.get(dbName).close();
            dbPools.remove(dbName);
            orientDB.drop(dbName);
            LOGGER.info("Database dropped : {}", dbName);
        }
    }

    private void openEmbeddedOrient() {
        OGlobalConfiguration.setConfiguration(orientConfigurationProperties.getContextConfiguration());

        orientDB = new OrientDB("embedded:" + orientConfigurationProperties.getPath(), contextConfiguration());
        LOGGER.info("Orient opened");
    }

    private void closePools() {
        dbPools.values().forEach(ODatabasePool::close);
        LOGGER.info("Pools closed");
    }

    private void closeEmbeddedOrient() {
        if (orientDB != null) {
            orientDB.close();
            LOGGER.info("Orient closed");
        }
    }

    private OrientDBConfig contextConfiguration() {
        return OrientDBConfig.builder()
            .fromMap(orientConfigurationProperties.getContextConfiguration())
            .build();
    }
}
