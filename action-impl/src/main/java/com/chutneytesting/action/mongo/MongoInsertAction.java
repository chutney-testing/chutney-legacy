package com.chutneytesting.action.mongo;

import static com.chutneytesting.action.mongo.MongoActionValidatorsUtils.mongoTargetValidation;
import static com.chutneytesting.action.spi.validation.ActionValidatorsUtils.notBlankStringValidation;
import static com.chutneytesting.action.spi.validation.Validator.getErrorsFrom;

import com.chutneytesting.action.spi.Action;
import com.chutneytesting.action.spi.ActionExecutionResult;
import com.chutneytesting.action.spi.injectable.Input;
import com.chutneytesting.action.spi.injectable.Logger;
import com.chutneytesting.action.spi.injectable.Target;
import com.chutneytesting.tools.CloseableResource;
import com.google.common.base.Ascii;
import com.mongodb.client.MongoDatabase;
import java.util.List;
import org.bson.Document;

public class MongoInsertAction implements Action {

    private final MongoDatabaseFactory mongoDatabaseFactory = new DefaultMongoDatabaseFactory();
    private final Target target;
    private final Logger logger;
    private final String collection;
    private final String document;

    public MongoInsertAction(Target target,
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
    public ActionExecutionResult execute() {
        try (CloseableResource<MongoDatabase> database = mongoDatabaseFactory.create(target)) {
            database.getResource().getCollection(collection).insertOne(Document.parse(document));
            logger.info(
                "Inserted in Mongo collection '" + collection + "':\n\t" +
                    Ascii.truncate(document.replace("\n", "\n\t"), 50, "...")
            );
            return ActionExecutionResult.ok();
        } catch (IllegalArgumentException e) {
            logger.error(e.getMessage());
            return ActionExecutionResult.ko();
        }
    }
}
