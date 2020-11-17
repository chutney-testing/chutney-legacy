package com.chutneytesting.design.infra.storage.db.orient;

import static java.util.Optional.empty;

import com.orientechnologies.orient.core.db.ODatabaseSession;
import com.orientechnologies.orient.core.exception.ORecordNotFoundException;
import com.orientechnologies.orient.core.id.ORecordId;
import com.orientechnologies.orient.core.metadata.schema.OClass;
import com.orientechnologies.orient.core.metadata.schema.OType;
import com.orientechnologies.orient.core.record.OElement;
import com.orientechnologies.orient.core.sql.executor.OResult;
import com.orientechnologies.orient.core.sql.executor.OResultSet;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.IntStream;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings("EmptyTryBlock")
public final class OrientUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(OrientUtils.class);

    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    public static void setOrRemoveProperty(OElement element, String name, Optional<?> value, OType type) {
        setOrRemoveProperty(element, name, value, Optional::isPresent, Optional::get, type);
    }

    public static <T> void setOrRemoveProperty(OElement element, String name, T value, Function<T, Boolean> setCondition, OType type) {
        setOrRemoveProperty(element, name, value, setCondition, t -> t, type);
    }

    public static <T> void setOrRemoveProperty(OElement element, String name, T value, Function<T, Boolean> setCondition, Function<T, Object> extractValueFunction, OType type) {
        if (setCondition.apply(value)) {
            element.setProperty(name, extractValueFunction.apply(value), type);
        } else {
            element.removeProperty(name);
        }
    }

    public static void setOrRemoveProperty(OElement element, String name, String value, OType type) {
        setOrRemoveProperty(element, name, value, StringUtils::isNotBlank, type);
    }

    public static void setOrRemoveProperty(OElement element, String name, Object value, OType type) {
        setOrRemoveProperty(element, name, value, Objects::nonNull, type);
    }

    public static void setOnlyOnceProperty(OElement element, String name, Object value, OType type) {
        if (element.getProperty(name) == null) {
            element.setProperty(name, value, type);
        }
    }

    public static Long resultSetToCount(OResultSet resultSet) {
        if (resultSet.hasNext()) {
            OResult result = resultSet.next();
            if (result.hasProperty("count")) {
                return (Long) result.getProperty("count");
            }
        }
        throw new IllegalArgumentException("OResultSet has no count property");
    }

    public static String addPaginationParameters(String query) {
        return query + " SKIP ? LIMIT ?";
    }

    public static OClass createClass(String className, String superClassName, int clusterNb, ODatabaseSession dbSession) {
        if (clusterNb > 0) {
            if (superClassName != null) {
                try (OResultSet ignored = dbSession.command("CREATE CLASS " + className + " EXTENDS " + superClassName + " CLUSTERS " + clusterNb)) {
                }
            } else {
                try (OResultSet ignored = dbSession.command("CREATE CLASS " + className + " CLUSTERS " + clusterNb)) {
                }
            }
            return dbSession.getClass(className);
        } else {
            if (superClassName != null) {
                return dbSession.createClass(className, superClassName);
            } else {
                return dbSession.createClass(className);
            }
        }
    }

    public static void dropClassWithData(String className, ODatabaseSession dbSession) {
        OClass oClass = dbSession.getClass(className);

        if (oClass != null) {
            oClass.getIndexes()
                .forEach(index -> OrientUtils.dropdIndex(index.getName(), dbSession));

            IntStream.of(oClass.getClusterIds())
                .filter(clusterId -> oClass.getDefaultClusterId() != clusterId)
                .forEach(clusterId -> {
                    oClass.removeClusterId(clusterId);
                    dbSession.dropCluster(clusterId, false);
                });

            OrientUtils.dropClass(className, dbSession);
        } else {
            LOGGER.warn("Cannot find {} class : skip dropping ", className);
        }
    }

    public static OClass createClass(String className, int clusterNb, ODatabaseSession dbSession) {
        return createClass(className, null, clusterNb, dbSession);
    }

    static void rebuildIndex(String indexName, ODatabaseSession dbSession) {
        try (OResultSet rs = dbSession.command("REBUILD INDEX " + indexName)) {
            if (rs.hasNext()) {
                OResult result = rs.next();
                if (result.hasProperty("totalIndexed")) {
                    LOGGER.debug("Rebuild index : {} indexed {} documents", indexName, result.getProperty("totalIndexed"));
                }
            }
        }
    }

    public static void close(ODatabaseSession dbSession) {
        if (dbSession != null) dbSession.close();
    }

    public static void rollback(ODatabaseSession dbSession) {
        if (dbSession != null) dbSession.rollback();
    }

    public static Optional<OElement> load(final String recordId, final ODatabaseSession dbSession) {
        if (!recordId.isEmpty() && ORecordId.isA(recordId)) {
            try {
                return Optional.ofNullable(dbSession.load(
                    new ORecordId(recordId))
                );
            } catch (ORecordNotFoundException e) {
                return empty();
            }
        }
        return empty();
    }

    public static void reloadIfDirty(OElement element) {
        if (element.isDirty()) {
            element.reload();
        }
    }

    public static void deleteVertex(final String recordId, ODatabaseSession dbSession) {
        if (!recordId.isEmpty() && ORecordId.isA(recordId)) {
            try (OResultSet ignored = dbSession.command("DELETE VERTEX " + recordId)) {
            }
        } else {
            LOGGER.warn("Cannot delete vertex with wrong id : {}", recordId);
        }
    }

    public static void dropdIndex(String indexName, ODatabaseSession dbSession) {
        try (OResultSet ignored = dbSession.command("DROP INDEX " + indexName + " IF EXISTS")) {
        }
    }

    private static void dropClass(String className, ODatabaseSession dbSession) {
        try (OResultSet ignored = dbSession.command("DROP CLASS " + className)) {
        }
    }
}
