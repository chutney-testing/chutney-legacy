package com.chutneytesting.design.infra.storage.environment;

import static org.assertj.core.api.Assertions.assertThat;

import com.chutneytesting.design.domain.environment.Environment;
import com.chutneytesting.design.domain.environment.EnvironmentNotFoundException;
import com.chutneytesting.design.domain.environment.EnvironmentRepository;
import com.chutneytesting.design.domain.environment.InvalidEnvironmentNameException;
import com.chutneytesting.design.domain.environment.SecurityInfo;
import com.chutneytesting.design.domain.environment.Target;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public class JsonFilesEnvironmentRepositoryTest {

    @ClassRule
    public static final TemporaryFolder TEMPORARY_FOLDER = new TemporaryFolder();

    private static EnvironmentRepository sut;

    @BeforeClass
    public static void setUp() throws IOException {
        String tmpConfDir = TEMPORARY_FOLDER.newFolder("conf").getAbsolutePath();
        System.setProperty("configuration-folder", tmpConfDir);
        System.setProperty("persistence-repository-folder", tmpConfDir);

        sut = new JsonFilesEnvironmentRepository(tmpConfDir);
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

        assertThat(sut.listNames()).contains("TEST");

        sut.delete("TEST");

        assertThat(sut.listNames()).doesNotContain("TEST");
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
        File env1 = TEMPORARY_FOLDER.newFile("conf/env1-tobackup.json");
        File env2 = TEMPORARY_FOLDER.newFile("conf/env2-tobackup.json");
        File envZip = TEMPORARY_FOLDER.newFile("env.zip");

        try (OutputStream outputStream = Files.newOutputStream(envZip.toPath())) {
            // When
            sut.backup(outputStream);
        }

        // Then
        ZipFile zipFile = new ZipFile(envZip);
        List<String> entriesNames = zipFile.stream().map(ZipEntry::getName).collect(Collectors.toList());
        assertThat(entriesNames).containsExactlyInAnyOrder(env1.toPath().getFileName().toString(), env2.toPath().getFileName().toString());
    }
}
