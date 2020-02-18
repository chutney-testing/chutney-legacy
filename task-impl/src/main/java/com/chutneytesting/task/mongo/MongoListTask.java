package com.chutneytesting.task.mongo;

import com.mongodb.client.MongoDatabase;
import com.mongodb.client.MongoIterable;
import com.chutneytesting.task.spi.Task;
import com.chutneytesting.task.spi.TaskExecutionResult;
import com.chutneytesting.task.spi.injectable.Logger;
import com.chutneytesting.task.spi.injectable.Target;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import com.chutneytesting.tools.CloseableResource;

public class MongoListTask implements Task {

    private final MongoDatabaseFactory mongoDatabaseFactory = new DefaultMongoDatabaseFactory();
    private final Target target;
    private final Logger logger;

    public MongoListTask(Target target, Logger logger) {
        this.target = target;
        this.logger = logger;
    }

    @Override
    public TaskExecutionResult execute() {
        try (CloseableResource<MongoDatabase> database = mongoDatabaseFactory.create(target)) {
            MongoIterable<String> collectionNames = database.getResource().listCollectionNames();
            List<String> collectionNameList = new ArrayList<>();
            collectionNames.iterator().forEachRemaining(collectionNameList::add);
            logger.info("Found " + collectionNameList.size() + " collection(s)");
            return TaskExecutionResult.ok(Collections.singletonMap("collectionNames", collectionNameList));
        } catch (IllegalArgumentException e) {
            logger.error(e.getMessage());
            return TaskExecutionResult.ko();
        }
    }
}
