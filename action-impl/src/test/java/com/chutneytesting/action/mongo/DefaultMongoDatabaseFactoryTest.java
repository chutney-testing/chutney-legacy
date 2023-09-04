package com.chutneytesting.action.mongo;

import com.chutneytesting.action.spi.injectable.Target;
import com.chutneytesting.engine.domain.delegation.NamedHostAndPort;
import com.chutneytesting.engine.domain.environment.TargetImpl;
import java.util.List;
import java.util.Map;
import org.junit.Test;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

@Testcontainers
public class DefaultMongoDatabaseFactoryTest {

  @Container
  final MongoDBContainer mongoDBContainer = new MongoDBContainer(DockerImageName.parse("mongo:latest")).withExposedPorts(27017);

  private final DefaultMongoDatabaseFactory mongoDatabaseFactory = new DefaultMongoDatabaseFactory();


  @Test
  public void should_enable_ssl_connection_according_to_target_properties() {

    // Given
      mongoDBContainer.start();
      Target target =  TargetImpl
        .builder()
        .withName("target name")
        .withUrl(mongoDBContainer.getConnectionString())
        .withProperties(
          Map.of(
            "trustStore", "toto",
            "trustStorePassword", "tutu",
            "keyStore", "",
            "keyStorePassword", ""
          ))
        .withAgents(List.of(new NamedHostAndPort("name", mongoDBContainer.getHost(), mongoDBContainer.getFirstMappedPort())))
        .build();

    // When
    assertDoesNotThrow(() -> mongoDatabaseFactory.create(target).close());
  }

  @Test
  public void should_fail_when_target_does_not_contain_ssl_properties() {

    // Given
      mongoDBContainer.start();
      Target target =  TargetImpl
        .builder()
        .withName("target name")
        .withUrl(mongoDBContainer.getConnectionString())
        .withProperties(
          Map.of())
        .withAgents(List.of(new NamedHostAndPort("name", mongoDBContainer.getHost(), mongoDBContainer.getFirstMappedPort())))
        .build();

    // When / Then
    assertThrows(IllegalArgumentException.class, () -> mongoDatabaseFactory.create(target));
  }
}
