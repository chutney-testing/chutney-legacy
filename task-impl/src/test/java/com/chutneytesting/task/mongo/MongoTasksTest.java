package com.chutneytesting.task.mongo;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.chutneytesting.task.TestLogger;
import com.chutneytesting.task.TestTarget;
import com.chutneytesting.task.spi.Task;
import com.chutneytesting.task.spi.TaskExecutionResult;
import com.chutneytesting.task.spi.TaskExecutionResult.Status;
import com.chutneytesting.task.spi.injectable.Target;
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

public class MongoTasksTest {

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
        Task insertTask = mockDatabase(new MongoInsertTask(mongoTarget, logger, "lolilol", "{name: 'test1', qty: 3}"), database);

        assertThat(insertTask.execute().status).isEqualTo(Status.Success);
        assertThat(logger.info).containsOnly("Inserted in Mongo collection 'lolilol':\n" +
            "\t{name: 'test1', qty: 3}");
    }

    @Test
    public void updateDocument() {
        UpdateResult deleteResult = mock(UpdateResult.class);
        when(deleteResult.wasAcknowledged()).thenReturn(true);
        when(deleteResult.getModifiedCount()).thenReturn(1L);
        when(database.getCollection(any()).updateMany(any(BsonDocument.class), any(BsonDocument.class))).thenReturn(deleteResult);

        Task updateTask = mockDatabase(new MongoUpdateTask(mongoTarget, logger, "lolilol", "{name: 'test1'}", "{ $set: {qty: 6}}", null), database);
        TaskExecutionResult updateTaskResult = updateTask.execute();
        assertThat(updateTaskResult.status).as("Logger errors: " + logger.errors).isEqualTo(Status.Success);
        assertThat(updateTaskResult.outputs.get("modifiedCount")).isEqualTo(1L);
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

        Task findTask = mockDatabase(new MongoFindTask(mongoTarget, logger, "lolilol", "{ qty: { $gt: 4 } }", null), database);

        TaskExecutionResult findTaskResult = findTask.execute();
        assertThat(findTaskResult.status).isEqualTo(Status.Success);
        @SuppressWarnings("unchecked")
        String insertedDocument = ((Iterable<String>) findTaskResult.outputs.get("documents")).iterator().next();
        assertThat(insertedDocument).isEqualTo("{fake: truc}");
        assertThat(logger.info).containsOnly("Found 1 document(s)");
    }

    @Test
    public void deleteDocument() {
        DeleteResult deleteResult = mock(DeleteResult.class);
        when(deleteResult.wasAcknowledged()).thenReturn(true);
        when(deleteResult.getDeletedCount()).thenReturn(1L);
        when(database.getCollection(any()).deleteMany(any(BsonDocument.class))).thenReturn(deleteResult);
        Task deleteTask = mockDatabase(new MongoDeleteTask(mongoTarget, logger, "lolilol", "{ name: { $eq: 'test1' } }"), database);

        TaskExecutionResult deleteTaskResult = deleteTask.execute();
        assertThat(deleteTaskResult.status).isEqualTo(Status.Success);
        assertThat(deleteTaskResult.outputs.get("deletedCount")).isEqualTo(1L);
        assertThat(logger.info).containsOnly("Deleted 1 document(s)");
    }

    @Test
    public void countDocuments() {
        when(database.getCollection(any()).countDocuments(any(BsonDocument.class))).thenReturn(1L);
        Task countTask = mockDatabase(new MongoCountTask(mongoTarget, logger, "lolilol", "{ }"), database);
        TaskExecutionResult countTaskResult = countTask.execute();

        assertThat(countTaskResult.status).isEqualTo(Status.Success);
        assertThat(countTaskResult.outputs.get("count")).isEqualTo(1L);
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
        Task listTask = mockDatabase(new MongoListTask(mongoTarget, logger), database);
        TaskExecutionResult listTaskResult = listTask.execute();

        assertThat(listTaskResult.status).isEqualTo(Status.Success);
        assertThat((List<String>) listTaskResult.outputs.get("collectionNames")).containsExactlyInAnyOrder("lolilol");
        assertThat(logger.info).containsOnly("Found 1 collection(s)");
    }

    private <T extends Task> T mockDatabase(T task, MongoDatabase database) {
        MongoDatabaseFactory mongoDatabaseFactory = t -> CloseableResource.build(database, () -> {
        });
        ReflectionTestUtils.setField(task, "mongoDatabaseFactory", mongoDatabaseFactory);
        return task;
    }
}
