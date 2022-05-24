package com.chutneytesting.environment.infra;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.chutneytesting.environment.domain.Environment;
import com.chutneytesting.environment.domain.EnvironmentRepository;
import com.chutneytesting.environment.domain.Target;
import com.chutneytesting.environment.domain.exception.EnvironmentNotFoundException;
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
    void should_save_configuration_then_read_it() {
        // Given
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
                        .build()))
            .build();

        // When
        sut.save(environment);

        // Then
        Environment testEnv = sut.findByName("TEST");
        assertThat(testEnv).isNotNull();
        assertThat(testEnv.targets).containsExactly(
            Target.builder().withName("target1").withUrl(url).withEnvironment("TEST").build()
        );
    }

    @Test
    void should_save_configuration_twice_without_creating_duplicate() {
        sut.save(Environment.builder().withName("TEST").withDescription("some description").build());
        assertThat(sut.findByName("TEST").description).isEqualTo("some description");

        sut.save(Environment.builder().withName("TEST").withDescription("some other description").build());

        assertThat(sut.listNames()).hasSize(1);
        assertThat(sut.findByName("TEST").description).isEqualTo("some other description");
    }

    @Test
    void should_list_existing_environments_names() {
        sut.save(Environment.builder().withName("TEST").withDescription("some description").build());
        assertThat(sut.listNames()).contains("TEST");

        sut.delete("TEST");
        assertThat(sut.listNames()).doesNotContain("TEST");
    }

    @Test
    void should_throws_exception_when_delete_missing_environment() {
        assertThatThrownBy(() -> sut.delete("MISSING_ENV"))
            .isInstanceOf(EnvironmentNotFoundException.class);
    }

    @Test
    void should_throws_exception_when_find_missing_environment() {
        assertThatThrownBy(() -> sut.findByName("MISSING_ENV"))
            .isInstanceOf(EnvironmentNotFoundException.class);
    }
}
