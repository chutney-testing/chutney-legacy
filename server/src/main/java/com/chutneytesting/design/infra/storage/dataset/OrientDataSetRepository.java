package com.chutneytesting.design.infra.storage.dataset;

import static com.chutneytesting.design.infra.storage.dataset.OrientDataSetMapper.dataSetToElement;
import static com.chutneytesting.design.infra.storage.dataset.OrientDataSetMapper.elementToDataSet;
import static com.chutneytesting.design.infra.storage.dataset.OrientDataSetMapper.elementToDataSetMetaData;
import static com.chutneytesting.design.infra.storage.scenario.compose.orient.OrientComponentDB.DATASET_CLASS;
import static com.chutneytesting.design.infra.storage.scenario.compose.orient.OrientUtils.close;
import static com.chutneytesting.design.infra.storage.scenario.compose.orient.OrientUtils.load;
import static com.chutneytesting.design.infra.storage.scenario.compose.orient.OrientUtils.rollback;

import com.chutneytesting.design.domain.dataset.DataSet;
import com.chutneytesting.design.domain.dataset.DataSetNotFoundException;
import com.chutneytesting.design.domain.dataset.DataSetRepository;
import com.chutneytesting.design.infra.storage.scenario.compose.orient.OrientComponentDB;
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
            LOGGER.info("Save dataSet : " + savedDataSet.toString());
            return savedDataSet.getIdentity().toString(null).toString();
        } catch (Exception e) {
            rollback(dbSession);
            throw new RuntimeException(e);
        } finally {
            close(dbSession);
        }
    }

    @Override
    public DataSet findById(String dataSetId) {
        try (ODatabaseSession dbSession = componentDBPool.acquire()) {
            OElement element = load(dataSetId, dbSession)
                .orElseThrow(() -> new DataSetNotFoundException(dataSetId));
            return elementToDataSet(element);
        }
    }

    @Override
    public DataSet removeById(String dataSetId) {
        ODatabaseSession dbSession = null;
        try {
            dbSession = componentDBPool.acquire();
            dbSession.begin();
            OElement removedODataSet = load(dataSetId, dbSession)
                .orElseThrow(() -> new DataSetNotFoundException(dataSetId))
                .delete();
            DataSet removedDataSet = elementToDataSet(removedODataSet);
            dbSession.commit();
            LOGGER.info("Delete dataset : " + dataSetId);
            return removedDataSet;
        } finally {
            close(dbSession);
        }
    }

    private static final String QUERY_SELECT_ALL = "SELECT @rid FROM " + DATASET_CLASS;

    @Override
    public List<DataSet> findAll() {
        try (ODatabaseSession dbSession = componentDBPool.acquire()) {
            OResultSet allSteps = dbSession.query(QUERY_SELECT_ALL);
            return Lists.newArrayList(allSteps).stream()
                .map(rs -> {
                    OElement element = dbSession.load(new ORecordId(rs.getProperty("@rid").toString()));
                    return elementToDataSetMetaData(element);
                })
                .collect(Collectors.toList());
        }
    }

    private OElement save(DataSet dataSet, ODatabaseSession dbSession) {
        Optional<OElement> dataSetRecord = load(dataSet.id, dbSession);
        OElement oDataSet = dataSetRecord.orElse(dbSession.newInstance(DATASET_CLASS));
        dataSetToElement(dataSet, oDataSet);
        return oDataSet.save();
    }
}
