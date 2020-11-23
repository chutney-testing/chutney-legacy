package com.chutneytesting.design.infra.storage.scenario.compose.orient;

import com.orientechnologies.orient.core.db.ODatabaseType;
import java.util.HashMap;
import java.util.Map;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "orient")
public class OrientConfigurationProperties {

    private static final String DEFAULT_DB_NAME = "chutney_component_db";
    private static final String DEFAULT_DB_TYPE = ODatabaseType.PLOCAL.name();
    private static final String DEFAULT_PATH = System.getProperty("user.home") + "/.chutney/orient/";

    private String path = DEFAULT_PATH;
    private DBProperties DBProperties = new DBProperties();
    private Map<String, Object> contextConfiguration = new HashMap<>();

    public DBProperties getDBProperties() {
        return DBProperties;
    }

    public void setDBProperties(DBProperties DBProperties) {
        this.DBProperties = DBProperties;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public Map<String, Object> getContextConfiguration() {
        return contextConfiguration;
    }

    /**
     * @see com.orientechnologies.orient.core.config.OGlobalConfiguration
     */
    public void setContextConfiguration(Map<String, Object> contextConfiguration) {
        this.contextConfiguration = contextConfiguration;
    }

    public static class DBProperties {
        private String dbName = DEFAULT_DB_NAME;
        private String dbType = DEFAULT_DB_TYPE;

        public String getDbName() {
            return dbName;
        }

        public void setDbName(String dbName) {
            this.dbName = dbName;
        }

        public String getDbType() {
            return dbType;
        }

        public void setDbType(String dbType) {
            this.dbType = dbType;
        }
    }
}
