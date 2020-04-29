package com.chutneytesting.design.infra.storage.dataset;

import static com.chutneytesting.design.infra.storage.db.orient.OrientComponentDB.DATASET_CLASS_PROPERTY_CREATIONDATE;
import static com.chutneytesting.design.infra.storage.db.orient.OrientComponentDB.DATASET_CLASS_PROPERTY_DESCRIPTION;
import static com.chutneytesting.design.infra.storage.db.orient.OrientComponentDB.DATASET_CLASS_PROPERTY_NAME;
import static com.chutneytesting.design.infra.storage.db.orient.OrientComponentDB.DATASET_CLASS_PROPERTY_TAGS;
import static com.chutneytesting.design.infra.storage.db.orient.OrientComponentDB.DATASET_CLASS_PROPERTY_VALUES_MULTIPLE;
import static com.chutneytesting.design.infra.storage.db.orient.OrientComponentDB.DATASET_CLASS_PROPERTY_VALUES_UNIQUE;

import com.chutneytesting.design.domain.dataset.DataSet;
import com.chutneytesting.design.domain.dataset.DataSetMetaData;
import com.orientechnologies.orient.core.metadata.schema.OType;
import com.orientechnologies.orient.core.record.OElement;
import java.sql.Date;

class OrientDataSetMapper {

    static void dataSetToElement(final DataSet dataSet, OElement oDataSet) {
        dataSetMetaDataToElement(dataSet.metadata, oDataSet);
        oDataSet.setProperty(DATASET_CLASS_PROPERTY_VALUES_UNIQUE, dataSet.uniqueValues, OType.EMBEDDEDMAP);
        oDataSet.setProperty(DATASET_CLASS_PROPERTY_VALUES_MULTIPLE, dataSet.multipleValues, OType.EMBEDDEDLIST);
    }

    private static void dataSetMetaDataToElement(final DataSetMetaData dataSetMetaData, OElement oDataSet) {
        oDataSet.setProperty(DATASET_CLASS_PROPERTY_NAME, dataSetMetaData.name, OType.STRING);
        oDataSet.setProperty(DATASET_CLASS_PROPERTY_DESCRIPTION, dataSetMetaData.description, OType.STRING);
        oDataSet.setProperty(DATASET_CLASS_PROPERTY_CREATIONDATE, Date.from(dataSetMetaData.creationDate), OType.DATETIME);
        oDataSet.setProperty(DATASET_CLASS_PROPERTY_TAGS, dataSetMetaData.tags, OType.EMBEDDEDLIST);
    }

    static DataSet elementToDataSet(OElement oDataSet) {
        DataSet.DataSetBuilder builder = DataSet.builder()
            .withId(oDataSet.getIdentity().toString())
            .withMetaData(elementToDataSetMetaData(oDataSet))
            .withUniqueValues(oDataSet.getProperty(DATASET_CLASS_PROPERTY_VALUES_UNIQUE))
            .withMultipleValues(oDataSet.getProperty(DATASET_CLASS_PROPERTY_VALUES_MULTIPLE));

        return builder.build();
    }

    static DataSetMetaData elementToDataSetMetaData(OElement oDataSet) {
        return DataSetMetaData.builder()
            .withName(oDataSet.getProperty(DATASET_CLASS_PROPERTY_NAME))
            .withDescription(oDataSet.getProperty(DATASET_CLASS_PROPERTY_DESCRIPTION))
            .withCreationDate(((java.util.Date) oDataSet.getProperty(DATASET_CLASS_PROPERTY_CREATIONDATE)).toInstant())
            .withTags(oDataSet.getProperty(DATASET_CLASS_PROPERTY_TAGS))
            .build();
    }
}
