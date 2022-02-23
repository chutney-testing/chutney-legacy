package com.chutneytesting.task.mongo;

import com.chutneytesting.task.spi.injectable.Target;
import com.chutneytesting.tools.CloseableResource;
import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;
import org.springframework.util.StringUtils;

public class DefaultMongoDatabaseFactory implements MongoDatabaseFactory {

    public CloseableResource<MongoDatabase> create(Target target) throws IllegalArgumentException {
        String databaseName = target.properties().get("databaseName");
        if (StringUtils.isEmpty(databaseName)) {
            throw new IllegalArgumentException("Missing Target property 'databaseName'");
        }

        ServerAddress serverAddress = new ServerAddress(target.host(), target.port());
        String conns = String.format("mongodb://%s:%d/?replicaSet=rs0", target.host(), target.port());

        final MongoClient mongoClient;
        MongoClientSettings.Builder mongoClientSettings = MongoClientSettings.builder()
            .applyConnectionString(new ConnectionString(conns));

        if (target.security().credential().isPresent()) {
            MongoCredential credential = MongoCredential.createCredential(target.security().credential().get().username(), databaseName, target.security().credential().get().password().toCharArray());
            mongoClientSettings
                .credential(credential);
        }
        mongoClient = MongoClients.create(mongoClientSettings.build());
        return CloseableResource.build(mongoClient.getDatabase(databaseName), mongoClient::close);
    }

}
