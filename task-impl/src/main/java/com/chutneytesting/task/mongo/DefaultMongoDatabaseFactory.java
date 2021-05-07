package com.chutneytesting.task.mongo;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientOptions;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;
import com.mongodb.client.MongoDatabase;
import com.chutneytesting.task.spi.injectable.Target;
import java.util.Collections;
import com.chutneytesting.tools.CloseableResource;
import org.springframework.util.StringUtils;

public class DefaultMongoDatabaseFactory implements MongoDatabaseFactory {

    public CloseableResource<MongoDatabase> create(Target target) throws IllegalArgumentException {
        String databaseName = target.properties().get("databaseName");
        if (StringUtils.isEmpty(databaseName)) {
            throw new IllegalArgumentException("Missing Target property 'databaseName'");
        }

        ServerAddress serverAddress = new ServerAddress(target.getUrlAsURI().getHost(), target.getUrlAsURI().getPort());

        final MongoClient mongoClient;
        if (target.security().credential().isPresent()) {
            MongoCredential credential = MongoCredential.createCredential(target.security().credential().get().username(), databaseName, target.security().credential().get().password().toCharArray());
            mongoClient = new MongoClient(serverAddress, credential, MongoClientOptions.builder().build());
        } else {
            mongoClient = new MongoClient(serverAddress);
        }

        return CloseableResource.build(mongoClient.getDatabase(databaseName), mongoClient::close);
    }
}
