package com.chutneytesting.design.infra.storage.scenario.compose.orient;

import com.chutneytesting.admin.domain.Backupable;
import com.chutneytesting.design.infra.storage.scenario.compose.orient.changelog.OrientChangelogExecutor;
import com.orientechnologies.orient.core.db.ODatabasePool;
import com.orientechnologies.orient.core.db.ODatabaseType;
import java.io.OutputStream;
import org.springframework.stereotype.Component;

/**
 * Orient Database manager
 */
@Component
public class OrientComponentDB implements Backupable {

    public static final String STEP_CLASS = "FuncStep"; // ComposableStep
    public static final String STEP_CLASS_PROPERTY_NAME = "name";
    public static final String STEP_CLASS_PROPERTY_PARAMETERS = "parameters";
    public static final String STEP_CLASS_PROPERTY_TAGS = "tags";
    public static final String STEP_CLASS_PROPERTY_IMPLEMENTATION = "implementation";
    public static final String STEP_CLASS_PROPERTY_STRATEGY = "strategy";
    public static final String STEP_CLASS_INDEX_NAME = "idx_" + STEP_CLASS + "_" + STEP_CLASS_PROPERTY_NAME;

    public static final String TESTCASE_CLASS = "TestCase";
    public static final String TESTCASE_CLASS_PROPERTY_TITLE = "title";
    public static final String TESTCASE_CLASS_PROPERTY_DESCRIPTION = "description";
    public static final String TESTCASE_CLASS_PROPERTY_CREATIONDATE = "created";
    public static final String TESTCASE_CLASS_PROPERTY_UPDATEDATE = "updated";
    public static final String TESTCASE_CLASS_PROPERTY_AUTHOR = "author";
    public static final String TESTCASE_CLASS_PROPERTY_TAGS = "tags";
    public static final String TESTCASE_CLASS_PROPERTY_PARAMETERS = "parameters";
    public static final String TESTCASE_CLASS_PROPERTY_DATASET_ID = "datasetId";

    public static final String GE_STEP_CLASS = "Denote"; // edge link between parent step and sub step
    public static final String GE_STEP_CLASS_PROPERTY_RANK = "rank"; // substep order
    public static final String GE_STEP_CLASS_PROPERTY_PARAMETERS = "parameters"; // execution parameters

    public static final String DATASET_CLASS = "DataSet";
    public static final String DATASET_HISTORY_CLASS = "DataSetHistory";
    public static final String DATASET_CLASS_PROPERTY_NAME = "name";
    public static final String DATASET_CLASS_PROPERTY_DESCRIPTION = "description";
    public static final String DATASET_CLASS_PROPERTY_CREATIONDATE = "created";
    public static final String DATASET_CLASS_PROPERTY_TAGS = "tags";
    public static final String DATASET_CLASS_PROPERTY_CONSTANTS = "uniqueValues";
    public static final String DATASET_CLASS_PROPERTY_DATATABLE = "multipleValues";
    public static final String DATASET_HISTORY_CLASS_PROPERTY_VERSION = "version";
    public static final String DATASET_HISTORY_CLASS_PROPERTY_DATASET_ID = "dataSetId";
    public static final String DATASET_HISTORY_CLASS_PROPERTY_PATCH= "patch";
    public static final String DATASET_HISTORY_CLASS_INDEX_LAST = "idx_" + DATASET_HISTORY_CLASS + "_" + DATASET_HISTORY_CLASS_PROPERTY_DATASET_ID;


    private final OrientDBManager orientDBManager;
    private final String componentDBName;

    public OrientComponentDB(OrientDBManager orientDBManager, OrientConfigurationProperties orientConfigurationProperties, OrientChangelogExecutor changelogExecution) {
        this.orientDBManager = orientDBManager;
        this.componentDBName = orientConfigurationProperties.getDBProperties().getDbName();

        ODatabaseType dbType = ODatabaseType.valueOf(orientConfigurationProperties.getDBProperties().getDbType());
        String dbName = orientConfigurationProperties.getDBProperties().getDbName();
        orientDBManager.createOrientDB(dbName, dbType);

        changelogExecution.updateWithChangelog(orientDBManager.getDBPool(dbName));
    }

    public ODatabasePool dbPool() {
        return orientDBManager.getDBPool(componentDBName);
    }

    @Override
    public void backup(OutputStream outputStream) {
        orientDBManager.backupOrientDB(componentDBName, outputStream);
    }

}
