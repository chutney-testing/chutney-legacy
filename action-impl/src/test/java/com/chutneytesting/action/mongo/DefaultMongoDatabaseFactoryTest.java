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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.chutneytesting.action.TestLogger;
import com.chutneytesting.action.TestTarget;
import com.chutneytesting.action.spi.ActionExecutionResult;
import com.chutneytesting.action.spi.injectable.Target;
import java.net.URISyntaxException;
import java.nio.file.Paths;
import org.assertj.core.api.InstanceOfAssertFactories;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.utility.DockerImageName;
import org.testcontainers.utility.MountableFile;

public class DefaultMongoDatabaseFactoryTest {

    private static final TestLogger logger = new TestLogger();
    private static final String TRUSTSTORE_JKS;
    private static final String KEYSTORE_JKS;

    static {
        try {
            TRUSTSTORE_JKS = Paths.get(DefaultMongoDatabaseFactoryTest.class.getResource("/security/truststore.jks").toURI()).toString();
            KEYSTORE_JKS = Paths.get(DefaultMongoDatabaseFactoryTest.class.getResource("/security/server.jks").toURI()).toString();
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void should_accept_ssl_connection_when_truststore_in_target_properties() {
        GenericContainer mongoContainer = new GenericContainer(DockerImageName.parse("mongo:4.4.24"))
            .withExposedPorts(27017)
            .withCommand("--config /etc/mongod.conf")
            .withCopyFileToContainer(MountableFile.forClasspathResource("/security/mongod.conf"), "/etc/mongod.conf")
            .withCopyFileToContainer(MountableFile.forClasspathResource("/security/server.pem"), "/etc/ssl/server.pem");
        mongoContainer.start();


        final Target mongoTarget = TestTarget.TestTargetBuilder.builder()
            .withTargetId("mongo")
            .withUrl("mongodb://" + mongoContainer.getHost() + ":" + mongoContainer.getMappedPort(27017))
            .withProperty("databaseName", "local")
            .withProperty("keyStore", KEYSTORE_JKS)
            .withProperty("trustStore", TRUSTSTORE_JKS)
            .withProperty("trustStorePassword", "truststore")
            .withProperty("keyStorePassword", "server")
            .build();

        MongoListAction action = new MongoListAction(mongoTarget, logger);
        ActionExecutionResult result = action.execute();


        assertThat(result.outputs)
            .extractingByKey("collectionNames")
            .asInstanceOf(InstanceOfAssertFactories.list(String.class))
            .isNotEmpty();
    }

    @Test
    public void should_reject_ssl_connection_when_no_truststore_in_target() {

        GenericContainer mongoContainer = new GenericContainer(DockerImageName.parse("mongo:4.4.24"))
            .withExposedPorts(27017)
            .withCommand("--config /etc/mongod.conf")
            .withCopyFileToContainer(MountableFile.forClasspathResource("/security/mongod.conf"), "/etc/mongod.conf")
            .withCopyFileToContainer(MountableFile.forClasspathResource("/security/server.pem"), "/etc/ssl/server.pem");
        mongoContainer.start();
        final Target mongoTarget = TestTarget.TestTargetBuilder.builder()
            .withTargetId("mongo")
            .withUrl("mongodb://" + mongoContainer.getHost() + ":" + mongoContainer.getFirstMappedPort())
            .withProperty("databaseName", "local")
            .build();

        MongoListAction action = new MongoListAction(mongoTarget, logger);
        assertThatThrownBy(action::execute);
    }
}
