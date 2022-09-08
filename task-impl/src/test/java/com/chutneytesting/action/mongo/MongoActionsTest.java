package com.chutneytesting.action.mongo;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.chutneytesting.action.TestLogger;
import com.chutneytesting.action.TestTarget;
import com.chutneytesting.action.spi.Action;
import com.chutneytesting.action.spi.ActionExecutionResult;
import com.chutneytesting.action.spi.ActionExecutionResult.Status;
import com.chutneytesting.action.spi.injectable.Target;
import com.chutneytesting.tools.CloseableResource;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.UpdateResult;
import java.util.List;
import java.util.function.Consumer;
import org.bson.BsonDocument;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.mockito.stubbing.OngoingStubbing;
import org.springframework.test.util.ReflectionTestUtils;

public class MongoActionsTest {

    private final Target mongoTarget = TestTarget.TestTargetBuilder.builder()
        .withTargetId("mongo")
        .withUrl("mongodb://host1:27017")
        .withProperty("user", "user")
        .withProperty("password", "pass")
        .withProperty("databaseName", "lol")
        .build();

    private final TestLogger logger = new TestLogger();

    private final MongoDatabase database = Mockito.mock(MongoDatabase.class, Mockito.RETURNS_DEEP_STUBS);

    @Test
    public void insertDocument() {
        Action insertAction = mockDatabase(new MongoInsertAction(mongoTarget, logger, "lolilol", "{name: 'test1', qty: 3}"), database);

        assertThat(insertAction.execute().status).isEqualTo(Status.Success);
        assertThat(logger.info).containsOnly("Inserted in Mongo collection 'lolilol':\n" +
            "\t{name: 'test1', qty: 3}");
    }

    @Test
    public void updateDocument() {
        UpdateResult deleteResult = mock(UpdateResult.class);
        when(deleteResult.wasAcknowledged()).thenReturn(true);
        when(deleteResult.getModifiedCount()).thenReturn(1L);
        when(database.getCollection(any()).updateMany(any(BsonDocument.class), any(BsonDocument.class))).thenReturn(deleteResult);

        Action updateAction = mockDatabase(new MongoUpdateAction(mongoTarget, logger, "lolilol", "{name: 'test1'}", "{ $set: {qty: 6}}", null), database);
        ActionExecutionResult updateActionResult = updateAction.execute();
        assertThat(updateActionResult.status).as("Logger errors: " + logger.errors).isEqualTo(Status.Success);
        assertThat(updateActionResult.outputs.get("modifiedCount")).isEqualTo(1L);
        assertThat(logger.info).containsOnly("Modified in Mongo collection 'lolilol': 1 documents");
    }

    @Test
    public void findDocument() {
        MongoCursor<Object> iterable = mock(MongoCursor.class);
        OngoingStubbing<MongoCursor<Object>> resultStubbing = Mockito.when(database.getCollection(any())
            .find(any(BsonDocument.class))
            .limit(anyInt())
            .map(any())
            .iterator());
        resultStubbing.thenReturn(iterable);
        doAnswer(iom -> {
            Consumer<String> consumer = iom.getArgument(0);
            consumer.accept("{fake: truc}");
            return null;
        }).when(iterable).forEachRemaining(any());

        Action findAction = mockDatabase(new MongoFindAction(mongoTarget, logger, "lolilol", "{ qty: { $gt: 4 } }", null), database);

        ActionExecutionResult findActionResult = findAction.execute();
        assertThat(findActionResult.status).isEqualTo(Status.Success);
        @SuppressWarnings("unchecked")
        String insertedDocument = ((Iterable<String>) findActionResult.outputs.get("documents")).iterator().next();
        assertThat(insertedDocument).isEqualTo("{fake: truc}");
        assertThat(logger.info).containsOnly("Found 1 document(s)");
    }

    @Test
    public void deleteDocument() {
        DeleteResult deleteResult = mock(DeleteResult.class);
        when(deleteResult.wasAcknowledged()).thenReturn(true);
        when(deleteResult.getDeletedCount()).thenReturn(1L);
        when(database.getCollection(any()).deleteMany(any(BsonDocument.class))).thenReturn(deleteResult);
        Action deleteAction = mockDatabase(new MongoDeleteAction(mongoTarget, logger, "lolilol", "{ name: { $eq: 'test1' } }"), database);

        ActionExecutionResult deleteActionResult = deleteAction.execute();
        assertThat(deleteActionResult.status).isEqualTo(Status.Success);
        assertThat(deleteActionResult.outputs.get("deletedCount")).isEqualTo(1L);
        assertThat(logger.info).containsOnly("Deleted 1 document(s)");
    }

    @Test
    public void countDocuments() {
        when(database.getCollection(any()).countDocuments(any(BsonDocument.class))).thenReturn(1L);
        Action countAction = mockDatabase(new MongoCountAction(mongoTarget, logger, "lolilol", "{ }"), database);
        ActionExecutionResult countActionResult = countAction.execute();

        assertThat(countActionResult.status).isEqualTo(Status.Success);
        assertThat(countActionResult.outputs.get("count")).isEqualTo(1L);
        assertThat(logger.info).containsOnly("Found 1 objects matching query:\n" +
            "\t{ }");
    }

    @Test
    @SuppressWarnings("unchecked")
    public void listCollections() {
        MongoCursor<String> iterable = mock(MongoCursor.class);
        when(database.listCollectionNames().iterator()).thenReturn(iterable);
        doAnswer(iom -> {
            Consumer<String> consumer = iom.getArgument(0);
            consumer.accept("lolilol");
            return null;
        }).when(iterable).forEachRemaining(any());
        Action listAction = mockDatabase(new MongoListAction(mongoTarget, logger), database);
        ActionExecutionResult listActionResult = listAction.execute();

        assertThat(listActionResult.status).isEqualTo(Status.Success);
        assertThat((List<String>) listActionResult.outputs.get("collectionNames")).containsExactlyInAnyOrder("lolilol");
        assertThat(logger.info).containsOnly("Found 1 collection(s)");
    }

    private <T extends Action> T mockDatabase(T action, MongoDatabase database) {
        MongoDatabaseFactory mongoDatabaseFactory = t -> CloseableResource.build(database, () -> {
        });
        ReflectionTestUtils.setField(action, "mongoDatabaseFactory", mongoDatabaseFactory);
        return action;
    }
}
