package com.chutneytesting.task.mongo;

import com.mongodb.client.MongoDatabase;
import com.mongodb.client.MongoIterable;
import com.chutneytesting.task.spi.Task;
import com.chutneytesting.task.spi.TaskExecutionResult;
import com.chutneytesting.task.spi.injectable.Input;
import com.chutneytesting.task.spi.injectable.Logger;
import com.chutneytesting.task.spi.injectable.Target;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.bson.BsonDocument;
import org.bson.Document;
import com.chutneytesting.tools.CloseableResource;

public class MongoFindTask implements Task {

    private final MongoDatabaseFactory mongoDatabaseFactory = new DefaultMongoDatabaseFactory();
    private final Target target;
    private final Logger logger;
    private final String collection;
    private final String query;
    private final Integer limit;

    public MongoFindTask(Target target,
                         Logger logger,
                         @Input("collection") String collection,
                         @Input("query") String query,
                         @Input("limit") Integer limit
    ) {
        this.target = target;
        this.logger = logger;
        this.collection = collection;
        this.query = query;
        this.limit = limit;
    }

    @Override
    public TaskExecutionResult execute() {
        try (CloseableResource<MongoDatabase> database = mongoDatabaseFactory.create(target)) {
            MongoIterable<String> documents = database.getResource()
                .getCollection(collection)
                .find(BsonDocument.parse(query))
                .limit(Optional.ofNullable(limit).orElse(20))
                .map(Document::toJson);

            List<String> documentList = new ArrayList<>();
            documents.iterator().forEachRemaining(documentList::add);
            logger.info("Found " + documentList.size() + " document(s)");
            return TaskExecutionResult.ok(Collections.singletonMap("documents", documentList));
        } catch (IllegalArgumentException e) {
            logger.error(e.getMessage());
            return TaskExecutionResult.ko();
        }
    }
}
