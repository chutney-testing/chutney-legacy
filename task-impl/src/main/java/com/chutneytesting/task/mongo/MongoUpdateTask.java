package com.chutneytesting.task.mongo;

import static com.chutneytesting.task.TaskValidatorsUtils.stringValidation;
import static com.chutneytesting.task.TaskValidatorsUtils.targetValidation;
import static com.chutneytesting.task.mongo.MongoTaskValidatorsUtils.mongoTargetValidation;
import static com.chutneytesting.task.spi.validation.Validator.getErrorsFrom;
import static java.util.Collections.emptyList;
import static java.util.Optional.ofNullable;

import com.chutneytesting.task.spi.Task;
import com.chutneytesting.task.spi.TaskExecutionResult;
import com.chutneytesting.task.spi.injectable.Input;
import com.chutneytesting.task.spi.injectable.Logger;
import com.chutneytesting.task.spi.injectable.Target;
import com.chutneytesting.tools.CloseableResource;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.UpdateOptions;
import com.mongodb.client.result.UpdateResult;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import org.bson.BsonDocument;
import org.bson.Document;

public class MongoUpdateTask implements Task {

    private final MongoDatabaseFactory mongoDatabaseFactory = new DefaultMongoDatabaseFactory();
    private final Target target;
    private final Logger logger;
    private final String collection;
    private final String filter;
    private final String update;
    private final List<String> arrayFilters;

    public MongoUpdateTask(Target target,
                           Logger logger,
                           @Input("collection") String collection,
                           @Input("filter") String filter,
                           @Input("update") String update,
                           // See https://jira.mongodb.org/browse/SERVER-831 for usage.
                           // Only since @3.5.12 mongodb version
                           @Input("arrayFilters") List<String> arrayFilters) {
        this.target = target;
        this.logger = logger;
        this.collection = collection;
        this.filter = filter;
        this.update = update;
        this.arrayFilters = ofNullable(arrayFilters).orElse(emptyList());
    }

    @Override
    public List<String> validateInputs() {
        return getErrorsFrom(
            targetValidation(target),
            stringValidation(collection, "collection"),
            stringValidation(update, "update"),
            mongoTargetValidation(target)
        );
    }

    @Override
    public TaskExecutionResult execute() {
        try (CloseableResource<MongoDatabase> database = mongoDatabaseFactory.create(target)) {
            MongoCollection<Document> collection = database
                .getResource()
                .getCollection(this.collection);

            final UpdateResult updateResult;
            if (!arrayFilters.isEmpty()) {
                List<BsonDocument> arrayFilterDocuments = arrayFilters.stream()
                    .map(BsonDocument::parse)
                    .collect(Collectors.toList());
                updateResult = collection
                    .updateMany(
                        BsonDocument.parse(filter),
                        BsonDocument.parse(update),
                        new UpdateOptions().arrayFilters(arrayFilterDocuments)
                    );
            } else {
                updateResult = collection
                    .updateMany(
                        BsonDocument.parse(filter),
                        BsonDocument.parse(update)
                    );
            }
            if (!updateResult.wasAcknowledged()) {
                logger.error("Update was not acknowledged");
                return TaskExecutionResult.ko();
            }
            long modifiedCount = updateResult.getModifiedCount();
            logger.info("Modified in Mongo collection '" + this.collection + "': " + modifiedCount + " documents");
            return TaskExecutionResult.ok(Collections.singletonMap("modifiedCount", modifiedCount));
        } catch (IllegalArgumentException e) {
            logger.error(e.getMessage());
            return TaskExecutionResult.ko();
        }
    }
}
