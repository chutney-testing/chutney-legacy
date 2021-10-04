package com.chutneytesting.task.mongo;

import static com.chutneytesting.task.TaskValidatorsUtils.targetValidation;
import static com.chutneytesting.task.mongo.MongoTaskValidatorsUtils.mongoTargetValidation;
import static com.chutneytesting.task.spi.validation.Validator.getErrorsFrom;

import com.chutneytesting.task.spi.Task;
import com.chutneytesting.task.spi.TaskExecutionResult;
import com.chutneytesting.task.spi.injectable.Logger;
import com.chutneytesting.task.spi.injectable.Target;
import com.chutneytesting.tools.CloseableResource;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.MongoIterable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MongoListTask implements Task {

    private final MongoDatabaseFactory mongoDatabaseFactory = new DefaultMongoDatabaseFactory();
    private final Target target;
    private final Logger logger;

    public MongoListTask(Target target, Logger logger) {
        this.target = target;
        this.logger = logger;
    }

    @Override
    public List<String> validateInputs() {
        return getErrorsFrom(
            targetValidation(target),
            mongoTargetValidation(target)
        );
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
