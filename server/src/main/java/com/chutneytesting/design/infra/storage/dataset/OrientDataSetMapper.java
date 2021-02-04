package com.chutneytesting.design.infra.storage.dataset;

import static com.chutneytesting.design.infra.storage.scenario.compose.orient.OrientComponentDB.DATASET_CLASS_PROPERTY_CREATIONDATE;
import static com.chutneytesting.design.infra.storage.scenario.compose.orient.OrientComponentDB.DATASET_CLASS_PROPERTY_DESCRIPTION;
import static com.chutneytesting.design.infra.storage.scenario.compose.orient.OrientComponentDB.DATASET_CLASS_PROPERTY_NAME;
import static com.chutneytesting.design.infra.storage.scenario.compose.orient.OrientComponentDB.DATASET_CLASS_PROPERTY_TAGS;
import static com.chutneytesting.design.infra.storage.scenario.compose.orient.OrientComponentDB.DATASET_CLASS_PROPERTY_DATATABLE;
import static com.chutneytesting.design.infra.storage.scenario.compose.orient.OrientComponentDB.DATASET_CLASS_PROPERTY_CONSTANTS;

import com.chutneytesting.design.domain.dataset.DataSet;
import com.orientechnologies.orient.core.metadata.schema.OType;
import com.orientechnologies.orient.core.record.OElement;
import java.sql.Date;

class OrientDataSetMapper {

    protected static void dataSetToElement(final DataSet dataSet, OElement oDataSet) {
        dataSetMetaDataToElement(dataSet, oDataSet);
        oDataSet.setProperty(DATASET_CLASS_PROPERTY_CONSTANTS, dataSet.constants, OType.EMBEDDEDMAP);
        oDataSet.setProperty(DATASET_CLASS_PROPERTY_DATATABLE, dataSet.datatable, OType.EMBEDDEDLIST);
    }

    private static void dataSetMetaDataToElement(final DataSet dataSet, OElement oDataSet) {
        oDataSet.setProperty(DATASET_CLASS_PROPERTY_NAME, dataSet.name, OType.STRING);
        oDataSet.setProperty(DATASET_CLASS_PROPERTY_DESCRIPTION, dataSet.description, OType.STRING);
        oDataSet.setProperty(DATASET_CLASS_PROPERTY_CREATIONDATE, Date.from(dataSet.creationDate), OType.DATETIME);
        oDataSet.setProperty(DATASET_CLASS_PROPERTY_TAGS, dataSet.tags, OType.EMBEDDEDLIST);
    }

    protected static DataSet elementToDataSet(OElement oDataSet) {
        DataSet.DataSetBuilder builder = elementToDataSetMetaDataBuilder(oDataSet);

        builder
            .withConstants(oDataSet.getProperty(DATASET_CLASS_PROPERTY_CONSTANTS))
            .withDatatable(oDataSet.getProperty(DATASET_CLASS_PROPERTY_DATATABLE));

        return builder.build();
    }

    protected static DataSet elementToDataSetMetaData(OElement oDataSet) {
        return elementToDataSetMetaDataBuilder(oDataSet).build();
    }

    protected static DataSet.DataSetBuilder elementToDataSetMetaDataBuilder(OElement oDataSet) {
        return DataSet.builder()
            .withId(oDataSet.getIdentity().toString())
            .withName(oDataSet.getProperty(DATASET_CLASS_PROPERTY_NAME))
            .withDescription(oDataSet.getProperty(DATASET_CLASS_PROPERTY_DESCRIPTION))
            .withCreationDate(((java.util.Date) oDataSet.getProperty(DATASET_CLASS_PROPERTY_CREATIONDATE)).toInstant())
            .withTags(oDataSet.getProperty(DATASET_CLASS_PROPERTY_TAGS));
    }
}
