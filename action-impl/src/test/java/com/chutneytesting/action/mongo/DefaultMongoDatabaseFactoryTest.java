package com.chutneytesting.action.mongo;

import com.chutneytesting.action.TestTarget;
import com.chutneytesting.action.spi.injectable.Target;
import com.chutneytesting.tools.CloseableResource;
import com.mongodb.client.MongoDatabase;
import org.junit.Test;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;
import org.testcontainers.utility.MountableFile;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

@Testcontainers
public class DefaultMongoDatabaseFactoryTest {

    @Container
  final MongoDBContainer mongoDBContainer = new MongoDBContainer(DockerImageName.parse("mongo:4.4.0"))
      .withExposedPorts(27017)
      .withEnv("MONGO_INITDB_DATABASE", "toto")
      .withEnv("MONGO_INITDB_USERNAME", "toto")
      .withEnv("MONGO_INITDB_PASSWORD", "totoTutu")
      .withEnv("MONGO_INITDB_SSL_MODE", "requireSSL")
      .withEnv("MONGO_INITDB_SSL_CA_FILE", "/etc/ssl/server.jks")
      .withCopyFileToContainer(MountableFile.forClasspathResource("/security/server.jks"), "/etc/ssl/server.jks");
  private final DefaultMongoDatabaseFactory mongoDatabaseFactory = new DefaultMongoDatabaseFactory();

  private static final String KEYSTORE_JKS = DefaultMongoDatabaseFactoryTest.class.getResource("/security/server.jks").getPath();

    @Test
  public void should_enable_ssl_connection_according_to_target_properties() {

    // Given
      mongoDBContainer.start();
      Target target =  TestTarget.TestTargetBuilder
          .builder()
          .withTargetId("target name")
          .withUrl(mongoDBContainer.getConnectionString())
          .withProperty("trustStore", KEYSTORE_JKS)
          .withProperty("trustStorePassword", "server")
          .withProperty("databaseName", "toto")
          .withProperty("username", "toto")
          .withProperty("userPassword", "totoTutu")
          .build();

    // When
    assertDoesNotThrow(() ->  {
        CloseableResource<MongoDatabase> db = mongoDatabaseFactory.create(target);
        db.getResource().createCollection("titi");
        db.close();
    });
  }
}
