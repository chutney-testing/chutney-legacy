package com.chutneytesting.task.mongo;

import com.chutneytesting.task.spi.injectable.Target;
import com.chutneytesting.tools.CloseableResource;
import com.mongodb.client.MongoDatabase;

public interface MongoDatabaseFactory {

    /**
     * @throws IllegalArgumentException when given {@link Target} does not supply needed parameters
     */
    CloseableResource<MongoDatabase> create(Target target) throws IllegalArgumentException;
}
