package com.chutneytesting.action.mongo;

import com.chutneytesting.action.common.SecurityUtils;
import com.chutneytesting.action.spi.injectable.Target;
import com.chutneytesting.tools.CloseableResource;
import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.MongoCredential;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;
import java.security.GeneralSecurityException;
import org.apache.commons.lang3.StringUtils;

public class DefaultMongoDatabaseFactory implements MongoDatabaseFactory {

    public CloseableResource<MongoDatabase> create(Target target) throws IllegalArgumentException {
        String databaseName = target.property("databaseName").orElse("");
        if (StringUtils.isEmpty(databaseName)) {
            throw new IllegalArgumentException("Missing Target property 'databaseName'");
        }

        String connectionString = String.format("mongodb://%s:%d/", target.host(), target.port());

        final MongoClient mongoClient;
        MongoClientSettings.Builder mongoClientSettings = MongoClientSettings.builder().applyConnectionString(new ConnectionString(connectionString));
        target.keyStore().ifPresent(trustStore -> {
            target.keyStorePassword().orElseThrow(IllegalArgumentException::new);
            mongoClientSettings.applyToSslSettings(builder -> {
              try {
                builder
                  .invalidHostNameAllowed(true)
                  .enabled(true)
                  .context(SecurityUtils.buildSslContext(target).build())
                  .applyConnectionString(new ConnectionString(connectionString));
              } catch (GeneralSecurityException e) {
                throw new IllegalArgumentException(e.getMessage(), e);
              }
            });
        });
        if (target.user().isPresent()) {
            String user = target.user().get();
            String password = target.userPassword().orElse("");
            mongoClientSettings.credential(
                MongoCredential.createCredential(user, databaseName, password.toCharArray())
            );
        }
        mongoClient = MongoClients.create(mongoClientSettings.build());
        return CloseableResource.build(mongoClient.getDatabase(databaseName), mongoClient::close);
    }

}
