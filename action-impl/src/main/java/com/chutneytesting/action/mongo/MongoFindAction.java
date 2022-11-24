package com.chutneytesting.action.mongo;

import static com.chutneytesting.action.mongo.MongoActionValidatorsUtils.mongoTargetValidation;
import static com.chutneytesting.action.spi.validation.ActionValidatorsUtils.notBlankStringValidation;
import static com.chutneytesting.action.spi.validation.Validator.getErrorsFrom;
import static java.util.Optional.ofNullable;

import com.chutneytesting.action.spi.Action;
import com.chutneytesting.action.spi.ActionExecutionResult;
import com.chutneytesting.action.spi.injectable.Input;
import com.chutneytesting.action.spi.injectable.Logger;
import com.chutneytesting.action.spi.injectable.Target;
import com.chutneytesting.tools.CloseableResource;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.MongoIterable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.bson.BsonDocument;
import org.bson.Document;

public class MongoFindAction implements Action {

    private final MongoDatabaseFactory mongoDatabaseFactory = new DefaultMongoDatabaseFactory();
    private final Target target;
    private final Logger logger;
    private final String collection;
    private final String query;
    private final Integer limit;

    public MongoFindAction(Target target,
                         Logger logger,
                         @Input("collection") String collection,
                         @Input("query") String query,
                         @Input("limit") Integer limit
    ) {
        this.target = target;
        this.logger = logger;
        this.collection = collection;
        this.query = query;
        this.limit = ofNullable(limit).orElse(20);
    }

    @Override
    public List<String> validateInputs() {
        return getErrorsFrom(
            notBlankStringValidation(collection, "collection"),
            notBlankStringValidation(query, "query"),
            mongoTargetValidation(target)
        );
    }

    @Override
    public ActionExecutionResult execute() {
        try (CloseableResource<MongoDatabase> database = mongoDatabaseFactory.create(target)) {
            MongoIterable<String> documents = database.getResource()
                .getCollection(collection)
                .find(BsonDocument.parse(query))
                .limit(limit)
                .map(Document::toJson);

            List<String> documentList = new ArrayList<>();
            documents.iterator().forEachRemaining(documentList::add);
            logger.info("Found " + documentList.size() + " document(s)");
            return ActionExecutionResult.ok(Collections.singletonMap("documents", documentList));
        } catch (IllegalArgumentException e) {
            logger.error(e.getMessage());
            return ActionExecutionResult.ko();
        }
    }
}
