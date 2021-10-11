package com.chutneytesting.task.mongo;

import static com.chutneytesting.task.mongo.MongoTaskValidatorsUtils.mongoTargetValidation;
import static com.chutneytesting.task.spi.validation.TaskValidatorsUtils.notBlankStringValidation;
import static com.chutneytesting.task.spi.validation.Validator.getErrorsFrom;

import com.chutneytesting.task.spi.Task;
import com.chutneytesting.task.spi.TaskExecutionResult;
import com.chutneytesting.task.spi.injectable.Input;
import com.chutneytesting.task.spi.injectable.Logger;
import com.chutneytesting.task.spi.injectable.Target;
import com.chutneytesting.tools.CloseableResource;
import com.google.common.base.Ascii;
import com.mongodb.client.MongoDatabase;
import java.util.List;
import org.bson.Document;

public class MongoInsertTask implements Task {

    private final MongoDatabaseFactory mongoDatabaseFactory = new DefaultMongoDatabaseFactory();
    private final Target target;
    private final Logger logger;
    private final String collection;
    private final String document;

    public MongoInsertTask(Target target,
                           Logger logger,
                           @Input("collection") String collection,
                           @Input("document") String document) {
        this.target = target;
        this.logger = logger;
        this.collection = collection;
        this.document = document;
    }

    @Override
    public List<String> validateInputs() {
        return getErrorsFrom(
            notBlankStringValidation(collection, "collection"),
            notBlankStringValidation(document, "document"),
            mongoTargetValidation(target)
        );
    }

    @Override
    public TaskExecutionResult execute() {
        try (CloseableResource<MongoDatabase> database = mongoDatabaseFactory.create(target)) {
            database.getResource().getCollection(collection).insertOne(Document.parse(document));
            logger.info(
                "Inserted in Mongo collection '" + collection + "':\n\t" +
                    Ascii.truncate(document.replace("\n", "\n\t"), 50, "...")
            );
            return TaskExecutionResult.ok();
        } catch (IllegalArgumentException e) {
            logger.error(e.getMessage());
            return TaskExecutionResult.ko();
        }
    }
}
