package com.chutneytesting.design.infra.storage.dataset;

import static com.chutneytesting.design.infra.storage.dataset.DataSetPatchUtils.extractValues;
import static com.chutneytesting.design.infra.storage.dataset.DataSetPatchUtils.patchString;
import static com.chutneytesting.design.infra.storage.dataset.OrientDataSetHistoryMapper.dataSetPatchToElement;
import static com.chutneytesting.design.infra.storage.dataset.OrientDataSetHistoryMapper.elementToDataSetPatch;
import static com.chutneytesting.design.infra.storage.dataset.OrientDataSetMapper.elementToDataSetMetaDataBuilder;
import static com.chutneytesting.design.infra.storage.scenario.compose.orient.OrientComponentDB.DATASET_HISTORY_CLASS;
import static com.chutneytesting.design.infra.storage.scenario.compose.orient.OrientComponentDB.DATASET_HISTORY_CLASS_PROPERTY_DATASET_ID;
import static com.chutneytesting.design.infra.storage.scenario.compose.orient.OrientComponentDB.DATASET_HISTORY_CLASS_PROPERTY_VERSION;
import static com.chutneytesting.design.infra.storage.scenario.compose.orient.OrientUtils.close;
import static com.chutneytesting.design.infra.storage.scenario.compose.orient.OrientUtils.resultSetToCount;
import static com.chutneytesting.design.infra.storage.scenario.compose.orient.OrientUtils.rollback;
import static java.time.temporal.ChronoUnit.MILLIS;
import static java.util.Optional.empty;

import com.chutneytesting.design.domain.dataset.DataSet;
import com.chutneytesting.design.domain.dataset.DataSetHistoryRepository;
import com.chutneytesting.design.domain.dataset.DataSetNotFoundException;
import com.chutneytesting.design.infra.storage.scenario.compose.orient.OrientComponentDB;
import com.google.common.collect.Lists;
import com.orientechnologies.orient.core.db.ODatabasePool;
import com.orientechnologies.orient.core.db.ODatabaseSession;
import com.orientechnologies.orient.core.id.ORecordId;
import com.orientechnologies.orient.core.record.OElement;
import com.orientechnologies.orient.core.sql.executor.OResult;
import com.orientechnologies.orient.core.sql.executor.OResultSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

@Repository
public class OrientDataSetHistoryRepository implements DataSetHistoryRepository {

    private static final Logger LOGGER = LoggerFactory.getLogger(OrientDataSetHistoryRepository.class);

    private ODatabasePool componentDBPool;

    public OrientDataSetHistoryRepository(OrientComponentDB orientComponentDB) {
        this.componentDBPool = orientComponentDB.dbPool();
    }

    private static final String QUERY_LAST_VERSION =
        "SELECT max(" + DATASET_HISTORY_CLASS_PROPERTY_VERSION + ") as maxVersion FROM " + DATASET_HISTORY_CLASS +
            " WHERE " + DATASET_HISTORY_CLASS_PROPERTY_DATASET_ID + " = ?";

    @Override
    public Integer lastVersion(String dataSetId) {
        try (ODatabaseSession dbSession = componentDBPool.acquire()) {
            OResultSet lastVersion = dbSession.query(QUERY_LAST_VERSION, new ORecordId(dataSetId));
            if (lastVersion.hasNext()) {
                return lastVersion.next().getProperty("maxVersion");
            }
            throw new DataSetNotFoundException(dataSetId);
        }
    }

    @Override
    public Optional<Pair<String, Integer>> addVersion(DataSet newDataSet) {
        ODatabaseSession dbSession = null;
        DataSet previousDataSet = null;
        try {
            // Retrieve last version
            Integer nextVersion = nextVersion(newDataSet.id);
            if (nextVersion > 1) {
                previousDataSet = version(newDataSet.id, nextVersion - 1);
            }
            // Create patch
            DataSetPatch dataSetPatch = DataSetPatch.builder()
                .fromDataSets(newDataSet, previousDataSet)
                .withRefId(newDataSet.id)
                .withVersion(nextVersion)
                .build();

            if (dataSetPatch.hasPatchedValues()) {
                dbSession = componentDBPool.acquire();
                dbSession.begin();
                OElement oDataSetPatch = dbSession.newInstance(DATASET_HISTORY_CLASS);
                dataSetPatchToElement(dataSetPatch, oDataSetPatch);
                oDataSetPatch.save();
                dbSession.commit();
                LOGGER.info("Save version {} of dataset {}-{}", dataSetPatch.version, dataSetPatch.refId, newDataSet.name);
                return Optional.of(Pair.of(oDataSetPatch.getIdentity().toString(null).toString(), dataSetPatch.version));
            }
        } catch (Exception e) {
            rollback(dbSession);
            throw new RuntimeException(e);
        } finally {
            close(dbSession);
        }
        return empty();
    }

    private static final String QUERY_ALL_VERSIONS =
        "SELECT FROM " + DATASET_HISTORY_CLASS +
            " WHERE " + DATASET_HISTORY_CLASS_PROPERTY_DATASET_ID + " = ?" +
            " ORDER BY " + DATASET_HISTORY_CLASS_PROPERTY_VERSION;

    @Override
    public Map<Integer, DataSet> allVersions(String dataSetId) {
        try (ODatabaseSession dbSession = componentDBPool.acquire()) {
            OResultSet allVersions = dbSession.query(QUERY_ALL_VERSIONS, dataSetId);
            Map<Integer, DataSet> allVersionsMap = new LinkedHashMap<>();
            while(allVersions.hasNext()) {
                Optional<OElement> element = allVersions.next().getElement();
                if (element.isPresent()) {
                    OElement oDataSet = element.get();
                    DataSet.DataSetBuilder dataSetBuilder = elementToDataSetMetaDataBuilder(oDataSet)
                        .withId(dataSetId);
                    allVersionsMap.put(oDataSet.getProperty(DATASET_HISTORY_CLASS_PROPERTY_VERSION), dataSetBuilder.build());
                }
            }
            return allVersionsMap;
        }
    }

    private static final String QUERY_FIND_VERSION = "SELECT FROM " + DATASET_HISTORY_CLASS +
        " WHERE " + DATASET_HISTORY_CLASS_PROPERTY_DATASET_ID + " = ?" +
        " AND " + DATASET_HISTORY_CLASS_PROPERTY_VERSION + " <= ?" +
        " ORDER BY " + DATASET_HISTORY_CLASS_PROPERTY_VERSION;

    @Override
    public DataSet version(String dataSetId, Integer version) {
        try (ODatabaseSession dbSession = componentDBPool.acquire()) {
            OResultSet query = dbSession.query(QUERY_FIND_VERSION, dataSetId, version);
            if (query.hasNext()) {
                DataSet.DataSetBuilder dataSetBuilder = DataSet.builder().withId(dataSetId);
                String datasetValues = "";
                for (OResult rs : Lists.newArrayList(query)) {
                    Optional<OElement> element = rs.getElement();
                    if (element.isPresent()) {
                        DataSetPatch dataSetPatch = elementToDataSetPatch(element.get());
                        if (dataSetPatch.name != null) {
                            dataSetBuilder.withName(dataSetPatch.name);
                        }
                        if (dataSetPatch.description != null) {
                            dataSetBuilder.withDescription(dataSetPatch.description);
                        }
                        dataSetBuilder.withCreationDate(dataSetPatch.creationDate.truncatedTo(MILLIS));
                        if (dataSetPatch.tags != null) {
                            dataSetBuilder.withTags(dataSetPatch.tags);
                        }
                        datasetValues = patchString(datasetValues, dataSetPatch);
                    }
                }
                Pair<Map<String, String>, List<Map<String, String>>> values = extractValues(datasetValues);
                return dataSetBuilder
                    .withUniqueValues(values.getLeft())
                    .withMultipleValues(values.getRight())
                    .build();
            }
        } catch (Exception e) {
            LOGGER.error("Error finding dataset [{}] version {}", dataSetId, version, e);
            throw new RuntimeException(e);
        }
        throw new DataSetNotFoundException(dataSetId);
    }

    private static final String QUERY_DELETE_DATASET = "DELETE FROM " + DATASET_HISTORY_CLASS + " WHERE " + DATASET_HISTORY_CLASS_PROPERTY_DATASET_ID + " = ?";

    @Override
    public void removeHistory(String dataSetId) {
        try (ODatabaseSession dbSession = componentDBPool.acquire()) {
            OResultSet rs = dbSession.command(QUERY_DELETE_DATASET, dataSetId);
            LOGGER.info("Delete {} versions of DataSet {}", resultSetToCount(rs), dataSetId);
        }
    }

    private Integer nextVersion(String dataSetId) {
        try {
            return lastVersion(dataSetId) + 1;
        } catch (DataSetNotFoundException e) {
            return 1;
        }
    }
}
