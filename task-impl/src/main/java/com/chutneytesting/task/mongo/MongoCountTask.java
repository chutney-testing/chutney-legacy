package com.chutneytesting.task.mongo;

import static com.chutneytesting.task.TaskValidatorsUtils.stringValidation;
import static com.chutneytesting.task.TaskValidatorsUtils.targetValidation;
import static com.chutneytesting.task.mongo.MongoTaskValidatorsUtils.mongoTargetValidation;
import static com.chutneytesting.task.spi.validation.Validator.getErrorsFrom;

import com.chutneytesting.task.spi.Task;
import com.chutneytesting.task.spi.TaskExecutionResult;
import com.chutneytesting.task.spi.injectable.Input;
import com.chutneytesting.task.spi.injectable.Logger;
import com.chutneytesting.task.spi.injectable.Target;
import com.chutneytesting.tools.CloseableResource;
import com.mongodb.client.MongoDatabase;
import java.util.Collections;
import java.util.List;
import org.bson.BsonDocument;

public class MongoCountTask implements Task {

    private final MongoDatabaseFactory mongoDatabaseFactory = new DefaultMongoDatabaseFactory();
    private final Target target;
    private final Logger logger;
    private final String collection;
    private final String query;

    public MongoCountTask(Target target,
                          Logger logger,
                          @Input("collection") String collection,
                          @Input("query") String query) {
        this.target = target;
        this.logger = logger;
        this.collection = collection;
        this.query = query;
    }

    @Override
    public List<String> validateInputs() {
        return getErrorsFrom(
            targetValidation(target),
            stringValidation(collection, "collection"),
            stringValidation(query, "query"),
            mongoTargetValidation(target)
        );
    }

    @Override
    public TaskExecutionResult execute() {
        try (CloseableResource<MongoDatabase> database = mongoDatabaseFactory.create(target)) {
            final long count = database.getResource().getCollection(collection).countDocuments(BsonDocument.parse(query));
            logger.info("Found " + count + " objects matching query:\n\t" + query.replace("\n", "\n\t"));
            return TaskExecutionResult.ok(Collections.singletonMap("count", count));
        } catch (IllegalArgumentException e) {
            logger.error(e.getMessage());
            return TaskExecutionResult.ko();
        }
    }
}
