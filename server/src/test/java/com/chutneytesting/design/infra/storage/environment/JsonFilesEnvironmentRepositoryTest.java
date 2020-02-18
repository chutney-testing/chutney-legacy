package com.chutneytesting.design.infra.storage.environment;

import static org.assertj.core.api.Assertions.assertThat;

import com.chutneytesting.design.domain.environment.Environment;
import com.chutneytesting.design.domain.environment.EnvironmentNotFoundException;
import com.chutneytesting.design.domain.environment.EnvironmentRepository;
import com.chutneytesting.design.domain.environment.InvalidEnvironmentNameException;
import com.chutneytesting.design.domain.environment.Target;
import com.chutneytesting.engine.domain.environment.SecurityInfo;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import com.chutneytesting.tools.ThrowingConsumer;
import org.junit.After;
import org.junit.Test;

public class JsonFilesEnvironmentRepositoryTest {

    private static final Path CONFIGURATION_FOLDER = Paths.get("target", "conf");

    private final EnvironmentRepository sut = new JsonFilesEnvironmentRepository(CONFIGURATION_FOLDER.toString());

    @After
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
                        .withId(Target.TargetId.of("target1", "envName"))
                        .withUrl(url)
                        .withSecurity(SecurityInfo.builder()
                            .keyStore("not_existing_keystore")
                            .keyStorePassword("nek")
                            .trustStore("not_existing_trustsore")
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

        assertThat(sut.listNames()).hasSize(1);

        sut.delete("TEST");

        assertThat(sut.listNames()).hasSize(0);
    }

    @Test(expected = EnvironmentNotFoundException.class)
    public void delete_missing_environment_throws() {
        sut.delete("MISSING_ENV");
    }

    @Test(expected = EnvironmentNotFoundException.class)
    public void find_missing_environment_return_default_environment() {
        sut.listNames().forEach(sut::delete);
        sut.findByName("MISSING_ENV");
    }

    @Test(expected = InvalidEnvironmentNameException.class)
    public void save_environment_with_illegal_name_throws() {
        sut.save(Environment.builder().withName("illegal name").withDescription("some description").build());
    }

    @Test
    public void should_backup_env_files() throws IOException {
        // Given
        Path backup = Paths.get("./target/backup", "env");
        Files.createDirectories(backup.getParent());
        Files.deleteIfExists(backup);

        Path env1Path = CONFIGURATION_FOLDER.resolve("env1-tobackup.json");
        Files.createFile(env1Path);
        Path env2Path = CONFIGURATION_FOLDER.resolve("env2-tobackup.json");
        Files.createFile(env2Path);

        try (OutputStream outputStream = Files.newOutputStream(Files.createFile(backup))) {
            // When
            sut.backup(outputStream);
        }

        // Then
        ZipFile zipFile = new ZipFile(backup.toString());
        List<String> entriesNames = zipFile.stream().map(ZipEntry::getName).collect(Collectors.toList());
        assertThat(entriesNames).containsExactlyInAnyOrder(env1Path.getFileName().toString(), env2Path.getFileName().toString());
    }
}
