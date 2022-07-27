package com.chutneytesting.component.dataset.infra;

import static com.chutneytesting.component.scenario.infra.orient.OrientComponentDB.DATASET_CLASS_PROPERTY_CREATIONDATE;
import static com.chutneytesting.component.scenario.infra.orient.OrientComponentDB.DATASET_CLASS_PROPERTY_DESCRIPTION;
import static com.chutneytesting.component.scenario.infra.orient.OrientComponentDB.DATASET_CLASS_PROPERTY_NAME;
import static com.chutneytesting.component.scenario.infra.orient.OrientComponentDB.DATASET_CLASS_PROPERTY_TAGS;
import static com.chutneytesting.component.scenario.infra.orient.OrientComponentDB.DATASET_HISTORY_CLASS_PROPERTY_DATASET_ID;
import static com.chutneytesting.component.scenario.infra.orient.OrientComponentDB.DATASET_HISTORY_CLASS_PROPERTY_PATCH;
import static com.chutneytesting.component.scenario.infra.orient.OrientComponentDB.DATASET_HISTORY_CLASS_PROPERTY_VERSION;

import com.orientechnologies.orient.core.metadata.schema.OType;
import com.orientechnologies.orient.core.record.OElement;
import java.sql.Date;

class OrientDataSetHistoryMapper {

    protected static void dataSetPatchToElement(final DataSetPatch dataSetPatch, OElement oDataSetPatch) {
        oDataSetPatch.setProperty(DATASET_HISTORY_CLASS_PROPERTY_DATASET_ID, dataSetPatch.refId, OType.LINK);
        oDataSetPatch.setProperty(DATASET_CLASS_PROPERTY_NAME, dataSetPatch.name, OType.STRING);
        oDataSetPatch.setProperty(DATASET_CLASS_PROPERTY_DESCRIPTION, dataSetPatch.description, OType.STRING);
        oDataSetPatch.setProperty(DATASET_CLASS_PROPERTY_CREATIONDATE, Date.from(dataSetPatch.creationDate), OType.DATETIME);
        oDataSetPatch.setProperty(DATASET_CLASS_PROPERTY_TAGS, dataSetPatch.tags, OType.EMBEDDEDLIST);
        oDataSetPatch.setProperty(DATASET_HISTORY_CLASS_PROPERTY_PATCH, dataSetPatch.unifiedDiffValues, OType.STRING);
        oDataSetPatch.setProperty(DATASET_HISTORY_CLASS_PROPERTY_VERSION, dataSetPatch.version, OType.INTEGER);
    }

    protected static DataSetPatch elementToDataSetPatch(OElement oDataSetPatch) {
        return DataSetPatch.builder()
            .withId(oDataSetPatch.getIdentity().toString())
            .withRefId(((OElement)oDataSetPatch.getProperty(DATASET_HISTORY_CLASS_PROPERTY_DATASET_ID)).getIdentity().toString())
            .withName(oDataSetPatch.getProperty(DATASET_CLASS_PROPERTY_NAME))
            .withDescription(oDataSetPatch.getProperty(DATASET_CLASS_PROPERTY_DESCRIPTION))
            .withCreationDate(((java.util.Date) oDataSetPatch.getProperty(DATASET_CLASS_PROPERTY_CREATIONDATE)).toInstant())
            .withTags(oDataSetPatch.getProperty(DATASET_CLASS_PROPERTY_TAGS))
            .withUnifiedDiffValues(oDataSetPatch.getProperty(DATASET_HISTORY_CLASS_PROPERTY_PATCH))
            .withVersion(oDataSetPatch.getProperty(DATASET_HISTORY_CLASS_PROPERTY_VERSION))
            .build();
    }
}
