package com.chutneytesting.design.infra.storage.scenario.compose.orient.changelog;

import static com.chutneytesting.design.infra.storage.scenario.compose.orient.OrientUtils.resultSetToCount;

import com.orientechnologies.orient.core.db.ODatabasePool;
import com.orientechnologies.orient.core.db.ODatabaseSession;
import com.orientechnologies.orient.core.metadata.schema.OClass;
import com.orientechnologies.orient.core.sql.executor.OResultSet;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Comparator;
import java.util.stream.Stream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class OrientChangelogExecutor {

    private static final Logger LOGGER = LoggerFactory.getLogger(OrientChangelogExecutor.class);

    public static final String DBCHANGELOG_CLASS = "DBChangeLog";

    public void updateWithChangelog(ODatabasePool dbPool) {
        initChangeLogClass(dbPool);
        try {
            filterFStepSchemaScripts().forEach(method -> {
                try (ODatabaseSession dbSession = dbPool.acquire()) {
                    executeChangelog(method, dbSession);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            });
        } catch (Exception e) {
            LOGGER.error("Orient database update failed");
            throw new RuntimeException("Orient database update failed", e);
        }

        LOGGER.info("Orient database update finished");
    }

    public static Stream<Method> filterFStepSchemaScripts() {
        Method[] methods = OrientChangelog.class.getMethods();
        return Arrays.stream(methods)
            .filter(m -> m.getDeclaredAnnotation(ChangelogOrder.class) != null)
            .sorted(Comparator.comparing(value -> value.getDeclaredAnnotation(ChangelogOrder.class).order()));
    }

    private void initChangeLogClass(ODatabasePool dbPool) {
        try (ODatabaseSession dbSession = dbPool.acquire()) {
            OClass changeLogClass = dbSession.getClass(DBCHANGELOG_CLASS);
            if (changeLogClass == null) {
                dbSession.command("CREATE CLASS " + DBCHANGELOG_CLASS + " CLUSTERS 1");
                LOGGER.debug("Creation of class : {}", DBCHANGELOG_CLASS);
            }
        }
    }

    private void executeChangelog(Method method, ODatabaseSession dbSession) throws Exception {
        String changelogId = method.getDeclaredAnnotation(ChangelogOrder.class).uuid();
        if (shouldExecuteChangelog(dbSession, changelogId)) {
            method.invoke(null, dbSession);
            insertChangeLog(dbSession, changelogId);
        }
    }

    private boolean shouldExecuteChangelog(ODatabaseSession dbSession, String name) {
        return resultSetToCount(dbSession.query("SELECT COUNT(*) as count FROM " + DBCHANGELOG_CLASS + " WHERE name = ?", name)) == 0;
    }

    @SuppressWarnings("EmptyTryBlock")
    private void insertChangeLog(ODatabaseSession dbSession, String name) {
        try (OResultSet ignored = dbSession.command("INSERT INTO " + DBCHANGELOG_CLASS + " (name, executed) VALUES ('" + name + "', sysdate().format('yyyyMMddHHmmssSSS')" + ")")) {
        }
    }
}
