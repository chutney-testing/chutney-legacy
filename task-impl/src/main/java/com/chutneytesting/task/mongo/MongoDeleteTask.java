package com.chutneytesting.task.mongo;

import com.mongodb.client.MongoDatabase;
import com.mongodb.client.result.DeleteResult;
import com.chutneytesting.task.spi.Task;
import com.chutneytesting.task.spi.TaskExecutionResult;
import com.chutneytesting.task.spi.injectable.Input;
import com.chutneytesting.task.spi.injectable.Logger;
import com.chutneytesting.task.spi.injectable.Target;
import java.util.Collections;
import org.bson.BsonDocument;
import com.chutneytesting.tools.CloseableResource;

public class MongoDeleteTask implements Task {

    private final MongoDatabaseFactory mongoDatabaseFactory = new DefaultMongoDatabaseFactory();
    private final Target target;
    private final Logger logger;
    private final String collection;
    private final String query;

    public MongoDeleteTask(Target target,
                           Logger logger,
                           @Input("collection") String collection,
                           @Input("query") String query) {
        this.target = target;
        this.logger = logger;
        this.collection = collection;
        this.query = query;
    }

    @Override
    public TaskExecutionResult execute() {
        try (CloseableResource<MongoDatabase> database = mongoDatabaseFactory.create(target)) {
            DeleteResult deleteResult = database.getResource().getCollection(collection).deleteMany(BsonDocument.parse(query));
            if (!deleteResult.wasAcknowledged()) {
                logger.error("Deletion was not acknowledged");
                return TaskExecutionResult.ko();
            }
            long deletedCount = deleteResult.getDeletedCount();
            logger.info("Deleted " + deletedCount + " document(s)");
            return TaskExecutionResult.ok(Collections.singletonMap("deletedCount", deletedCount));
        } catch (IllegalArgumentException e) {
            logger.error(e.getMessage());
            return TaskExecutionResult.ko();
        }
    }
}
