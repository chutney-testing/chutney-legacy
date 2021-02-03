package com.chutneytesting.environment.infra;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.chutneytesting.environment.domain.Environment;
import com.chutneytesting.environment.domain.exception.EnvironmentNotFoundException;
import com.chutneytesting.environment.domain.EnvironmentRepository;
import com.chutneytesting.environment.domain.SecurityInfo;
import com.chutneytesting.environment.domain.Target;
import com.chutneytesting.tools.ThrowingConsumer;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.stream.Stream;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

public class JsonFilesEnvironmentRepositoryTest {

    private static final Path CONFIGURATION_FOLDER = Paths.get("target", "conf");

    private final EnvironmentRepository sut = new JsonFilesEnvironmentRepository(CONFIGURATION_FOLDER.toString());

    @AfterEach
    public void after() {
        try (Stream<Path> confStream = Files.list(CONFIGURATION_FOLDER)) {
            confStream.forEach(ThrowingConsumer.toUnchecked(Files::delete));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void saved_configuration_is_readable() {

        // GIVEN
        final String url = "http://target1:8080";
        final Environment environment = Environment.builder()
            .withName("TEST")
            .withDescription("some description")
            .withTargets(
                Collections.singleton(
                    Target.builder()
                        .withName("target1")
                        .withEnvironment("envName")
                        .withUrl(url)
                        .withSecurity(SecurityInfo.builder()
                            .keyStore("not_existing_keystore")
                            .keyStorePassword("nek")
                            .trustStore("not_existing_truststore")
                            .trustStorePassword("net")
                            .credential(SecurityInfo.Credential.of("username", "password"))
                            .build())
                        .build()))
            .build();

        // WHEN
        sut.save(environment);

        // THEN
        assertThat(sut.findByName("TEST")).isNotNull();
        assertThat(sut.findByName("TEST").targets.get(0).url).isEqualTo(url);
    }

    @Test
    public void saving_configuration_twice_does_not_create_duplicate() {
        sut.save(Environment.builder().withName("TEST").withDescription("some description").build());
        assertThat(sut.findByName("TEST").description).isEqualTo("some description");

        sut.save(Environment.builder().withName("TEST").withDescription("some other description").build());

        assertThat(sut.listNames()).hasSize(1);
        assertThat(sut.findByName("TEST").description).isEqualTo("some other description");
    }

    @Test
    public void delete_environment_removes_it_from_list() {
        sut.save(Environment.builder().withName("TEST").withDescription("some description").build());

        assertThat(sut.listNames()).contains("TEST");

        sut.delete("TEST");

        assertThat(sut.listNames()).doesNotContain("TEST");
    }

    @Test()
    public void delete_missing_environment_throws() {
        assertThatThrownBy(() -> sut.delete("MISSING_ENV"))
            .isInstanceOf(EnvironmentNotFoundException.class);
    }

    @Test()
    public void find_missing_environment_return_default_environment() {
        sut.listNames().forEach(sut::delete);
        assertThatThrownBy(() -> sut.findByName("MISSING_ENV"))
            .isInstanceOf(EnvironmentNotFoundException.class);
    }

}
