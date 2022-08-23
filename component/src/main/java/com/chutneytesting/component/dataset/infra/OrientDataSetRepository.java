package com.chutneytesting.component.dataset.infra;

import static com.chutneytesting.component.dataset.infra.OrientDataSetMapper.dataSetToElement;
import static com.chutneytesting.component.dataset.infra.OrientDataSetMapper.elementToDataSet;
import static com.chutneytesting.component.dataset.infra.OrientDataSetMapper.elementToDataSetMetaData;
import static com.chutneytesting.component.scenario.infra.orient.OrientComponentDB.DATASET_CLASS;
import static com.chutneytesting.component.scenario.infra.orient.OrientUtils.close;
import static com.chutneytesting.component.scenario.infra.orient.OrientUtils.load;
import static com.chutneytesting.component.scenario.infra.orient.OrientUtils.rollback;

import com.chutneytesting.component.ComposableIdUtils;
import com.chutneytesting.component.dataset.domain.DataSet;
import com.chutneytesting.component.dataset.domain.DataSetNotFoundException;
import com.chutneytesting.component.dataset.domain.DataSetRepository;
import com.chutneytesting.component.scenario.infra.orient.OrientComponentDB;
import com.google.common.collect.Lists;
import com.orientechnologies.orient.core.db.ODatabasePool;
import com.orientechnologies.orient.core.db.ODatabaseSession;
import com.orientechnologies.orient.core.id.ORecordId;
import com.orientechnologies.orient.core.record.OElement;
import com.orientechnologies.orient.core.sql.executor.OResultSet;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

@Repository
public class OrientDataSetRepository implements DataSetRepository {

    private static final Logger LOGGER = LoggerFactory.getLogger(OrientDataSetRepository.class);

    private final ODatabasePool componentDBPool;

    public OrientDataSetRepository(OrientComponentDB orientComponentDB) {
        this.componentDBPool = orientComponentDB.dbPool();
    }

    @Override
    public String save(DataSet dataSet) {
        ODatabaseSession dbSession = null;
        try {
            dbSession = componentDBPool.acquire();
            dbSession.begin();
            OElement savedDataSet = save(dataSet, dbSession);
            dbSession.commit();
            LOGGER.info("Save dataset : " + savedDataSet.toString());
            return ComposableIdUtils.toExternalId(savedDataSet.getIdentity().toString(null).toString());
        } catch (Exception e) {
            rollback(dbSession);
            throw new RuntimeException(e);
        } finally {
            close(dbSession);
        }
    }

    @Override
    public DataSet findById(String dataSetId) {
        String internalId = ComposableIdUtils.toInternalId(dataSetId);
        try (ODatabaseSession dbSession = componentDBPool.acquire()) {
            OElement element = load(internalId, dbSession)
                .orElseThrow(() -> new DataSetNotFoundException(internalId));
            return elementToDataSet(element);
        }
    }

    @Override
    public DataSet removeById(String dataSetId) {
        String internalId = ComposableIdUtils.toInternalId(dataSetId);
        ODatabaseSession dbSession = null;
        try {
            dbSession = componentDBPool.acquire();
            dbSession.begin();
            OElement removedODataSet = load(internalId, dbSession)
                .orElseThrow(() -> new DataSetNotFoundException(internalId))
                .delete();
            DataSet removedDataSet = elementToDataSet(removedODataSet);
            dbSession.commit();
            LOGGER.info("Delete dataset : " + internalId);
            return removedDataSet;
        } finally {
            close(dbSession);
        }
    }

    private static final String QUERY_SELECT_ALL = "SELECT @rid FROM " + DATASET_CLASS;

    @Override
    public List<DataSet> findAll() {
        try (ODatabaseSession dbSession = componentDBPool.acquire()) {
            OResultSet datasets = dbSession.query(QUERY_SELECT_ALL);
            return Lists.newArrayList(datasets).stream()
                .map(rs -> {
                    OElement element = dbSession.load(new ORecordId(rs.getProperty("@rid").toString()));
                    return elementToDataSetMetaData(element);
                })
                .collect(Collectors.toList());
        }
    }

    private OElement save(DataSet dataSet, ODatabaseSession dbSession) {
        String internalId = ComposableIdUtils.toInternalId(dataSet.id);
        Optional<OElement> dataSetRecord = load(internalId, dbSession);
        OElement oDataSet = dataSetRecord.orElse(dbSession.newInstance(DATASET_CLASS));
        dataSetToElement(dataSet, oDataSet);
        return oDataSet.save();
    }
}
