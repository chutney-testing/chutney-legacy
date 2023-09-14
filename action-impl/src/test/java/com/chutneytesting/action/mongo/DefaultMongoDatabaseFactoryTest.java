package com.chutneytesting.action.mongo;

import com.chutneytesting.action.TestLogger;
import com.chutneytesting.action.TestTarget;
import com.chutneytesting.action.spi.ActionExecutionResult;
import com.chutneytesting.action.spi.injectable.Target;
import org.assertj.core.api.InstanceOfAssertFactories;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.utility.DockerImageName;
import org.testcontainers.utility.MountableFile;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class DefaultMongoDatabaseFactoryTest {

    private static final TestLogger logger = new TestLogger();
    private static final String TRUSTSTORE_JKS = DefaultMongoDatabaseFactoryTest.class.getResource("/security/truststore.jks").getPath();
    private static final String KEYSTORE_JKS = DefaultMongoDatabaseFactoryTest.class.getResource("/security/server.jks").getPath();

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
        assertThatThrownBy(() -> action.execute());
    }
}
