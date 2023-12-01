/*
 * Copyright 2017-2023 Enedis
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
        MongoClientSettings.Builder mongoClientSettings = MongoClientSettings.builder();
        target.keyStore().ifPresent(keystore ->
            mongoClientSettings.applyToSslSettings(builder -> {
              try {
                builder
                  .invalidHostNameAllowed(true)
                  .enabled(true)
                  .context(SecurityUtils.buildSslContext(target).build());
              } catch (GeneralSecurityException e) {
                throw new IllegalArgumentException(e.getMessage(), e);
              }
            })
        );
        mongoClientSettings.applyConnectionString(new ConnectionString(connectionString));
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
